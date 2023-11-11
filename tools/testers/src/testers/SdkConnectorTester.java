package testers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import client.connector.SdkConnectorClient;
import testers.util.MockConnectorOutput;
import testers.util.MockWarehouse;
import com.google.protobuf.ByteString;
import fivetran_sdk.*;
import io.grpc.ManagedChannel;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

/** This tool mocks Fivetran core + data-writer + mock warehouse (duckdb) for testing SDK Connector */
public final class SdkConnectorTester {
    private static final Logger LOG = Logger.getLogger(SdkConnectorTester.class.getName());

    private static final String VERSION = "2023.1103.1640";

    static final String CONFIG_FILE = "configuration.json";
    private static final String SCHEMA_SELECTION_FILE = "schema_selection.txt";
    private static final String STATE_FILE = "state.json";
    static final String WH_FILE = "warehouse.db";
    private static final String DEFAULT_SCHEMA = "default_schema";

    private static final String SCHEMA_LABEL = "<sch>";
    private static final String TABLE_LABEL = "<tbl>";
    private static final String COLUMN_LABEL = "<col>";

    private SdkConnectorTester() {}

    @CommandLine.Command(
            name = "cliargs",
            mixinStandardHelpOptions = true,
            description = "Command line args for sync simulation")
    static class CliArgs {
        @CommandLine.Option(
                names = {"--working-dir"},
                required = true,
                description = "Directory to use for saving sync files")
        String workingDir;

        @CommandLine.Option(
                names = {"--default-schema"},
                required = true,
                defaultValue = DEFAULT_SCHEMA,
                description = "Service ID of the connector")
        String defaultSchema;

        @CommandLine.Option(
                names = {"--port"},
                defaultValue = "50051",
                required = true,
                description = "")
        String port;
    }

    public static void main(String[] args) throws IOException {
        CliArgs cliargs = new CliArgs();
        new CommandLine(cliargs).parseArgs(args);

        String grpcHost =
                (System.getenv("GRPC_HOSTNAME") == null)
                        ? SdkConnectorClient.DEFAULT_GRPC_HOST
                        : System.getenv("GRPC_HOSTNAME");

        new SdkConnectorTester()
                .run(cliargs.workingDir, cliargs.defaultSchema, grpcHost, Integer.parseInt(cliargs.port));
    }

    static final ObjectMapper JSON = create();

    private static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        // https://github.com/FasterXML/jackson-databind/issues/2643
        mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(false));

        return mapper;
    }

    private String stateLoader(Path stateFilePath) {
        try {
            if (Files.exists(stateFilePath)) {
                return Files.readString(stateFilePath);
            }
            return "{}";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stateSaver(String newStateJson, Path stateFilePath) {
        try {
            Files.write(stateFilePath, newStateJson.getBytes());
        } catch (IOException e) {
            LOG.warning("Failed to persist new state!");
        }
    }

    public static void saveConfig(ConfigurationFormResponse configurationForm, Path configFilePath) {
        try {
            if (Files.exists(configFilePath)) {
                // TODO: Handle changes in ConfigurationFormResponse
            } else {
                LOG.info("No prior configuration form found");
                Map<String, String> configuration = promptForConfiguration(configurationForm);
                String strConfig = JSON.writeValueAsString(configuration);
                Files.write(configFilePath, strConfig.getBytes());
            }
        } catch (IOException e) {
            LOG.severe(e.getMessage());
            System.exit(1);
        }
    }

    public void run(String workingDir, String defaultSchema, String grpcHost, int grpcPort) {
        LOG.info("Version: " + VERSION);
        LOG.info("Directory: " + workingDir);
        LOG.info("Default schema name: " + defaultSchema);
        LOG.info("GRPC_HOSTNAME: " + grpcHost);
        LOG.info("GRPC_PORT: " + grpcPort);

        ManagedChannel channel = SdkConnectorClient.createChannel(grpcHost, grpcPort);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> SdkConnectorClient.closeChannel(channel)));
        SdkConnectorClient client = new SdkConnectorClient(channel);

        LOG.info("Fetching configuration form");
        Path configFilePath = Paths.get(workingDir, CONFIG_FILE);
        ConfigurationFormResponse configurationForm = client.configurationForm();
        SdkConnectorTester.saveConfig(configurationForm, configFilePath);

        try (MockWarehouse destination = new MockWarehouse(Paths.get(workingDir, WH_FILE));
                MockConnectorOutput output =
                        new MockConnectorOutput(
                                destination,
                                defaultSchema,
                                newStateJson -> stateSaver(newStateJson, Paths.get(workingDir, STATE_FILE)),
                                () -> stateLoader(Paths.get(workingDir, STATE_FILE)))) {

            String strConfig = Files.readString(configFilePath);
            LOG.info("Configuration:\n" + strConfig);
            Map<String, String> creds = JSON.readValue(strConfig, new MapTypeReference());

            LOG.info("Running setup tests");
            for (ConfigurationTest connectorTest : configurationForm.getTestsList()) {
                Optional<String> testResponse = client.test(connectorTest.getName(), creds);
                String result = testResponse.orElse("PASSED");
                System.out.println("[" + connectorTest.getLabel() + "]: " + result);
                if (!testResponse.isEmpty()) {
                    LOG.severe("Exiting due to test failure!");
                    System.exit(1);
                }
            }

            String stateJson = output.getState();
            LOG.info("Previous state:\n" + stateJson);

            SchemaResponse schemaResponse = client.schema(creds, stateJson);
            Path schemaSelectionsFilePath = Paths.get(workingDir, SCHEMA_SELECTION_FILE);
            if (Files.exists(schemaSelectionsFilePath)) {
                // TODO: Handle changes in SchemaResponse
            } else {
                createSchemaFileForSelections(schemaResponse, defaultSchema, schemaSelectionsFilePath);
                LOG.info("Schema selection file is generated");

                LOG.info("\nPlease update your schema selections and press RETURN to continue\n");
                System.in.read();
            }

            LOG.info("Schema Selections:\n" + Files.readString(schemaSelectionsFilePath));
            Selection selection = readSchemaFileForSelections(schemaSelectionsFilePath, defaultSchema);
            boolean supported =
                    SdkConnectorClient.walkSchemaResponse(
                            schemaResponse,
                            (schema, tables) -> {
                                for (Table table : tables) {
                                    String schemaName = schema.orElse(defaultSchema);
                                    if (isIncluded(schemaName, table.getName(), selection)) {
                                        output.handleSchemaChange(schemaName, table);
                                    }
                                }
                            });
            if (!supported) {
                LOG.info("This connector does not support schema discovery");
            }

            client.update(creds, stateJson, selection, output::enqueueOperation, System.out::println);

            output.displayReport();
            LOG.info("Sync SUCCEEDED");

        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Sync FAILED", e);
        }

        System.exit(0);
    }

    private static boolean isIncluded(String schema, String table, Selection selection) {
        Selection.SelectionCase selectionCase = selection.getSelectionCase();
        if (selectionCase == Selection.SelectionCase.WITH_SCHEMA) {
            TablesWithSchema withSchema = selection.getWithSchema();

            for (SchemaSelection schemaSelection : withSchema.getSchemasList()) {
                if (schemaSelection.getSchemaName().equals(schema)) {
                    return isTableIncluded(table, schemaSelection.getTablesList());
                }
            }

        } else {
            TablesWithNoSchema withoutSchema = selection.getWithoutSchema();
            return isTableIncluded(table, withoutSchema.getTablesList());
        }

        return false;
    }

    private static boolean isTableIncluded(String tableName, List<TableSelection> tableSelections) {
        for (TableSelection tableSelection : tableSelections) {
            if (tableSelection.getTableName().equals(tableName)) {
                return tableSelection.getIncluded();
            }
        }

        return false;
    }

    private static void writeTables(List<Table> tables, FileWriter fw) throws IOException {
        for (Table table : tables) {
            fw.write(TABLE_LABEL + "\t\t\t[x]  " + table.getName() + "\n");

            for (Column column : table.getColumnsList()) {
                fw.write(COLUMN_LABEL + "\t\t\t\t[x]  " + column.getName() + "\n");
            }
        }
    }

    private static void createSchemaFileForSelections(SchemaResponse response, String defaultSchema, Path path)
            throws IOException {
        SchemaResponse.ResponseCase responseCase = response.getResponseCase();

        try (FileWriter fw = new FileWriter(path.toFile())) {
            switch (responseCase) {
                case WITH_SCHEMA:
                    SchemaList schemaList = response.getWithSchema();
                    for (Schema schema : schemaList.getSchemasList()) {
                        fw.write(SCHEMA_LABEL + "\t\t[x]  " + schema.getName() + "\n");
                        writeTables(schema.getTablesList(), fw);
                    }
                    break;

                case WITHOUT_SCHEMA:
                    fw.write(SCHEMA_LABEL + "\t\t[x]  " + defaultSchema + "\n");
                    TableList tableList = response.getWithoutSchema();
                    writeTables(tableList.getTablesList(), fw);
                    break;

                case SCHEMA_RESPONSE_NOT_SUPPORTED:
                    System.out.println("Schema discovery is not supported!");
                    break;

                default:
                    throw new RuntimeException("Unknown response case: " + responseCase);
            }
        }
    }

    private static Selection readSchemaFileForSelections(Path path, String defaultSchemaName) throws IOException {
        Selection.Builder selectionBuilder = Selection.newBuilder();
        SchemaSelection.Builder schemaSelectionBuilder = SchemaSelection.newBuilder();
        TableSelection.Builder tableSelectionBuilder = TableSelection.newBuilder();

        String schemaName = null;
        Boolean schemaIncluded = null;
        @Nullable String tableName = null;
        Boolean tableIncluded = null;

        Map<String, Boolean> columns = new HashMap<>();
        List<TableSelection> tables = new ArrayList<>();
        List<SchemaSelection> schemas = new ArrayList<>();

        boolean defaultSchema = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            while (true) {
                String line = reader.readLine();

                if (line == null || line.contains(SCHEMA_LABEL)) {
                    if (schemaName != null) {
                        if (tableName != null) {
                            Objects.requireNonNull(schemaName);

                            tableSelectionBuilder.clear();
                            TableSelection tableSelection =
                                    tableSelectionBuilder
                                            .setTableName(tableName)
                                            .putAllColumns(new HashMap<>(columns))
                                            .setIncluded(tableIncluded)
                                            .setIncludeNewColumns(true) // TODO: Make this selectable from file
                                            .build();
                            tables.add(tableSelection);
                            columns.clear();
                            tableName = null;
                        }

                        if (tables.isEmpty()) {
                            System.out.println(String.format("Schema `%s` has no tables", schemaName));
                        }

                        if (defaultSchema) {
                            TablesWithNoSchema tablesWithNoSchema =
                                    TablesWithNoSchema.newBuilder()
                                            .addAllTables(tables)
                                            .setIncludeNewTables(true) // TODO: Make this selectable from file
                                            .build();

                            return selectionBuilder.setWithoutSchema(tablesWithNoSchema).build();
                        }

                        schemaSelectionBuilder.clear();
                        SchemaSelection schemaSelection =
                                schemaSelectionBuilder
                                        .addAllTables(new ArrayList<>(tables))
                                        .setSchemaName(schemaName)
                                        .setIncluded(schemaIncluded)
                                        .setIncludeNewTables(true) // TODO: Make this selectable from file
                                        .build();
                        schemas.add(schemaSelection);
                        tables.clear();
                    }

                    if (line == null) {
                        break;
                    }

                    schemaName = StringUtils.substringAfter(line, "]").strip();
                    schemaIncluded = line.contains("[x]");
                    defaultSchema = schemaName.equals(defaultSchemaName);

                } else if (line.contains(TABLE_LABEL)) {
                    if (tableName != null) {
                        Objects.requireNonNull(schemaName);

                        tableSelectionBuilder.clear();
                        TableSelection tableSelection =
                                tableSelectionBuilder
                                        .setTableName(tableName)
                                        .putAllColumns(new HashMap<>(columns))
                                        .setIncluded(tableIncluded)
                                        .setIncludeNewColumns(true) // TODO: Make this selectable from file
                                        .build();
                        tables.add(tableSelection);
                        columns.clear();
                    }

                    tableName = StringUtils.substringAfter(line, "]").strip();
                    tableIncluded = line.contains("[x]");

                } else if (line.contains(COLUMN_LABEL)) {
                    String columnName = StringUtils.substringAfter(line, "]").strip();
                    columns.put(columnName, line.contains("[x]"));
                }
            }
        }

        TablesWithSchema tablesWithSchema =
                TablesWithSchema.newBuilder()
                        .addAllSchemas(schemas)
                        .setIncludeNewSchemas(true) // TODO: Make this selectable from file
                        .build();

        return selectionBuilder.setWithSchema(tablesWithSchema).build();
    }

    private static Map<String, String> promptForConfiguration(ConfigurationFormResponse configurationForm) {
        System.out.println("\nEnter configuration values");

        try (FilterInputStream inputStream =
                        new FilterInputStream(System.in) {
                            @Override
                            public void close() {
                                // Don't close System.in!
                            }
                        };
                Scanner scanner = new Scanner(inputStream)) {
            Map<String, String> config = new HashMap<>();
            for (FormField field : configurationForm.getFieldsList()) {
                System.out.println();
                String description = field.getDescription();
                switch (field.getTypeCase()) {
                    case TEXT_FIELD:
                        TextField textField = field.getTextField();
                        String strText =
                                String.format(
                                        "<%s [%s] [%s] %s>",
                                        field.getName(),
                                        textField.name(),
                                        description,
                                        field.getRequired() ? "required" : "");
                        System.out.println(strText);
                        System.out.print(field.getLabel() + ": ");
                        String inputFieldVal = scanner.nextLine();
                        config.put(field.getName(), inputFieldVal);
                        break;

                    case DROPDOWN_FIELD:
                        DropdownField dropdownField = field.getDropdownField();
                        List<String> items =
                                dropdownField
                                        .getDropdownFieldList()
                                        .asByteStringList()
                                        .stream()
                                        .map(ByteString::toStringUtf8)
                                        .collect(Collectors.toList());
                        StringJoiner itemsJoiner = new StringJoiner(",");
                        items.forEach(i -> itemsJoiner.add(i));
                        String strDropdown =
                                String.format(
                                        "<%s [%s] [%s] [%s] %s>",
                                        field.getName(),
                                        field.getTypeCase(),
                                        description,
                                        itemsJoiner,
                                        field.getRequired() ? "required" : "");
                        System.out.println(strDropdown);
                        System.out.print(field.getLabel() + ": ");
                        String selectFieldVal = scanner.nextLine();
                        if (!items.contains(selectFieldVal)) {
                            throw new RuntimeException("Invalid value: " + selectFieldVal);
                        }
                        config.put(field.getName(), selectFieldVal);
                        break;

                    case TOGGLE_FIELD:
                        ToggleField toggleField = field.getToggleField();
                        String strToggle =
                                String.format(
                                        "<%s [%s] [true, false] [%s] %s>",
                                        field.getName(),
                                        field.getTypeCase(),
                                        description,
                                        field.getRequired() ? "required" : "");
                        System.out.println(strToggle);
                        System.out.print(field.getLabel() + ": ");
                        String toggleFieldVal = scanner.nextLine().toLowerCase();
                        if (!toggleFieldVal.equals("true") && !toggleFieldVal.equals("false")) {
                            throw new RuntimeException("Invalid value: " + toggleField);
                        }
                        config.put(field.getName(), toggleFieldVal);
                        break;

                    default:
                        throw new RuntimeException("Unsupported form field type: " + field.getTypeCase().name());
                }
            }

            return config;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class MapTypeReference extends TypeReference<Map<String, String>> {}
}
