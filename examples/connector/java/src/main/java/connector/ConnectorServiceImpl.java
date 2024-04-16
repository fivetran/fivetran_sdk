package connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fivetran_sdk.*;
import fivetran_sdk.Record;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class ConnectorServiceImpl extends SourceConnectorGrpc.SourceConnectorImplBase {
    @Override
    public void configurationForm(ConfigurationFormRequest request, StreamObserver<ConfigurationFormResponse> responseObserver) {
        responseObserver.onNext(
                ConfigurationFormResponse.newBuilder()
                        .setSchemaSelectionSupported(true)
                        .setTableSelectionSupported(true)
                        .addAllFields(Arrays.asList(
                                FormField.newBuilder()
                                        .setSingle(Field.newBuilder().setName("apiKey").setLabel("API Key").setPlaceholder("my-api-key")
                                                .setRequired(true).setTextField(TextField.PlainText).build())
                                        .build(),
                                FormField.newBuilder()
                                        .setSingle(Field.newBuilder().setName("password").setLabel("User Password").setPlaceholder("p4ssw0rd")
                                                .setRequired(true).setTextField(TextField.Password).build())
                                        .build(),
                                FormField.newBuilder()
                                        .setSingle(Field.newBuilder().setName("region").setLabel("AWS Region").setDefaultValue("US-EAST").setRequired(false)
                                                .setDropdownField(DropdownField.newBuilder().addAllDropdownField(Arrays.asList("US-EAST", "US-WEST")).build())
                                                .build())
                                        .build(),
                                FormField.newBuilder()
                                        .setSingle(Field.newBuilder().setName("hidden").setLabel("my-hidden-value").setTextField(TextField.Hidden).build())
                                        .build(),
                                FormField.newBuilder()
                                        .setSingle(Field.newBuilder().setName("isPublic").setLabel("Public?").setDescription("Is this public?")
                                                .setToggleField(ToggleField.newBuilder().build())
                                                .build())
                                        .build()
                        ))
                        .addAllTests(Arrays.asList(
                                ConfigurationTest.newBuilder().setName("connect").setLabel("Tests connection").build(),
                                ConfigurationTest.newBuilder().setName("select").setLabel("Tests selection").build()))
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
        String stateJson = request.hasStateJson() ? request.getStateJson() : "{}";
        Selection selection = request.hasSelection() ? request.getSelection() : null;

        ObjectMapper mapper = new ObjectMapper();
        UpdateResponse.Builder responseBuilder = UpdateResponse.newBuilder();

        try {
            State state = mapper.readValue(stateJson, State.class);

            // -- Send a log message
            System.out.println("[Update]: Sync STARTING");

            // -- Send UPSERT records
            Record.Builder recordBuilder = Record.newBuilder();
            Map<String, ValueType> row = new HashMap<>();
            for (int i=0; i<3; i++) {
                responseBuilder.clear();
                recordBuilder.clear();

                row.clear();
                row.put("a1", ValueType.newBuilder().setString("a-" + i).build());
                row.put("a2", ValueType.newBuilder().setDouble(i * 0.234d).build());

                responseObserver.onNext(responseBuilder.setRecord(recordBuilder
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
            responseObserver.onNext(responseBuilder.setRecord(recordBuilder
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
            responseObserver.onNext(responseBuilder.setRecord(recordBuilder
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
            System.out.println("[Update]: Sync DONE");
        } catch (JsonProcessingException e) {
            responseObserver.onError(e);
        }

        // End the streaming RPC call
        responseObserver.onCompleted();
    }
}
