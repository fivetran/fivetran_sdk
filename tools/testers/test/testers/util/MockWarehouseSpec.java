package testers.util;

import static testers.util.SdkConverters.SYS_CLOCK;
import static testers.util.SdkConverters.objectToValueType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fivetran_sdk.Column;
import fivetran_sdk.DataType;
import fivetran_sdk.DecimalParams;
import fivetran_sdk.Table;
import fivetran_sdk.ValueType;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.io.FileUtils;
import org.junit.*;

public class MockWarehouseSpec {
    private static MockWarehouse warehouse = null;
    private static Path tmpDbDir = null;

    @BeforeClass
    public static void setup() throws IOException {
        tmpDbDir = Files.createTempDirectory("test-db-");
        warehouse = new MockWarehouse(Paths.get(tmpDbDir.toString(), "test.db"));
    }

    @AfterClass
    public static void teardown() throws IOException {
        FileUtils.deleteDirectory(tmpDbDir.toFile());
    }

    static Table buildTable(String prefix) {
        return Table.newBuilder()
                .addAllColumns(
                        Arrays.asList(
                                Column.newBuilder().setName("id1").setType(DataType.INT).setPrimaryKey(true).build(),
                                Column.newBuilder().setName("id2").setType(DataType.INT).setPrimaryKey(true).build(),
                                Column.newBuilder()
                                        .setName("isL")
                                        .setType(DataType.BOOLEAN)
                                        .setPrimaryKey(false)
                                        .build(),
                                Column.newBuilder()
                                        .setName("dbl")
                                        .setType(DataType.DOUBLE)
                                        .setPrimaryKey(false)
                                        .build()))
                .setName(generateRandomName(prefix))
                .build();
    }

    static Table buildTableAllTypes(String prefix) {
        return Table.newBuilder()
                .addAllColumns(
                        Arrays.asList(
                                Column.newBuilder().setName("id1").setType(DataType.STRING).setPrimaryKey(true).build(),
                                Column.newBuilder()
                                        .setName("_bool")
                                        .setType(DataType.BOOLEAN)
                                        .setPrimaryKey(false)
                                        .build(),
                                Column.newBuilder()
                                        .setName("_shrt")
                                        .setType(DataType.SHORT)
                                        .setPrimaryKey(false)
                                        .build(),
                                Column.newBuilder().setName("_int").setType(DataType.INT).setPrimaryKey(false).build(),
                                Column.newBuilder().setName("_lng").setType(DataType.LONG).setPrimaryKey(false).build(),
                                Column.newBuilder()
                                        .setName("_dec")
                                        .setType(DataType.DECIMAL)
                                        .setDecimal(DecimalParams.newBuilder().setPrecision(8).setScale(3).build())
                                        .setPrimaryKey(false)
                                        .build(),
                                Column.newBuilder()
                                        .setName("_flt")
                                        .setType(DataType.FLOAT)
                                        .setPrimaryKey(false)
                                        .build(),
                                Column.newBuilder()
                                        .setName("_dbl")
                                        .setType(DataType.DOUBLE)
                                        .setPrimaryKey(false)
                                        .build(),
                                Column.newBuilder()
                                        .setName("_date")
                                        .setType(DataType.NAIVE_DATE)
                                        .setPrimaryKey(false)
                                        .build(),
                                Column.newBuilder()
                                        .setName("_dt")
                                        .setType(DataType.NAIVE_DATETIME)
                                        .setPrimaryKey(false)
                                        .build(),
                                Column.newBuilder().setName("_xml").setType(DataType.XML).setPrimaryKey(false).build(),
                                Column.newBuilder()
                                        .setName("_json")
                                        .setType(DataType.JSON)
                                        .setPrimaryKey(false)
                                        .build(),
                                Column.newBuilder()
                                        .setName("_udt")
                                        .setType(DataType.UTC_DATETIME)
                                        .setPrimaryKey(false)
                                        .build()))
                .setName(generateRandomName(prefix))
                .build();
    }

    static String generateRandomName(String prefix) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        return prefix
                + random.ints(leftLimit, rightLimit + 1)
                        .limit(7)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString();
    }

    @Test
    public void create_newTable() {
        String schema = generateRandomName("my_schema_");
        Table table = buildTable("my_table_");
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());

        warehouse.createTable(schemaTable, table.getColumnsList());

        assertTrue(warehouse.exists(schemaTable));
    }

    @Test
    public void insert_allTypes() {
        String schema = generateRandomName("my_schema_");
        Table table = buildTableAllTypes("my_table_");
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        warehouse.createTable(schemaTable, table.getColumnsList());

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", objectToValueType("abc"));
        row.put("_bool", objectToValueType(true));
        row.put("_shrt", objectToValueType((short) 15));
        row.put("_int", objectToValueType(1));
        row.put("_lng", objectToValueType(123L));
        row.put("_dec", objectToValueType(new BigDecimal("12.345")));
        row.put("_flt", objectToValueType(345.355f));
        row.put("_dbl", objectToValueType(879.345d));
        row.put("_date", objectToValueType(LocalDate.now(SYS_CLOCK)));
        row.put("_dt", objectToValueType(LocalDateTime.now(SYS_CLOCK)));
        row.put("_udt", objectToValueType(SYS_CLOCK.instant()));
        row.put("_xml", objectToValueType("<xml>123</xml>"));
        row.put("_json", objectToValueType("{\"field\": \"value\"}"));
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        List<Map<String, ValueType>> rows = warehouse.read(schemaTable);
        assertEquals(rows.size(), 1);
        assertEquals(rows.get(0), row);
    }

    @Test
    public void upsert() {
        String schema = generateRandomName("my_schema_");
        Table table = buildTable("my_table_");
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        warehouse.createTable(schemaTable, table.getColumnsList());

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", ValueType.newBuilder().setInt(1).build());
        row.put("id2", ValueType.newBuilder().setInt(100).build());
        row.put("isL", ValueType.newBuilder().setBool(true).build());
        row.put("dbl", ValueType.newBuilder().setDouble(2.1235d).build());
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        row.put("isL", ValueType.newBuilder().setBool(false).build());
        row.put("dbl", ValueType.newBuilder().setDouble(100.456d).build());
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        List<Map<String, ValueType>> rows = warehouse.read(schemaTable);
        assertEquals(rows.size(), 1);
        assertEquals(rows.get(0), row);
    }

    @Test
    public void upsert_ColumnOrderMatters() {
        String schema = generateRandomName("my_schema_");
        Table table =
                Table.newBuilder()
                        .addAllColumns(
                                Arrays.asList(
                                        Column.newBuilder()
                                                .setName("store_id")
                                                .setType(DataType.INT)
                                                .setPrimaryKey(true)
                                                .build(),
                                        Column.newBuilder()
                                                .setName("manager_staff_id")
                                                .setType(DataType.INT)
                                                .setPrimaryKey(false)
                                                .build(),
                                        Column.newBuilder()
                                                .setName("address_id")
                                                .setType(DataType.INT)
                                                .setPrimaryKey(false)
                                                .build(),
                                        Column.newBuilder()
                                                .setName("last_update")
                                                .setType(DataType.UTC_DATETIME)
                                                .setPrimaryKey(false)
                                                .build()))
                        .setName(generateRandomName("my_table_"))
                        .build();
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        warehouse.createTable(schemaTable, table.getColumnsList());

        Map<String, ValueType> row = new LinkedHashMap<>();
        row.put("store_id", objectToValueType(1));
        row.put("manager_staff_id", objectToValueType(1));
        row.put("address_id", objectToValueType(1));
        row.put("last_update", objectToValueType(SYS_CLOCK.instant()));
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        List<Map<String, ValueType>> rows = warehouse.read(schemaTable);
        assertEquals(rows.size(), 1);
        assertEquals(rows.get(0), row);
    }

    @Test
    public void addColumn() {
        String schema = generateRandomName("my_schema_");
        Table table = buildTable("my_table_");
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        warehouse.createTable(schemaTable, table.getColumnsList());

        warehouse.addColumn(schemaTable, "new_col", DataType.UTC_DATETIME);

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", ValueType.newBuilder().setInt(1).build());
        row.put("id2", ValueType.newBuilder().setInt(100).build());
        row.put("isL", ValueType.newBuilder().setBool(true).build());
        row.put("dbl", ValueType.newBuilder().setDouble(2.1235d).build());
        row.put("new_col", objectToValueType(SYS_CLOCK.instant()));
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        List<Map<String, ValueType>> rows = warehouse.read(schemaTable);
        assertEquals(rows.size(), 1);
        assertEquals(rows.get(0), row);
    }

    @Test
    public void update_singleColumn() {
        String schema = generateRandomName("my_schema_");
        Table table = buildTable("my_table_");
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        warehouse.createTable(schemaTable, table.getColumnsList());

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", ValueType.newBuilder().setInt(1).build());
        row.put("id2", ValueType.newBuilder().setInt(100).build());
        row.put("isL", ValueType.newBuilder().setBool(true).build());
        row.put("dbl", ValueType.newBuilder().setDouble(2.1235d).build());
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        row.clear();
        row.put("id1", ValueType.newBuilder().setInt(1).build());
        row.put("id2", ValueType.newBuilder().setInt(100).build());
        row.put("dbl", ValueType.newBuilder().setDouble(160.456d).build());
        warehouse.update(schemaTable, table.getColumnsList(), row);

        List<Map<String, ValueType>> rows = warehouse.read(schemaTable);
        row.put("isL", ValueType.newBuilder().setBool(true).build());
        assertEquals(rows.size(), 1);
        assertEquals(rows.get(0), row);
    }

    @Test
    public void update_multipleColumns() {
        String schema = generateRandomName("my_schema_");
        Table table = buildTable("my_table_");
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        warehouse.createTable(schemaTable, table.getColumnsList());

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", ValueType.newBuilder().setInt(1).build());
        row.put("id2", ValueType.newBuilder().setInt(100).build());
        row.put("isL", ValueType.newBuilder().setBool(true).build());
        row.put("dbl", ValueType.newBuilder().setDouble(2.1235d).build());
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        row.clear();
        row.put("id1", ValueType.newBuilder().setInt(1).build());
        row.put("id2", ValueType.newBuilder().setInt(100).build());
        row.put("isL", ValueType.newBuilder().setBool(false).build());
        row.put("dbl", ValueType.newBuilder().setNull(true).build());
        warehouse.update(schemaTable, table.getColumnsList(), row);

        List<Map<String, ValueType>> rows = warehouse.read(schemaTable);
        assertEquals(rows.size(), 1);
        assertEquals(rows.get(0), row);
    }

    @Test
    public void delete() {
        String schema = generateRandomName("my_schema_");
        Table table = buildTable("my_table_");
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        warehouse.createTable(schemaTable, table.getColumnsList());

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", ValueType.newBuilder().setInt(1).build());
        row.put("id2", ValueType.newBuilder().setInt(100).build());
        row.put("isL", ValueType.newBuilder().setBool(true).build());
        row.put("dbl", ValueType.newBuilder().setDouble(2.1235d).build());
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        List<Map<String, ValueType>> rows = warehouse.read(schemaTable);
        assertEquals(rows.size(), 1);

        row.clear();
        row.put("id1", ValueType.newBuilder().setInt(1).build());
        row.put("id2", ValueType.newBuilder().setInt(100).build());
        warehouse.delete(schemaTable, table.getColumnsList(), row);

        List<Map<String, ValueType>> rows2 = warehouse.read(schemaTable);
        assertEquals(rows2.size(), 0);
    }

    @Test
    public void change_columnType() {
        String schema = generateRandomName("my_schema_");
        Table table = buildTable("my_table_");
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        warehouse.createTable(schemaTable, table.getColumnsList());

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", objectToValueType(1));
        row.put("id2", objectToValueType(100));
        row.put("isL", objectToValueType(true));
        row.put("dbl", objectToValueType(2.1235d));
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        warehouse.changeColumnType(schemaTable, "dbl", DataType.STRING);

        row.put("dbl", objectToValueType("testing"));
        warehouse.update(schemaTable, table.getColumnsList(), row);

        List<Map<String, ValueType>> rows = warehouse.read(schemaTable);
        assertEquals(rows.size(), 1);
        assertEquals(rows.get(0), row);
    }

    @Test
    public void truncate() {
        String schema = generateRandomName("my_schema_");
        Table table = buildTable("my_table_");
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        warehouse.createTable(schemaTable, table.getColumnsList());

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", ValueType.newBuilder().setInt(1).build());
        row.put("id2", ValueType.newBuilder().setInt(100).build());
        row.put("isL", ValueType.newBuilder().setBool(true).build());
        row.put("dbl", ValueType.newBuilder().setDouble(2.1235d).build());
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        warehouse.truncate(schemaTable);

        List<Map<String, ValueType>> rows = warehouse.read(schemaTable);
        assertEquals(rows.size(), 0);
    }

    @Test
    public void valueTypeToString() {
        Map<String, ValueType> row = new HashMap<>();
        row.put("_ldt", objectToValueType(LocalDateTime.parse("2005-05-26T20:57:00")));

        var result = SdkConverters.valueTypeToString(row);

        assertEquals(result.get("_ldt"), "'2005-05-26T20:57:00'");
    }

    @Test
    public void getColumnType() {
        String schema = generateRandomName("my_schema_");
        Table table = buildTableAllTypes("my_table_");
        SchemaTable schemaTable = new SchemaTable(schema, table.getName());
        warehouse.createTable(schemaTable, table.getColumnsList());

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", objectToValueType("abc"));
        row.put("_bool", objectToValueType(true));
        row.put("_shrt", objectToValueType((short) 15));
        row.put("_int", objectToValueType(1));
        row.put("_lng", objectToValueType(123L));
        row.put("_dec", objectToValueType(new BigDecimal("12.345")));
        row.put("_flt", objectToValueType(345.355f));
        row.put("_dbl", objectToValueType(879.345d));
        row.put("_date", objectToValueType(LocalDate.now(SYS_CLOCK)));
        row.put("_dt", objectToValueType(LocalDateTime.now(SYS_CLOCK)));
        row.put("_udt", objectToValueType(SYS_CLOCK.instant()));
        row.put("_xml", objectToValueType("<xml>123</xml>"));
        row.put("_json", objectToValueType("{\"field\": \"value\"}"));
        warehouse.upsert(schemaTable, table.getColumnsList(), row);

        assertEquals(warehouse.getColumnType(schemaTable, "id1").get(), DataType.STRING);
        assertEquals(warehouse.getColumnType(schemaTable, "_bool").get(), DataType.BOOLEAN);
        assertEquals(warehouse.getColumnType(schemaTable, "_shrt").get(), DataType.SHORT);
        assertEquals(warehouse.getColumnType(schemaTable, "_int").get(), DataType.INT);
        assertEquals(warehouse.getColumnType(schemaTable, "_lng").get(), DataType.LONG);
        assertEquals(warehouse.getColumnType(schemaTable, "_dec").get(), DataType.DECIMAL);
        assertEquals(warehouse.getColumnType(schemaTable, "_flt").get(), DataType.FLOAT);
        assertEquals(warehouse.getColumnType(schemaTable, "_dbl").get(), DataType.DOUBLE);
        assertEquals(warehouse.getColumnType(schemaTable, "_date").get(), DataType.NAIVE_DATE);
        assertEquals(warehouse.getColumnType(schemaTable, "_dt").get(), DataType.NAIVE_DATETIME);
        assertEquals(warehouse.getColumnType(schemaTable, "_udt").get(), DataType.UTC_DATETIME);
        assertEquals(warehouse.getColumnType(schemaTable, "_xml").get(), DataType.STRING);
        assertEquals(warehouse.getColumnType(schemaTable, "_json").get(), DataType.STRING);
    }
}
