package testers.util;

import static testers.util.SdkConverters.SYS_CLOCK;
import static testers.util.SdkConverters.objectToValueType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import fivetran_sdk.Column;
import fivetran_sdk.DataType;
import fivetran_sdk.OpType;
import fivetran_sdk.Operation;
import fivetran_sdk.Record;
import fivetran_sdk.Table;
import fivetran_sdk.ValueType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MockConnectorOutputSpec {
    private static final String SCHEMA_NAME = "my_schema";
    private static final String TABLE_NAME_PREFIX = "my_table";
    private MockConnectorOutput connectorOutput;
    @Mock private MockWarehouse destination;

    @Before
    public void setup() {
        connectorOutput = new MockConnectorOutput(destination, "defaultSchema", (s) -> {}, () -> "{}");
    }

    static Record createRecord(String schema, String table, OpType opType, Map<String, ValueType> data) {
        return Record.newBuilder().setSchemaName(schema).setTableName(table).setType(opType).putAllData(data).build();
    }

    @Test
    public void createTableUpFront() {
        Table table = MockWarehouseSpec.buildTable(TABLE_NAME_PREFIX);
        SchemaTable schemaTable = new SchemaTable(SCHEMA_NAME, table.getName());
        connectorOutput.handleSchemaChange(SCHEMA_NAME, table);

        verify(destination).createTable(schemaTable, table.getColumnsList());
    }

    @Test
    public void createTable_skip_allUnspecifiedPkeys() {
        Table table =
                Table.newBuilder()
                        .setName(MockWarehouseSpec.generateRandomName(TABLE_NAME_PREFIX))
                        .addAllColumns(
                                Arrays.asList(
                                        Column.newBuilder()
                                                .setName("id1")
                                                .setType(DataType.UNSPECIFIED)
                                                .setPrimaryKey(true)
                                                .build(),
                                        Column.newBuilder()
                                                .setName("id2")
                                                .setType(DataType.UNSPECIFIED)
                                                .setPrimaryKey(true)
                                                .build(),
                                        Column.newBuilder()
                                                .setName("dbl")
                                                .setType(DataType.DOUBLE)
                                                .setPrimaryKey(false)
                                                .build()))
                        .build();
        connectorOutput.handleSchemaChange(SCHEMA_NAME, table);

        verify(destination, never()).createTable(any(), anyList());
    }

    @Test
    public void createTable_skip_someUnspecifiedPkeys() {
        Table table =
                Table.newBuilder()
                        .setName(MockWarehouseSpec.generateRandomName(TABLE_NAME_PREFIX))
                        .addAllColumns(
                                Arrays.asList(
                                        Column.newBuilder()
                                                .setName("id1")
                                                .setType(DataType.UNSPECIFIED)
                                                .setPrimaryKey(true)
                                                .build(),
                                        Column.newBuilder()
                                                .setName("id2")
                                                .setType(DataType.INT)
                                                .setPrimaryKey(true)
                                                .build(),
                                        Column.newBuilder()
                                                .setName("dbl")
                                                .setType(DataType.DOUBLE)
                                                .setPrimaryKey(false)
                                                .build()))
                        .build();
        connectorOutput.handleSchemaChange(SCHEMA_NAME, table);

        verify(destination, never()).createTable(any(), anyList());
    }

    @Test
    public void merge() {
        Assert.assertEquals(DataType.LONG, MockConnectorOutput.mergeTypes(DataType.BOOLEAN, DataType.LONG));
        Assert.assertEquals(DataType.DECIMAL, MockConnectorOutput.mergeTypes(DataType.DECIMAL, DataType.SHORT));
        Assert.assertEquals(DataType.STRING, MockConnectorOutput.mergeTypes(DataType.INT, DataType.NAIVE_DATETIME));
        Assert.assertEquals(DataType.STRING, MockConnectorOutput.mergeTypes(DataType.NAIVE_DATE, DataType.FLOAT));
        Assert.assertEquals(
                DataType.UTC_DATETIME, MockConnectorOutput.mergeTypes(DataType.NAIVE_DATE, DataType.UTC_DATETIME));
        Assert.assertEquals(
                DataType.NAIVE_DATETIME, MockConnectorOutput.mergeTypes(DataType.NAIVE_DATETIME, DataType.NAIVE_DATE));
        Assert.assertEquals(DataType.STRING, MockConnectorOutput.mergeTypes(DataType.BINARY, DataType.NAIVE_DATETIME));
        Assert.assertEquals(DataType.STRING, MockConnectorOutput.mergeTypes(DataType.NAIVE_DATE, DataType.XML));
        Assert.assertEquals(DataType.STRING, MockConnectorOutput.mergeTypes(DataType.XML, DataType.LONG));
        Assert.assertEquals(DataType.STRING, MockConnectorOutput.mergeTypes(DataType.INT, DataType.BINARY));
        Assert.assertEquals(DataType.STRING, MockConnectorOutput.mergeTypes(DataType.STRING, DataType.FLOAT));
        Assert.assertEquals(DataType.STRING, MockConnectorOutput.mergeTypes(DataType.BINARY, DataType.XML));
        Assert.assertEquals(DataType.STRING, MockConnectorOutput.mergeTypes(DataType.XML, DataType.JSON));
        Assert.assertEquals(DataType.SHORT, MockConnectorOutput.mergeTypes(DataType.SHORT, DataType.SHORT));
    }

    @Test
    public void upsert_createNewTable() {
        Table table =
                Table.newBuilder()
                        .setName(MockWarehouseSpec.generateRandomName(TABLE_NAME_PREFIX))
                        .addAllColumns(
                                Arrays.asList(
                                        Column.newBuilder()
                                                .setName("id1")
                                                .setType(DataType.UNSPECIFIED)
                                                .setPrimaryKey(true)
                                                .build(),
                                        Column.newBuilder()
                                                .setName("dbl")
                                                .setType(DataType.DOUBLE)
                                                .setPrimaryKey(false)
                                                .build()))
                        .build();
        connectorOutput.handleSchemaChange(SCHEMA_NAME, table);

        verify(destination, never()).createTable(any(), anyList());

        SchemaTable schemaTable = new SchemaTable(SCHEMA_NAME, table.getName());
        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", objectToValueType(100));
        row.put("dbl", objectToValueType(1.234d));
        Operation upsert =
                Operation.newBuilder()
                        .setRecord(createRecord(SCHEMA_NAME, table.getName(), OpType.UPSERT, row))
                        .build();
        connectorOutput.enqueueOperation(upsert);

        List<Column> expectedColumns =
                Arrays.asList(
                        Column.newBuilder().setName("id1").setType(DataType.INT).setPrimaryKey(true).build(),
                        Column.newBuilder().setName("dbl").setType(DataType.DOUBLE).setPrimaryKey(false).build());
        verify(destination).createTable(schemaTable, expectedColumns);
        verify(destination).upsert(schemaTable, expectedColumns, row);
    }

    @Test
    public void upsert_upgradeNonPkColumn() {
        List<Column> columns =
                Arrays.asList(
                        Column.newBuilder().setName("id1").setType(DataType.INT).setPrimaryKey(true).build(),
                        Column.newBuilder().setName("dbl").setType(DataType.DOUBLE).setPrimaryKey(false).build());
        Table table =
                Table.newBuilder()
                        .setName(MockWarehouseSpec.generateRandomName(TABLE_NAME_PREFIX))
                        .addAllColumns(columns)
                        .build();
        connectorOutput.handleSchemaChange(SCHEMA_NAME, table);

        SchemaTable schemaTable = new SchemaTable(SCHEMA_NAME, table.getName());
        verify(destination).createTable(schemaTable, columns);

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", objectToValueType(100));
        row.put("dbl", objectToValueType(SYS_CLOCK.instant()));
        Operation upsert =
                Operation.newBuilder()
                        .setRecord(createRecord(SCHEMA_NAME, table.getName(), OpType.UPSERT, row))
                        .build();
        connectorOutput.enqueueOperation(upsert);

        List<Column> expectedColumns =
                Arrays.asList(
                        Column.newBuilder().setName("id1").setType(DataType.INT).setPrimaryKey(true).build(),
                        Column.newBuilder().setName("dbl").setType(DataType.STRING).setPrimaryKey(false).build());
        verify(destination).changeColumnType(schemaTable, "dbl", DataType.STRING);
        verify(destination).upsert(schemaTable, expectedColumns, row);
    }

    @Test
    public void addMissingNonPkColumn() {
        List<Column> columns =
                Arrays.asList(
                        Column.newBuilder().setName("id1").setType(DataType.INT).setPrimaryKey(true).build(),
                        Column.newBuilder().setName("dbl").setType(DataType.DOUBLE).setPrimaryKey(false).build());
        Table table =
                Table.newBuilder()
                        .setName(MockWarehouseSpec.generateRandomName(TABLE_NAME_PREFIX))
                        .addAllColumns(columns)
                        .build();
        connectorOutput.handleSchemaChange(SCHEMA_NAME, table);

        SchemaTable schemaTable = new SchemaTable(SCHEMA_NAME, table.getName());
        verify(destination).createTable(schemaTable, columns);

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", objectToValueType(100));
        row.put("dbl", objectToValueType(123.46d));
        row.put("new_col", objectToValueType(true));
        Operation upsert =
                Operation.newBuilder()
                        .setRecord(createRecord(SCHEMA_NAME, table.getName(), OpType.UPSERT, row))
                        .build();
        connectorOutput.enqueueOperation(upsert);

        List<Column> expectedColumns =
                Arrays.asList(
                        Column.newBuilder().setName("id1").setType(DataType.INT).setPrimaryKey(true).build(),
                        Column.newBuilder().setName("dbl").setType(DataType.DOUBLE).setPrimaryKey(false).build(),
                        Column.newBuilder().setName("new_col").setType(DataType.BOOLEAN).setPrimaryKey(false).build());
        verify(destination).addColumn(schemaTable, "new_col", DataType.BOOLEAN);
        verify(destination).upsert(schemaTable, expectedColumns, row);
    }
}
