package connector;

import fivetran_sdk.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WriterServiceImpl extends WriterGrpc.WriterImplBase {
    @Override
    public void configurationForm(ConfigurationFormRequest request, StreamObserver<ConfigurationFormResponse> responseObserver) {
        responseObserver.onNext(
                ConfigurationFormResponse.newBuilder()
                        .setSchemaSelectionSupported(true)
                        .setTableSelectionSupported(true)
                        .addAllFields(Arrays.asList(
                                FormField.newBuilder()
                                        .setName("host").setLabel("Host").setRequired(true).setTextField(TextField.PlainText).build(),
                                FormField.newBuilder()
                                        .setName("password").setLabel("Password").setRequired(true).setTextField(TextField.Password).build(),
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
                                ConfigurationTest.newBuilder().setName("connect").setLabel("Tests connection").build(),
                                ConfigurationTest.newBuilder().setName("select").setLabel("Tests selection").build()))
                        .build());

        responseObserver.onCompleted();
    }

    @Override
    public void test(TestRequest request, StreamObserver<TestResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();
        String testName = request.getName();
        System.out.println("test name: " + testName);

        responseObserver.onNext(TestResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getTable(TableRequest request, StreamObserver<TableResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();

        TableResponse response = TableResponse.newBuilder()
                .setTable(
                        Table.newBuilder()
                                .setName(request.getTableName())
                                .addAllColumns(
                                Arrays.asList(
                                        Column.newBuilder().setName("a1").setType(DataType.UNSPECIFIED).setPrimaryKey(true).build(),
                                        Column.newBuilder().setName("a2").setType(DataType.DOUBLE).build())
                        ).build()).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void assertTable(AssertTableRequest request, StreamObserver<AssertTableResponse> responseObserver) {
        System.out.println("[AssertTable]: " + request.getSchemaName() + " | " + request.getTable().getName());
        responseObserver.onNext(AssertTableResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void fetchMetadata(MetadataRequest request, StreamObserver<MetadataResponse> responseObserver) {
        System.out.println("[FetchMetadata]");

        Map<String, DataType> types = new HashMap<>();
        types.put("int", DataType.INT);
        types.put("varchar", DataType.STRING);
        types.put("numeric", DataType.DECIMAL);

        responseObserver.onNext(MetadataResponse.newBuilder()
                        .setMaxIdentifierLength(1_000_000)
                        .setMaxIsoTimestamp("9990-12-31T23:59:59.999Z")
                        .putAllFivetranTypeMap(types)
                        .build());
        responseObserver.onCompleted();
    }

    @Override
    public void truncateTable(TruncateTableRequest request, StreamObserver<TruncateTableResponse> responseObserver) {
        System.out.println("[TruncateTable]: " + request.getSchemaName() + " | " + request.getTableName());
        responseObserver.onNext(TruncateTableResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<WriteBatchRequest> writeBatch(StreamObserver<WriteBatchResponse> responseObserver) {
        return new StreamObserver<WriteBatchRequest>() {
            private StartBatch startBatch = null;

            @Override
            public void onNext(WriteBatchRequest value) {
                // Accumulate incoming records

                WriteBatchRequest.RequestCase requestCase = value.getRequestCase();
                switch (requestCase) {
                    case INIT:
                        if (startBatch != null) {
                            throw new RuntimeException("Another batch is already in progress");
                        }
                        startBatch = value.getInit();
                        System.out.println("START_BATCH: " + startBatch.getTableName() + " | " + startBatch.getFlushId());
                        break;

                    case UPSERT:
                        Upsert upsert = value.getUpsert();
                        System.out.println("UPSERT: ["
                                + startBatch.getTableName() + "] "
                                + upsert.getRecordMap().keySet());
                        break;

                    case UPDATE:
                        Update update = value.getUpdate();
                        System.out.println("UPDATE: ["
                                + startBatch.getTableName() + "] "
                                + update.getRecordMap().keySet());
                        break;

                    case DELETE:
                        Delete delete = value.getDelete();
                        System.out.println("DELETE: ["
                                + startBatch.getTableName() + "] "
                                + delete.getRecordMap().keySet());
                        break;

                    default:
                        throw new RuntimeException("Unrecognized case: " + requestCase);
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Something went wrong: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("MERGE: " + startBatch.getTableName());

                // Do something with the records that you accumulated

                startBatch = null;

                WriteBatchResponse response = WriteBatchResponse.newBuilder()
                        .setSuccess(true)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }
}
