package connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fivetran_sdk.*;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class ConnectorServiceImpl extends ConnectorGrpc.ConnectorImplBase {
    @Override
    public void configurationForm(ConfigurationFormRequest request, StreamObserver<ConfigurationFormResponse> responseObserver) {
        responseObserver.onNext(
                ConfigurationFormResponse.newBuilder()
                        .setSchemaSelectionSupported(true)
                        .setTableSelectionSupported(true)
                        .addAllFields(Arrays.asList(
                                FormField.newBuilder()
                                        .setName("apikey").setLabel("API key").setRequired(true).setTextField(TextField.PlainText).build(),
                                FormField.newBuilder()
                                        .setName("password").setLabel("User Password").setRequired(true).setTextField(TextField.Password).build(),
                                FormField.newBuilder()
                                        .setName("region").setLabel("AWS Region").setRequired(false).setDropdownField(
                                                DropdownField.newBuilder().addAllDropdownField(
                                                        Arrays.asList("US-EAST", "US-WEST")).build()
                                        ).build(),
                                FormField.newBuilder()
                                        .setName("hidden").setLabel("my-hidden-value").setTextField(TextField.Hidden)
                                        .build(),
                                FormField.newBuilder()
                                        .setName("isPublic")
                                        .setLabel("Public?")
                                        .setToggleField(ToggleField.newBuilder()
                                                .setDescription("Is this public?")
                                                .build())
                                        .build()
                        ))
                        .addAllTests(Arrays.asList(
                                ConnectorTest.newBuilder().setName("connect").setLabel("Tests connection").build(),
                                ConnectorTest.newBuilder().setName("select").setLabel("Tests selection").build()))
                        .build());

        responseObserver.onCompleted();
    }

    @Override
    public void test(TestRequest request, StreamObserver<TestResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();

        // Name of the test to be run
        String testName = request.getName();
        System.out.println("test name: " + testName);

        responseObserver.onNext(TestResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void schema(SchemaRequest request, StreamObserver<SchemaResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();

        TableList tableList = TableList.newBuilder()
                .addAllTables(Arrays.asList(
                        Table.newBuilder().setName("table1").addAllColumns(
                                Arrays.asList(
                                        Column.newBuilder().setName("a1").setType(DataType.UNSPECIFIED).setPrimaryKey(true).build(),
                                        Column.newBuilder().setName("a2").setType(DataType.DOUBLE).build())
                        ).build(),
                        Table.newBuilder().setName("table2").addAllColumns(
                                Arrays.asList(
                                        Column.newBuilder().setName("b1").setType(DataType.STRING).setPrimaryKey(true).build(),
                                        Column.newBuilder().setName("b2").setType(DataType.UNSPECIFIED).build())
                        ).build())
                ).build();

        responseObserver.onNext(SchemaResponse.newBuilder().setWithoutSchema(tableList).build());
        responseObserver.onCompleted();
    }

    @Override
    public void update(UpdateRequest request, StreamObserver<UpdateResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();
        String state_json = request.hasStateJson() ? request.getStateJson() : "{}";
        Selection selection = request.hasSelection() ? request.getSelection() : null;

        ObjectMapper mapper = new ObjectMapper();
        UpdateResponse.Builder responseBuilder = UpdateResponse.newBuilder();

        try {
            State state = mapper.readValue(state_json, State.class);

            // -- Send a log message
            responseBuilder.clear();
            responseObserver.onNext(responseBuilder
                    .setLogEntry(LogEntry.newBuilder()
                            .setLevel(LogLevel.INFO)
                            .setMessage("Sync STARTING")
                            .build())
                    .build());

            // -- Send UPSERT records
            Operation.Builder operationBuilder = Operation.newBuilder();
            Record.Builder recordBuilder = Record.newBuilder();
            Map<String, ValueType> row = new HashMap<>();
            for (int i=0; i<3; i++) {
                responseBuilder.clear();
                operationBuilder.clear();
                recordBuilder.clear();

                row.clear();
                row.put("a1", ValueType.newBuilder().setString("a-" + i).build());
                row.put("a2", ValueType.newBuilder().setDouble(i * 0.234d).build());

                responseObserver.onNext(responseBuilder
                        .setOperation(operationBuilder
                                .setRecord(recordBuilder
                                        .setTableName("table1")
                                        .setType(OpType.UPSERT)
                                        .putAllData(row)
                                        .build())
                                .build())
                        .build());

                state.cursor += 1;
            }

            // -- Send UPDATE record
            responseBuilder.clear();
            operationBuilder.clear();
            recordBuilder.clear();
            row.clear();
            row.put("a1", ValueType.newBuilder().setString("a-0").build());
            row.put("a2", ValueType.newBuilder().setDouble(110.234d).build());
            responseObserver.onNext(responseBuilder
                    .setOperation(operationBuilder
                            .setRecord(recordBuilder
                                    .setTableName("table1")
                                    .setType(OpType.UPDATE)
                                    .putAllData(row)
                                    .build())
                            .build())
                    .build());
            state.cursor += 1;

            // -- Send DELETE record
            responseBuilder.clear();
            operationBuilder.clear();
            recordBuilder.clear();
            row.clear();
            row.put("a1", ValueType.newBuilder().setString("a-2").build());
            responseObserver.onNext(responseBuilder
                    .setOperation(operationBuilder
                            .setRecord(recordBuilder
                                    .setTableName("table1")
                                    .setType(OpType.DELETE)
                                    .putAllData(row)
                                    .build())
                            .build())
                    .build());
            state.cursor += 1;

            // -- Send checkpoint
            String newState = mapper.writeValueAsString(state);
            Checkpoint checkpoint = Checkpoint.newBuilder().setStateJson(newState).build();
            operationBuilder.clear();
            responseObserver.onNext(responseBuilder
                    .setOperation(operationBuilder
                            .setCheckpoint(checkpoint).build()).build());

            // -- Send a log message
            responseBuilder.clear();
            responseObserver.onNext(responseBuilder
                    .setLogEntry(LogEntry.newBuilder()
                            .setLevel(LogLevel.INFO)
                            .setMessage("Sync DONE")
                            .build())
                    .build());
        } catch (JsonProcessingException e) {
            responseObserver.onError(e);
        }

        // End the streaming RPC call
        responseObserver.onCompleted();
    }
}
