package connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fivetran_sdk.*;
import fivetran_sdk.Record;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class ConnectorServiceImpl extends ConnectorGrpc.ConnectorImplBase {
    private final String INFO = "INFO";
    private final String WARNING = "WARNING";
    private final String SEVERE = "SEVERE";
    @Override
    public void configurationForm(ConfigurationFormRequest request, StreamObserver<ConfigurationFormResponse> responseObserver) {
        logMessage(INFO, "Started fetching configuration form");
        responseObserver.onNext(
                ConfigurationFormResponse.newBuilder()
                        .addFields(FormField.newBuilder()
                                .setName("url")
                                .setLabel("Deployment URL")
                                .setRequired(true)
                                .setTextField(TextField.PlainText))
                        .addFields(FormField.newBuilder()
                                .setName("key")
                                .setLabel("Deploy Key")
                                .setRequired(true)
                                .setTextField(TextField.Password))
                        .addTests(ConfigurationTest.newBuilder()
                                .setName("select")
                                .setLabel("Tests selection"))
                        .build());

        logMessage(INFO, "Fetching configuration form completed");
        responseObserver.onCompleted();
    }

    @Override
    public void schema(SchemaRequest request, StreamObserver<SchemaResponse> responseObserver) {

        logMessage(WARNING, "Sample warning message while fetching schema");
        Map<String, String> configuration = request.getConfigurationMap();

        responseObserver.onNext(SchemaResponse.newBuilder()
                .setWithoutSchema(TableList.newBuilder()
                        .addTables(Table.newBuilder()
                                .setName("test_table_1")
                                .addColumns(Column.newBuilder()
                                        .setName("pk_column_string")
                                        .setType(DataType.STRING)
                                        .setPrimaryKey(true)
                                        .build())
                                .addColumns(Column.newBuilder()
                                        .setName("column_1_int")
                                        .setType(DataType.INT)
                                        .build())
                                .build())
                        .build())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void update(UpdateRequest request, StreamObserver<UpdateResponse> responseObserver) {
        Map<String, ValueType> row1 = Map.of(
                "pk_column_string",
                ValueType.newBuilder().setString("1").build(),
                "column_1_int", ValueType.newBuilder().setInt(1).build());
        Map<String, ValueType> row2 = Map.of(
                "pk_column_string",
                ValueType.newBuilder().setString("2").build(),
                "column_1_int", ValueType.newBuilder().setInt(2).build());
        UpdateResponse response1 = UpdateResponse.newBuilder()
                .setOperation(Operation.newBuilder()
                        .setRecord(Record.newBuilder()
                                .setTableName("test_table_1")
                                .setType(OpType.UPSERT)
                                .putAllData(row1)))
                .build();
        UpdateResponse response2 = UpdateResponse.newBuilder()
                .setOperation(Operation.newBuilder()
                        .setRecord(Record.newBuilder()
                                .setTableName("test_table_1")
                                .setType(OpType.UPSERT)
                                .putAllData(row2)))
                .build();
        responseObserver.onNext(response1);
        responseObserver.onNext(response2);
        responseObserver.onCompleted();
    }

    public static void logMessage(String level, String message) {
        System.out.println(String.format("{\"level\":\"%s\", \"message\": \"%s\", \"message-origin\": \"sdk_connector\"}", level, message));
    }
}
