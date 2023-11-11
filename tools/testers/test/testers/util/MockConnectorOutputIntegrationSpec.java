package testers.util;

import static testers.util.SdkConverters.objectToValueType;
import static testers.util.MockConnectorOutputSpec.createRecord;
import static testers.util.MockWarehouseSpec.generateRandomName;
import static org.junit.Assert.assertEquals;

import fivetran_sdk.Column;
import fivetran_sdk.DataType;
import fivetran_sdk.OpType;
import fivetran_sdk.Operation;
import fivetran_sdk.Table;
import fivetran_sdk.ValueType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MockConnectorOutputIntegrationSpec {
    private static final String SCHEMA_NAME = "my_schema";
    private static final String TABLE_NAME_PREFIX = "my_table";

    private static Path tmpDbDir = null;

    @BeforeClass
    public static void setup() throws IOException {
        tmpDbDir = Files.createTempDirectory("test-db-");
    }

    @AfterClass
    public static void teardown() throws IOException {
        FileUtils.deleteDirectory(tmpDbDir.toFile());
    }

    private static Table buildTable(String prefix) {
        return Table.newBuilder()
                .addAllColumns(
                        Arrays.asList(
                                Column.newBuilder().setName("id1").setType(DataType.INT).setPrimaryKey(true).build(),
                                Column.newBuilder().setName("val").setType(DataType.UNSPECIFIED).build()))
                .setName(generateRandomName(prefix))
                .build();
    }

    @Test
    public void update_existing_unspecified_column() {
        MockWarehouse warehouse = new MockWarehouse(Paths.get(tmpDbDir.toString(), "test.db"));
        MockConnectorOutput out1 = new MockConnectorOutput(warehouse, "defaultSchema", (s) -> {}, () -> "{}");
        MockConnectorOutput out2 = new MockConnectorOutput(warehouse, "defaultSchema", (s) -> {}, () -> "{}");

        Table table = buildTable(TABLE_NAME_PREFIX);
        out1.handleSchemaChange(SCHEMA_NAME, table);

        Map<String, ValueType> row = new HashMap<>();
        row.put("id1", objectToValueType(100));
        row.put("val", objectToValueType("Some string"));
        Operation upsert =
                Operation.newBuilder()
                        .setRecord(createRecord(SCHEMA_NAME, table.getName(), OpType.UPSERT, row))
                        .build();
        out1.enqueueOperation(upsert);

        out2.handleSchemaChange(SCHEMA_NAME, table);
        row.put("val", objectToValueType("Some new string"));
        Operation update =
                Operation.newBuilder()
                        .setRecord(createRecord(SCHEMA_NAME, table.getName(), OpType.UPDATE, row))
                        .build();
        out2.enqueueOperation(update);

        SchemaTable schemaTable = new SchemaTable(SCHEMA_NAME, table.getName());
        List<Map<String, ValueType>> rows = warehouse.read(schemaTable);
        assertEquals(rows.size(), 1);
        assertEquals(rows.get(0), row);
    }
}
