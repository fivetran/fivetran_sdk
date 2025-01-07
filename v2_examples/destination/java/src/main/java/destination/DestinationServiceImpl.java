package destination;

import fivetran_sdk.v2.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.Map;

public class DestinationServiceImpl extends DestinationConnectorGrpc.DestinationConnectorImplBase {

    private final String INFO = "INFO";
    private final String WARNING = "WARNING";
    private final String SEVERE = "SEVERE";

    @Override
    public void configurationForm(ConfigurationFormRequest request, StreamObserver<ConfigurationFormResponse> responseObserver) {
        logMessage(INFO, "Fetching configuration form");
        responseObserver.onNext(getConfigurationForm());

        responseObserver.onCompleted();
    }

    private ConfigurationFormResponse getConfigurationForm(){
        FormField apiBaseURL = FormField.newBuilder()
                .setName("apiBaseURL")
                .setLabel("API base URL")
                .setDescription("Enter the base URL for the API you're connecting to")
                .setRequired(true)
                .setTextField(TextField.PlainText)
                .setPlaceholder("api_base_url")
                .build();

        FormField authenticationMethods = FormField.newBuilder()
                .setName("authenticationMethod")
                .setLabel("Authentication Method")
                .setDescription("Choose the preferred authentication method to securely access the API")
                .setDropdownField(
                        DropdownField.newBuilder()
                                .addAllDropdownField(Arrays.asList("OAuth2.0", "API Key", "Basic Auth", "None"))
                                .build())
                .setDefaultValue("None")
                .build();

        FormField apiKey = FormField.newBuilder()
                .setName("apiKey")
                .setLabel("Api Key")
                .setTextField(TextField.Password)
                .setPlaceholder("your_api_key_here")
                .build();

        FormField clientId = FormField.newBuilder()
                .setName("clientId")
                .setLabel("Client Id")
                .setTextField(TextField.Password)
                .setPlaceholder("your_client_id_here")
                .build();

        FormField clientSecret = FormField.newBuilder()
                .setName("clientSecret")
                .setLabel("Client Secret")
                .setTextField(TextField.Password)
                .setPlaceholder("your_client_secret_here")
                .build();

        FormField userName = FormField.newBuilder()
                .setName("username")
                .setLabel("Username")
                .setTextField(TextField.PlainText)
                .setPlaceholder("your_username_here")
                .build();

        FormField password = FormField.newBuilder()
                .setName("password")
                .setLabel("Password")
                .setTextField(TextField.Password)
                .setPlaceholder("your_password_here")
                .build();

        FormField apiVersions = FormField.newBuilder()
                .setName("apiVersion")
                .setLabel("Api Version")
                .setDropdownField(
                        DropdownField.newBuilder().addAllDropdownField(Arrays.asList("v1","v2","v3")).build())
                .setDefaultValue("v2")
                .build();

        FormField addMetrics = FormField.newBuilder()
                .setName("shouldAddMetrics")
                .setLabel("Enable Metrics?")
                .setToggleField(ToggleField.newBuilder().build())
                .build();


        // Conditional Field for OAuth
        VisibilityCondition visibilityCondition1 = VisibilityCondition.newBuilder()
                .setConditionField("authenticationMethod")
                .setStringValue("OAuth2.0")
                .build();

        FormField conditionalField1 = FormField.newBuilder()
                .setName("doesNotMatter")
                .setLabel("It won't be used")
                .setConditionalFields(
                        ConditionalFields.newBuilder()
                                .setCondition(visibilityCondition1)
                                .addAllFields(Arrays.asList(clientId, clientSecret))
                                .build())
                .build();

        // Conditional Field for API Key authentication method
        VisibilityCondition visibilityCondition2 = VisibilityCondition.newBuilder()
                .setConditionField("authenticationMethod")
                .setStringValue("API Key")
                .build();

        FormField conditionalField2 = FormField.newBuilder()
                .setName("doesNotMatter")
                .setLabel("It won't be used")
                .setConditionalFields(
                        ConditionalFields.newBuilder()
                                .setCondition(visibilityCondition2)
                                .addAllFields(Arrays.asList(apiKey))
                                .build())
                .build();

        // Conditional Field for Basic Auth
        VisibilityCondition visibilityCondition3 = VisibilityCondition.newBuilder()
                .setConditionField("authenticationMethod")
                .setStringValue("Basic Auth")
                .build();

        FormField conditionalField3 = FormField.newBuilder()
                .setName("doesNotMatter")
                .setLabel("It won't be used")
                .setConditionalFields(
                        ConditionalFields.newBuilder()
                                .setCondition(visibilityCondition3)
                                .addAllFields(Arrays.asList(userName, password))
                                .build())
                .build();

        return ConfigurationFormResponse.newBuilder()
                .setSchemaSelectionSupported(true)
                .setTableSelectionSupported(true)
                .addAllFields(
                        Arrays.asList(
                                apiBaseURL,
                                authenticationMethods,
                                conditionalField1,
                                conditionalField2,
                                conditionalField3,
                                apiVersions,
                                addMetrics))
                .addAllTests(
                        Arrays.asList(
                                ConfigurationTest.newBuilder().setName("connect").setLabel("Tests connection").build(),
                                ConfigurationTest.newBuilder().setName("select").setLabel("Tests selection").build()))
                .build();
    }

    @Override
    public void test(TestRequest request, StreamObserver<TestResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();
        String testName = request.getName();
        String message = String.format("Test Name: %s", testName);
        logMessage(INFO, message);

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
        logMessage(SEVERE, "Sample Severe log: Completed describe Table method");
        responseObserver.onCompleted();
    }

    @Override
    public void createTable(CreateTableRequest request, StreamObserver<CreateTableResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();

        String message = "[CreateTable]: "
                + request.getSchemaName() + " | " + request.getTable().getName() + " | " + request.getTable().getColumnsList();
        logMessage(INFO, message);
        responseObserver.onNext(CreateTableResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void alterTable(AlterTableRequest request, StreamObserver<AlterTableResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();

        String message = "[AlterTable]: " +
                request.getSchemaName() + " | " + request.getTable().getName() + " | " + request.getTable().getColumnsList();
        logMessage(INFO, message);
        responseObserver.onNext(AlterTableResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void truncate(TruncateRequest request, StreamObserver<TruncateResponse> responseObserver) {
        System.out.printf("[TruncateTable]: %s | %s | soft=%s%n",
                request.getSchemaName(), request.getTableName(), request.hasSoft());
        responseObserver.onNext(TruncateResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void writeBatch(WriteBatchRequest request, StreamObserver<WriteBatchResponse> responseObserver) {
        String message = "[WriteBatch]: " + request.getSchemaName() + " | " + request.getTable().getName();
        logMessage(WARNING, String.format("Sample severe message: %s", message));
        for (String file : request.getReplaceFilesList()) {
            System.out.println("Replace files: " + file);
        }
        for (String file : request.getUpdateFilesList()) {
            System.out.println("Update files: " + file);
        }
        for (String file : request.getDeleteFilesList()) {
            System.out.println("Delete files: " + file);
        }
        responseObserver.onNext(WriteBatchResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    private void logMessage(String level, String message){
        System.out.println(String.format("{\"level\":\"%s\", \"message\": \"%s\", \"message-origin\": \"sdk_destination\"}", level, message));
    }
}
