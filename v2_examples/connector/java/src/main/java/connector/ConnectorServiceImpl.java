package connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fivetran_sdk.v2.*;
import fivetran_sdk.v2.Record;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class ConnectorServiceImpl extends SourceConnectorGrpc.SourceConnectorImplBase {
    private final String INFO = "INFO";
    private final String WARNING = "WARNING";
    private final String SEVERE = "SEVERE";
    @Override
    public void configurationForm(ConfigurationFormRequest request, StreamObserver<ConfigurationFormResponse> responseObserver) {
        logMessage(INFO, "Started fetching configuration form");
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
                                        .setDescription("Is this public?")
                                        .setToggleField(ToggleField.newBuilder()
                                                .build())
                                        .build()
                        ))
                        .addAllTests(Arrays.asList(
                                ConfigurationTest.newBuilder().setName("connect").setLabel("Tests connection").build(),
                                ConfigurationTest.newBuilder().setName("select").setLabel("Tests selection").build()))
                        .build());

        logMessage(INFO, "Fetching configuration form completed");
        responseObserver.onCompleted();
    }

    @Override
    public void test(TestRequest request, StreamObserver<TestResponse> responseObserver) {

        Map<String, String> configuration = request.getConfigurationMap();

        // Name of the test to be run
        String testName = request.getName();
        String message = String.format("test name: %s", testName);
        logMessage(INFO, message);

        responseObserver.onNext(TestResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void schema(SchemaRequest request, StreamObserver<SchemaResponse> responseObserver) {

        logMessage(WARNING, "Sample warning message while fetching schema");
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
            logMessage(WARNING, "Sync STARTING");

            // -- Send UPSERT records
            Record.Builder recordBuilder = Record.newBuilder();
            Map<String, ValueType> row = new HashMap<>();
            for (int i=0; i<3; i++) {
                responseBuilder.clear();
                recordBuilder.clear();

                row.clear();
                row.put("a1", ValueType.newBuilder().setString("a-" + i).build());
                row.put("a2", ValueType.newBuilder().setDouble(i * 0.234d).build());

                responseObserver.onNext(responseBuilder
                        .setRecord(recordBuilder
                                        .setTableName("table1")
                                        .setType(RecordType.UPSERT)
                                        .putAllData(row)
                                        .build())
                                .build());

                state.cursor += 1;
            }

            // -- Send UPDATE record
            responseBuilder.clear();
            recordBuilder.clear();
            row.clear();
            row.put("a1", ValueType.newBuilder().setString("a-0").build());
            row.put("a2", ValueType.newBuilder().setDouble(110.234d).build());
            responseObserver.onNext(responseBuilder
                    .setRecord(recordBuilder
                                    .setTableName("table1")
                                    .setType(RecordType.UPDATE)
                                    .putAllData(row)
                                    .build())
                            .build());
            state.cursor += 1;

            // -- Send DELETE record
            responseBuilder.clear();
            recordBuilder.clear();
            row.clear();
            row.put("a1", ValueType.newBuilder().setString("a-2").build());
            responseObserver.onNext(responseBuilder
                    .setRecord(recordBuilder
                                    .setTableName("table1")
                                    .setType(RecordType.DELETE)
                                    .putAllData(row)
                                    .build())
                            .build());
            state.cursor += 1;

            // -- Send checkpoint
            String newState = mapper.writeValueAsString(state);
            Checkpoint checkpoint = Checkpoint.newBuilder().setStateJson(newState).build();
            responseObserver.onNext(responseBuilder.setCheckpoint(checkpoint).build());

            // -- Send a log message
            logMessage(WARNING, "Sync DONE");
        } catch (JsonProcessingException e) {
            String message = e.getMessage();
            logMessage(SEVERE, message);
            responseObserver.onError(e);
        }

        // End the streaming RPC call
        responseObserver.onCompleted();
    }

    private void logMessage(String level, String message) {
        System.out.println(String.format("{\"level\":\"%s\", \"message\": \"%s\", \"message-origin\": \"sdk_connector\"}", level, message));
    }
}
