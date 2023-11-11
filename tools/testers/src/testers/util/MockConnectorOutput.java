package testers.util;

import static testers.util.SdkConverters.valueTypeToDataType;

import com.google.common.annotations.VisibleForTesting;
import fivetran_sdk.Checkpoint;
import fivetran_sdk.Column;
import fivetran_sdk.DataType;
import fivetran_sdk.OpType;
import fivetran_sdk.Operation;
import fivetran_sdk.Record;
import fivetran_sdk.Schema;
import fivetran_sdk.SchemaChange;
import fivetran_sdk.Table;
import fivetran_sdk.ValueType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class MockConnectorOutput implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(MockConnectorOutput.class.getName());

    private final Map<OpType, Consumer<Record>> recordMapper = new EnumMap<>(OpType.class);

    private final MockWarehouse destination;
    private final String defaultSchema;
    private final Consumer<String> stateSaver;
    private final Supplier<String> stateLoader;

    private final Map<SchemaTable, List<Column>> tables = new HashMap<>();

    private long upsertCount = 0;
    private long updateCount = 0;
    private long deleteCount = 0;
    private long truncateCount = 0;
    private long checkpointCount = 0;
    private long schemaChangeCount = 0;
    private String latestState = null;

    public MockConnectorOutput(
            MockWarehouse destination,
            String defaultSchema,
            Consumer<String> stateSaver,
            Supplier<String> stateLoader) {
        this.destination = destination;
        this.defaultSchema = defaultSchema;
        this.stateSaver = stateSaver;
        this.stateLoader = stateLoader;

        recordMapper.put(OpType.UPSERT, this::handleUpsert);
        recordMapper.put(OpType.UPDATE, this::handleUpdate);
        recordMapper.put(OpType.DELETE, this::handleDelete);
        recordMapper.put(OpType.TRUNCATE, this::handleTruncate);
    }

    public void enqueueOperation(Operation op) {
        Operation.OpCase opCase = op.getOpCase();
        switch (opCase) {
            case RECORD:
                Record record = op.getRecord();
                recordMapper.get(record.getType()).accept(record);
                break;

            case SCHEMA_CHANGE:
                SchemaChange schemaChange = op.getSchemaChange();
                SchemaChange.ChangeCase changeCase = schemaChange.getChangeCase();
                if (changeCase == SchemaChange.ChangeCase.WITH_SCHEMA) {
                    for (Schema schema : schemaChange.getWithSchema().getSchemasList()) {
                        for (Table table : schema.getTablesList()) {
                            handleSchemaChange(schema.getName(), table);
                        }
                    }
                } else if (changeCase == SchemaChange.ChangeCase.WITHOUT_SCHEMA) {
                    for (Table table : schemaChange.getWithoutSchema().getTablesList()) {
                        handleSchemaChange(defaultSchema, table);
                    }
                }
                break;

            case CHECKPOINT:
                handleCheckpoint(op.getCheckpoint());
                break;

            default:
                throw new RuntimeException("Unrecognized operation: " + opCase);
        }
    }

    private void handleUpsert(Record record) {
        upsertCount++;
        SchemaTable schemaTable =
                new SchemaTable(record.hasSchemaName() ? record.getSchemaName() : defaultSchema, record.getTableName());
        Map<String, ValueType> dataMap = record.getDataMap();

        handleColumnChanges(schemaTable, dataMap);

        destination.upsert(schemaTable, tables.get(schemaTable), dataMap);
        LOG.info(String.format("[Upsert]: %s  Data: %s", schemaTable, dataMap));
    }

    private void handleUpdate(Record record) {
        updateCount++;
        SchemaTable schemaTable =
                new SchemaTable(record.hasSchemaName() ? record.getSchemaName() : defaultSchema, record.getTableName());
        Map<String, ValueType> dataMap = record.getDataMap();

        handleColumnChanges(schemaTable, dataMap);

        destination.update(schemaTable, tables.get(schemaTable), dataMap);
        LOG.info(String.format("[Update]: %s  Data: %s", schemaTable, dataMap));
    }

    private void handleDelete(Record record) {
        deleteCount++;
        SchemaTable schemaTable =
                new SchemaTable(record.hasSchemaName() ? record.getSchemaName() : defaultSchema, record.getTableName());
        Map<String, ValueType> dataMap = record.getDataMap();
        destination.delete(schemaTable, tables.get(schemaTable), dataMap);
        LOG.info(String.format("[Delete]: %s  Data: %s", schemaTable, dataMap));
    }

    private void handleTruncate(Record record) {
        truncateCount++;
        SchemaTable schemaTable =
                new SchemaTable(record.hasSchemaName() ? record.getSchemaName() : defaultSchema, record.getTableName());
        destination.truncate(schemaTable);
        LOG.info(String.format("[Truncate]: %s", schemaTable));
    }

    @VisibleForTesting
    public static DataType mergeTypes(DataType existing, DataType incoming) {
        if (incoming.getNumber() > existing.getNumber()) {
            if (incoming.getNumber() <= DataType.DOUBLE.getNumber()) {
                return incoming;
            } else if (incoming.getNumber() <= DataType.UTC_DATETIME.getNumber()) {
                if (existing.getNumber() >= DataType.NAIVE_DATE.getNumber()) {
                    return incoming;
                } else {
                    return DataType.STRING;
                }
            } else {
                return DataType.STRING;
            }

        } else if (existing.getNumber() > incoming.getNumber()) {
            if (existing.getNumber() <= DataType.DOUBLE.getNumber()) {
                return existing;
            } else if (existing.getNumber() <= DataType.UTC_DATETIME.getNumber()) {
                if (incoming.getNumber() >= DataType.NAIVE_DATE.getNumber()) {
                    return existing;
                } else {
                    return DataType.STRING;
                }
            } else {
                return DataType.STRING;
            }
        }

        return existing;
    }

    private void handleColumnChanges(SchemaTable schemaTable, Map<String, ValueType> dataMap) {
        boolean createTable = false;
        Map<String, Column> existingColumns =
                tables.get(schemaTable).stream().collect(Collectors.toMap(Column::getName, Function.identity()));

        for (String incomingColumnName : dataMap.keySet()) {
            DataType incomingType = valueTypeToDataType(dataMap.get(incomingColumnName));

            if (existingColumns.containsKey(incomingColumnName)) {
                Column existingColumn = existingColumns.get(incomingColumnName);
                Optional<DataType> maybeColumnType = destination.getColumnType(schemaTable, incomingColumnName);
                DataType existingType = maybeColumnType.orElse(existingColumn.getType());
                DataType mergedType = mergeTypes(existingType, incomingType);

                if (mergedType.getNumber() != existingType.getNumber()) {
                    if (existingColumn.getPrimaryKey()) {
                        createTable = true;
                    } else {
                        if (existingType == DataType.UNSPECIFIED) {
                            destination.addColumn(schemaTable, incomingColumnName, incomingType);
                        } else {
                            destination.changeColumnType(schemaTable, incomingColumnName, mergedType);
                        }
                    }

                    updateExistingColumnType(schemaTable, existingColumns, incomingColumnName, mergedType);
                }
            } else {
                destination.addColumn(schemaTable, incomingColumnName, incomingType);

                addNewColumnToTablesMap(schemaTable, incomingColumnName, incomingType);
            }
        }

        if (createTable) {
            destination.createTable(schemaTable, tables.get(schemaTable));
        }
    }

    private void addNewColumnToTablesMap(SchemaTable schemaTable, String columnName, DataType newDataType) {
        Column newColumn = Column.newBuilder().setName(columnName).setPrimaryKey(false).setType(newDataType).build();
        tables.get(schemaTable).add(newColumn);
    }

    private void updateExistingColumnType(
            SchemaTable schemaTable, Map<String, Column> existing, String columnName, DataType newDataType) {
        Column newColumn =
                Column.newBuilder()
                        .setName(columnName)
                        .setPrimaryKey(existing.get(columnName).getPrimaryKey())
                        .setType(newDataType)
                        .build();
        existing.put(columnName, newColumn);
        tables.put(schemaTable, new ArrayList<>(existing.values()));
    }

    public void handleSchemaChange(String schema, Table table) {
        schemaChangeCount++;
        LOG.info(String.format("[SchemaChange]: %s.%s", schema, table.getName()));

        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        if (tables.containsKey(schemaTable)) {
            if (tables.get(schemaTable).equals(table.getColumnsList())) {
                // No change in table
                return;
            }

            // ALTER existing table
            // TODO: Possibilities: 1. add column, 2. change type (compare against existing Table)

        } else {
            List<Column> columns = new ArrayList<>(table.getColumnsList());
            if (columns.stream().noneMatch(c -> c.getPrimaryKey() && c.getType() == DataType.UNSPECIFIED)) {
                List<Column> specifiedColumns =
                        columns.stream().filter(c -> c.getType() != DataType.UNSPECIFIED).collect(Collectors.toList());
                destination.createTable(schemaTable, specifiedColumns);
            } else {
                LOG.warning("Cannot create table with any UNSPECIFIED PK column(s)");
            }

            tables.put(schemaTable, columns);
        }
    }

    private void handleCheckpoint(Checkpoint checkpoint) {
        checkpointCount++;

        String newStateJson = checkpoint.getStateJson();
        stateSaver.accept(newStateJson);
        latestState = newStateJson;
        LOG.info("Checkpoint: " + latestState);
    }

    // Core is responsible for saving state, why would it not be able to provide the latest state to a connector?!
    public String getState() {
        if (latestState == null) {
            latestState = stateLoader.get();
        }

        return latestState;
    }

    public void displayReport() {
        LOG.info(
                String.format(
                        "Upserts: %d\nUpdates: %d\nDeletes: %d\nTruncates: %d\nSchemaChanges: %d\nCheckpoints: %d",
                        upsertCount, updateCount, deleteCount, truncateCount, schemaChangeCount, checkpointCount));
    }

    @Override
    public void close() {}
}
