package destination;

import fivetran_sdk.v2.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.*;

public class DestinationServiceImpl extends DestinationConnectorGrpc.DestinationConnectorImplBase {

    private static final Logger logger = Logger.getLogger(DestinationServiceImpl.class.getName());

    static{
        configureLogging();
    }

    private static void configureLogging() {
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                rootLogger.removeHandler(handler);
            }
        }
        ConsoleHandler stdoutHandler = new ConsoleHandler();
        stdoutHandler.setLevel(Level.ALL);
        stdoutHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String level = record.getLevel().getName();
                String message = record.getMessage();
                return String.format("{\"level\":\"%s\", \"message\": \"%s\", \"message-origin\": \"sdk_destination\"}%n",
                        level, message);
            }
        });

        stdoutHandler.setFilter(record -> {
            Level level = record.getLevel();
            return level == Level.INFO || level == Level.WARNING || level == Level.SEVERE;
        });

        rootLogger.addHandler(stdoutHandler);
        rootLogger.setLevel(Level.ALL);
    }

    @Override
    public void configurationForm(ConfigurationFormRequest request, StreamObserver<ConfigurationFormResponse> responseObserver) {
        logger.info("Fetching configuration form");
        responseObserver.onNext(getConfigurationForm());

        responseObserver.onCompleted();
    }

    private ConfigurationFormResponse getConfigurationForm() {

        FormField writerType = FormField.newBuilder()
                .setName("writerType")
                .setLabel("Writer Type")
                .setDescription("Choose the destination type")
                .setDropdownField(
                        DropdownField.newBuilder()
                                .addAllDropdownField(Arrays.asList("Database", "File", "Cloud"))
                                .build())
                .setDefaultValue("Database")
                .build();

        FormField host = FormField.newBuilder()
                .setName("host")
                .setLabel("Host")
                .setTextField(TextField.PlainText)
                .setPlaceholder("your_host_details")
                .build();

        FormField port = FormField.newBuilder()
                .setName("port")
                .setLabel("Port")
                .setTextField(TextField.PlainText)
                .setPlaceholder("your_port_details")
                .build();

        FormField user = FormField.newBuilder()
                .setName("user")
                .setLabel("User")
                .setTextField(TextField.PlainText)
                .setPlaceholder("user_name")
                .build();

        FormField password = FormField.newBuilder()
                .setName("password")
                .setLabel("password")
                .setTextField(TextField.Password)
                .setPlaceholder("your_password")
                .build();

        FormField database = FormField.newBuilder()
                .setName("database")
                .setLabel("Database")
                .setTextField(TextField.PlainText)
                .setPlaceholder("your_database_name")
                .build();

        FormField table = FormField.newBuilder()
                .setName("table")
                .setLabel("Table")
                .setTextField(TextField.PlainText)
                .setPlaceholder("your_table_name")
                .build();

        FormField filePath = FormField.newBuilder()
                .setName("filePath")
                .setLabel("File Path")
                .setTextField(TextField.PlainText)
                .setPlaceholder("your_file_path")
                .build();

        FormField region = FormField.newBuilder()
                .setName("region")
                .setLabel("Cloud Region")
                .setDescription("Choose the cloud region")
                .setDropdownField(
                        DropdownField.newBuilder()
                                .addAllDropdownField(Arrays.asList("Azure", "AWS", "Google Cloud"))
                                .build())
                .setDefaultValue("Azure")
                .build();

        FormField enableEncryption = FormField.newBuilder()
                .setName("enableEncryption")
                .setDescription("To enable/disable encryption for data transfer")
                .setLabel("Enable Encryption?")
                .setToggleField(ToggleField.newBuilder().build())
                .build();

        // List of Visibility Conditions
        VisibilityCondition visibilityConditionForCloud = VisibilityCondition.newBuilder()
                .setConditionField("writerType")
                .setStringValue("Cloud")
                .build();

        VisibilityCondition visibilityConditionForDatabase = VisibilityCondition.newBuilder()
                .setConditionField("writerType")
                .setStringValue("Database")
                .build();

        VisibilityCondition visibilityConditionForFile = VisibilityCondition.newBuilder()
                .setConditionField("writerType")
                .setStringValue("File")
                .build();

        // List of conditional fields
        // Note: The 'name' and 'label' parameters in the FormField for conditional fields are not used.
        FormField conditionalFieldForCloud = FormField.newBuilder()
                .setName("conditionalFieldForCloud")
                .setLabel("Conditional Field for Cloud")
                .setConditionalFields(
                        ConditionalFields.newBuilder()
                                .setCondition(visibilityConditionForCloud)
                                .addAllFields(Arrays.asList(host, port, user, password, region))
                                .build())
                .build();

        FormField conditionalFieldForFile = FormField.newBuilder()
                .setName("conditionalFieldForFile")
                .setLabel("Conditional Field for File")
                .setConditionalFields(
                        ConditionalFields.newBuilder()
                                .setCondition(visibilityConditionForFile)
                                .addAllFields(Arrays.asList(host, port, user, password, table, filePath))
                                .build())
                .build();

        FormField conditionalFieldForDatabase = FormField.newBuilder()
                .setName("conditionalFieldForDatabase")
                .setLabel("Conditional Field for Database")
                .setConditionalFields(
                        ConditionalFields.newBuilder()
                                .setCondition(visibilityConditionForDatabase)
                                .addAllFields(Arrays.asList(host, port, user, password, database,  table))
                                .build())
                .build();

        return ConfigurationFormResponse.newBuilder()
                .setSchemaSelectionSupported(true)
                .setTableSelectionSupported(true)
                .addAllFields(
                        Arrays.asList(
                                writerType,
                                conditionalFieldForFile,
                                conditionalFieldForCloud,
                                conditionalFieldForDatabase,
                                enableEncryption))
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
        logger.info(message);

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
        logger.severe("Sample Severe log: Completed describe Table method");
        responseObserver.onCompleted();
    }

    @Override
    public void createTable(CreateTableRequest request, StreamObserver<CreateTableResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();

        String message = "[CreateTable]: "
                + request.getSchemaName() + " | " + request.getTable().getName() + " | " + request.getTable().getColumnsList();
        logger.info(message);
        responseObserver.onNext(CreateTableResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void alterTable(AlterTableRequest request, StreamObserver<AlterTableResponse> responseObserver) {
        Map<String, String> configuration = request.getConfigurationMap();

        String message = "[AlterTable]: " +
                request.getSchemaName() + " | " + request.getTable().getName() + " | " + request.getTable().getColumnsList();
        logger.info(message);
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
        logger.warning(String.format("Sample severe message: %s", message));
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
}
