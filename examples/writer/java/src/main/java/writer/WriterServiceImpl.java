package writer;

import fivetran_sdk.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
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
                                        .setDescription("Is this public?")
                                        .setToggleField(ToggleField.newBuilder().build())
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
    public void describeTable(DescribeTableRequest request, StreamObserver<DescribeTableResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();

        DescribeTableResponse response = DescribeTableResponse.newBuilder()
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
    public void createTable(CreateTableRequest request, StreamObserver<CreateTableResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();

        System.out.println("[CreateTable]: " + request.getSchemaName() + " | " + request.getTable().getName());
        responseObserver.onNext(CreateTableResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void alterTable(AlterTableRequest request, StreamObserver<AlterTableResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();

        System.out.println("[AlterTable]: " + request.getSchemaName() + " | " + request.getTable().getName());
        responseObserver.onNext(AlterTableResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void truncate(TruncateRequest request, StreamObserver<TruncateResponse> responseObserver) {
        System.out.println("[TruncateTable]: " + request.getSchemaName() + " | " + request.getTableName());
        responseObserver.onNext(TruncateResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void writeBatch(WriteBatchRequest request, StreamObserver<WriteBatchResponse> responseObserver) {
        System.out.println("[WriteBatch]: " + request.getSchemaName() + " | " + request.getTable().getName());
        for (String file : request.getReplaceList()) {
            System.out.println("Replace files: " + file);
        }
        for (String file : request.getUpdateList()) {
            System.out.println("Update files: " + file);
        }
        for (String file : request.getDeleteList()) {
            System.out.println("Delete files: " + file);
        }
        responseObserver.onNext(WriteBatchResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }
}
