package testers.util;

import static testers.util.SdkConverters.objectToValueType;
import static testers.util.SdkConverters.valueTypeToString;
import static java.util.stream.Collectors.joining;

import fivetran_sdk.Column;
import fivetran_sdk.DataType;
import fivetran_sdk.ValueType;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class MockWarehouse implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(MockWarehouse.class.getName());

    private final Path dbFile;

    public MockWarehouse(Path dbFile) {
        this.dbFile = dbFile;

        try {
            Class.forName("org.duckdb.DuckDBDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find DuckDB driver", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:duckdb:" + dbFile.toString());
    }

    private <T> T callOnConnection(Function<Connection, T> function) {
        try (Connection connection = getConnection()) {
            return function.apply(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void runOnConnection(Consumer<Connection> consumer) {
        try (Connection connection = getConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(SchemaTable schemaTable) {
        return callOnConnection(
                c -> {
                    String sql =
                            String.format(
                                    "select * from information_schema.tables "
                                            + "where table_schema = '%s' and table_name = '%s'",
                                    schemaTable.schema, schemaTable.table);
                    try (Statement s = c.createStatement();
                            ResultSet rs = s.executeQuery(sql)) {
                        return rs.next();

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public List<Map<String, ValueType>> read(SchemaTable schemaTable) {
        return callOnConnection(
                c -> {
                    List<Map<String, ValueType>> rows = new ArrayList<>();

                    try (Statement s = c.createStatement();
                            ResultSet rs = s.executeQuery(String.format("select * from %s", schemaTable))) {
                        ResultSetMetaData md = rs.getMetaData();
                        int numColumns = md.getColumnCount();

                        while (rs.next()) {
                            Map<String, ValueType> row = new HashMap<>(numColumns);
                            for (int i = 1; i <= numColumns; ++i) {
                                // TODO: DuckDB jdbc driver currently does not support reading BLOBs
                                Object raw = (md.getColumnType(i) == java.sql.Types.BLOB) ? null : rs.getObject(i);
                                row.put(md.getColumnName(i), objectToValueType(raw));
                            }
                            rows.add(row);
                        }

                        return rows;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void upsert(SchemaTable schemaTable, List<Column> columns, Map<String, ValueType> dataMap) {
        LOG.fine(String.format("[Upsert]: %s  Data: %s", schemaTable, dataMap));

        List<String> pkeys = getPrimaryKeys(columns);
        String columnNames = dataMap.keySet().stream().map(MockWarehouse::renamer).collect(joining(","));
        String values = String.join(",", valueTypeToString(dataMap).values());
        String updateOnConflict =
                dataMap.keySet()
                        .stream()
                        .filter(col -> !getPrimaryKeys(columns).contains(col))
                        .map(col -> String.format("%s = excluded.%s", renamer(col), renamer(col)))
                        .collect(joining(","));

        String sqlUpsert =
                "INSERT INTO "
                        + schemaTable.toString(MockWarehouse::renamer)
                        + "("
                        + columnNames
                        + ") VALUES ("
                        + values
                        + ") ON CONFLICT ("
                        + String.join(",", pkeys)
                        + ") DO UPDATE SET "
                        + updateOnConflict;

        try (Connection c = getConnection();
                Statement s = c.createStatement()) {
            s.execute(sqlUpsert);
        } catch (SQLException e) {
            // TODO: duckdb < 0.7.0 does not support UPSERT operation so we need to do it manually
            if (e.getMessage().contains("Constraint Error: Duplicate key")) {
                update(schemaTable, columns, dataMap);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public void update(SchemaTable schemaTable, List<Column> columns, Map<String, ValueType> dataMap) {
        LOG.fine(String.format("[Update]: %s  Data: %s", schemaTable, dataMap));

        List<String> pkeys = getPrimaryKeys(columns);
        String values =
                valueTypeToString(dataMap)
                        .entrySet()
                        .stream()
                        .filter(e -> !pkeys.contains(e.getKey()))
                        .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                        .collect(joining(","));

        String where =
                valueTypeToString(dataMap)
                        .entrySet()
                        .stream()
                        .filter(e -> pkeys.contains(e.getKey()))
                        .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                        .collect(joining(" AND "));

        String sqlUpdate =
                "UPDATE " + schemaTable.toString(MockWarehouse::renamer) + " SET " + values + " WHERE " + where;

        try (Connection c = getConnection();
                Statement s = c.createStatement()) {
            s.execute(sqlUpdate);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(SchemaTable schemaTable, List<Column> columns, Map<String, ValueType> dataMap) {
        LOG.fine(String.format("[Delete]: %s  Data: %s", schemaTable, dataMap));

        List<String> pkeys = getPrimaryKeys(columns);
        String where =
                valueTypeToString(dataMap)
                        .entrySet()
                        .stream()
                        .filter(e -> pkeys.contains(e.getKey()))
                        .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                        .collect(joining(" AND "));

        String sqlDelete = "DELETE FROM " + schemaTable.toString(MockWarehouse::renamer) + " WHERE " + where;

        try (Connection c = getConnection();
                Statement s = c.createStatement()) {
            s.execute(sqlDelete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void truncate(SchemaTable schemaTable) {
        LOG.fine(String.format("[Truncate]: %s", schemaTable));

        try (Connection c = getConnection();
                Statement s = c.createStatement()) {
            s.execute("DELETE FROM " + schemaTable.toString(MockWarehouse::renamer));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<DataType> getColumnType(SchemaTable schemaTable, String column) {
        LOG.fine("[GetColumnType]: " + schemaTable + "." + column);

        String type =
                callOnConnection(
                        conn -> {
                            String sql =
                                    String.format(
                                            "SELECT data_type FROM information_schema.columns WHERE "
                                                    + " table_schema = '%s' AND table_name = '%s' AND column_name = '%s'",
                                            schemaTable.schema, schemaTable.table, column);

                            try (PreparedStatement p = conn.prepareStatement(sql);
                                    ResultSet r = p.executeQuery()) {
                                if (r.next()) {
                                    return r.getString("data_type");
                                }

                                return null;
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });

        if (type == null) return Optional.empty();

        return Optional.of(sqlTypeToDataType(type));
    }

    public void addColumn(SchemaTable schemaTable, String column, DataType dataType) {
        LOG.fine("[AddColumn]: " + schemaTable + "." + column + "  Type: " + dataType.name());

        runOnConnection(
                conn -> {
                    String sql =
                            "ALTER TABLE "
                                    + schemaTable.toString(MockWarehouse::renamer)
                                    + " ADD COLUMN "
                                    + renamer(column)
                                    + " "
                                    + sqlType(dataType);

                    try (Statement s = conn.createStatement()) {
                        s.execute(sql);
                    } catch (SQLException e) {
                        if (!(e.getMessage().contains("Column with name")
                                && e.getMessage().contains("already exists"))) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    public void changeColumnType(SchemaTable schemaTable, String column, DataType dataType) {
        LOG.fine("[ChangeColumnType]: " + schemaTable + "." + column + "  NewType: " + dataType.name());

        runOnConnection(
                conn -> {
                    String sql =
                            "ALTER TABLE "
                                    + schemaTable.toString(MockWarehouse::renamer)
                                    + " ALTER "
                                    + renamer(column)
                                    + " TYPE "
                                    + sqlType(dataType);

                    try (Statement s = conn.createStatement()) {
                        s.execute(sql);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void createTable(SchemaTable schemaTable, List<Column> columns) {
        LOG.info("[CreateTable]: " + schemaTable);

        runOnConnection(
                conn -> {
                    try (Statement s = conn.createStatement()) {
                        s.execute("CREATE SCHEMA IF NOT EXISTS " + renamer(schemaTable.schema));

                        String columnTypes =
                                columns.stream()
                                        .map(c -> String.format("%s %s", c.getName(), sqlType(c)))
                                        .collect(joining(","));
                        String pkeys = String.join(",", getPrimaryKeys(columns));
                        String sqlCreateTable =
                                "CREATE TABLE IF NOT EXISTS "
                                        + schemaTable.toString(MockWarehouse::renamer)
                                        + " ("
                                        + columnTypes
                                        + ", PRIMARY KEY("
                                        + pkeys
                                        + "))";
                        s.execute(sqlCreateTable);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static String renamer(String incoming) {
        // Replace all illegal characters
        String name = incoming.replaceAll("[^\\w\\d]", "_");

        // Convert to lower_case
        name = name.toLowerCase();

        // Require that it start with a word character
        String validLeadingRegex = "[\\w_]";
        if (!name.isEmpty() && !name.substring(0, 1).matches(validLeadingRegex)) name = "_" + name;

        return name;
    }

    private static List<String> getPrimaryKeys(Collection<Column> columns) {
        return columns.stream().filter(Column::getPrimaryKey).map(Column::getName).collect(Collectors.toList());
    }

    private static String sqlType(Column c) {
        DataType dataType = c.getType();

        if (dataType == DataType.DECIMAL) {
            return String.format("DECIMAL(%d,%d)", c.getDecimal().getPrecision(), c.getDecimal().getScale());
        }

        return sqlType(dataType);
    }

    private static DataType sqlTypeToDataType(String dbType) {
        String type = dbType.toLowerCase();

        if (type.startsWith("varchar")) return DataType.STRING;
        else if (type.equals("integer")) return DataType.INT;
        else if (type.equals("smallint")) return DataType.SHORT;
        else if (type.equals("bigint")) return DataType.LONG;
        else if (type.equals("float")) return DataType.FLOAT;
        else if (type.equals("double")) return DataType.DOUBLE;
        else if (type.startsWith("decimal")) return DataType.DECIMAL;
        else if (type.startsWith("bool")) return DataType.BOOLEAN;
        else if (type.equals("bytea")) return DataType.BINARY;
        else if (type.equals("blob")) return DataType.BINARY;
        else if (type.equals("date")) return DataType.NAIVE_DATE;
        else if (type.equals("timestamp")) return DataType.NAIVE_DATETIME;
        else if (type.equals("timestamp with time zone")) return DataType.UTC_DATETIME;

        throw new RuntimeException("Unexpected type: " + type);
    }

    private static String sqlType(DataType dataType) {
        if (dataType == DataType.XML || dataType == DataType.JSON) return "STRING";
        else if (dataType == DataType.BINARY) return "BYTEA";
        else if (dataType == DataType.NAIVE_DATE) return "DATE";
        else if (dataType == DataType.NAIVE_DATETIME) return "TIMESTAMP";
        else if (dataType == DataType.UTC_DATETIME) return "TIMESTAMPTZ";
        else return dataType.name();
    }

    @Override
    public void close() throws Exception {}
}
