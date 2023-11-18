package testers;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import static testers.SdkConnectorTester.CONFIG_FILE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import client.connector.SdkConnectorClient;
import client.destination.SdkWriterClient;
import testers.util.InstantFormattedSerializer;
import testers.util.SdkCrypto;
import com.github.luben.zstd.ZstdOutputStream;
import com.google.protobuf.ByteString;
import fivetran_sdk.Column;
import fivetran_sdk.ConfigurationFormResponse;
import fivetran_sdk.ConfigurationTest;
import fivetran_sdk.DataType;
import fivetran_sdk.DescribeTableResponse;
import fivetran_sdk.Table;
import io.grpc.ManagedChannel;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import picocli.CommandLine;

/** This tool mocks Fivetran connector + core for testing SDK Destination */
public final class SdkDestinationTester {
    private static final Logger LOG = Logger.getLogger(SdkDestinationTester.class.getName());

    private static final String VERSION = "2023.1117.1724";

    private static final CsvMapper CSV = createCsvMapper();
    private static final String DEFAULT_SCHEMA = "tester";
    private static final String DEFAULT_NULL_STRING = "null-m8yilkvPsNulehxl2G6pmSQ3G3WWdLP";
    private static final String DEFAULT_UPDATE_UNMODIFIED = "unmod-NcK9NIjPUutCsz4mjOQQztbnwnE1sY3";
    private static final String SYNCED_COLUMN = "_fivetran_synced";
    private static final String DELETED_COLUMN = "_fivetran_deleted";

    private SdkDestinationTester() {}

    @CommandLine.Command(name = "cliargs", mixinStandardHelpOptions = true, description = "Command line args")
    static class CliArgs {
        @CommandLine.Option(
                names = {"--working-dir"},
                required = true,
                description = "Directory to use for reading/writing files")
        String workingDir;

        @CommandLine.Option(
                names = {"--port"},
                defaultValue = "50052",
                required = true,
                description = "")
        String port;

        @CommandLine.Option(
                names = {"--plain-text"},
                description = "Disable encryption and compression")
        boolean plainText = false;
    }

    public static void main(String[] args) throws IOException {
        CliArgs cliargs = new CliArgs();
        new CommandLine(cliargs).parseArgs(args);

        String grpcHost =
                (System.getenv("GRPC_HOSTNAME") == null)
                        ? SdkConnectorClient.DEFAULT_GRPC_HOST
                        : System.getenv("GRPC_HOSTNAME");

        String grpcWorkingDir = (System.getenv("WORKING_DIR") == null) ?
                cliargs.workingDir : System.getenv("WORKING_DIR");

        new SdkDestinationTester().run(cliargs.workingDir, grpcWorkingDir, grpcHost, Integer.parseInt(cliargs.port), cliargs.plainText);
    }

    public void run(String workingDir, String grpcWorkingDir, String grpcHost, int grpcPort, boolean plainText) throws IOException {
        LOG.info("Version: " + VERSION);
        LOG.info("GRPC_HOSTNAME: " + grpcHost);
        LOG.info("GRPC_PORT: " + grpcPort);
        LOG.info("Working Directory: " + grpcWorkingDir);
        LOG.info("NULL string: " + DEFAULT_NULL_STRING);
        LOG.info("UNMODIFIED string: " + DEFAULT_UPDATE_UNMODIFIED);
        LOG.info("Compression: " + ((plainText) ? "NONE" : "ZSTD"));
        LOG.info("Encryption: " + ((plainText) ? "NONE" : "AES/CBC"));

        ManagedChannel channel = SdkWriterClient.createChannel(grpcHost, grpcPort);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> SdkWriterClient.closeChannel(channel)));
        SdkWriterClient client = new SdkWriterClient(channel);

        File directoryPath = new File(workingDir);
        File[] filesList = directoryPath.listFiles();
        if (filesList == null) {
            LOG.severe("ERROR: Directory is empty");
            System.exit(1);
        }

        LOG.info("Fetching configuration form");
        Path configFilePath = Paths.get(workingDir, CONFIG_FILE);
        ConfigurationFormResponse configurationForm = client.configurationForm();
        SdkConnectorTester.saveConfig(configurationForm, configFilePath);

        String strConfig = Files.readString(configFilePath);
        LOG.info("Configuration:\n" + strConfig);
        Map<String, String> creds =
                SdkConnectorTester.JSON.readValue(strConfig, new SdkConnectorTester.MapTypeReference());

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

        ObjectMapper mapper = new ObjectMapper();
        for (File file : filesList) {
            if (file.isFile() && !file.getName().equals(CONFIG_FILE) && file.getName().endsWith(".json")) {
                LinkedHashMap<String, Object> batch = mapper.readValue(file, new TypeReference<>() {});
                String filename = file.getName().replaceFirst("[.][^.]+$", "");
                writeBatch(filename, batch, client, workingDir, grpcWorkingDir, creds, plainText);
            }
        }

        System.exit(0);
    }

    /** Executes the elements of the batch in the same order as Fivetran core */
    private void writeBatch(
            String batchName,
            Map<String, Object> batch,
            SdkWriterClient client,
            String workingDir,
            String grpcWorkingDir,
            Map<String, String> config,
            boolean plainText)
            throws IOException {
        Map<String, Table> tables = new HashMap<>();
        // <schema-table, Map<op-type, rows>>
        Map<String, Map<String, List<Object>>> tableDMLs = new HashMap<>();
        // <schema-table, truncateBefore>
        Map<String, Instant> tableTruncates = new HashMap<>();

        // describeTable
        if (batch.containsKey("describe_table")) {
            Object entry = batch.get("describe_table");

            if (!(entry instanceof Collection<?>)) {
                throw new RuntimeException("Describe_Table should have a list of table name(s)");
            }

            for (String tableName : (List<String>) entry) {
                DescribeTableResponse response = client.describeTable(DEFAULT_SCHEMA, tableName, config);

                if (response.hasFailure()) {
                    LOG.warning(String.format("Failed to fetch table `%s`: %s", tableName, response.getFailure()));
                } else if (response.getNotFound()) {
                    LOG.info(String.format("Table does not exist at the destination: %s", tableName));
                } else {
                    Table table = response.getTable();
                    LOG.info(String.format("Table: %s\n%s", tableName, table));
                    tables.put(tableName, table);
                }
            }
        }

        // createTable (needs to be fully defined)
        if (batch.containsKey("create_table")) {
            for (var tableEntry : ((Map<String, Object>) batch.get("create_table")).entrySet()) {
                String tableName = tableEntry.getKey();
                if (tables.containsKey(tableName)) {
                    LOG.warning("Table already exists: " + tableName);
                }

                if (!tableDMLs.containsKey(tableName)) {
                    tableDMLs.put(tableName, new LinkedHashMap<>());
                }

                Table table = buildTable(tableName, (Map<String, Object>) tableEntry.getValue());

                Optional<String> result = client.createTable(DEFAULT_SCHEMA, table, config);
                if (result.isPresent()) {
                    LOG.severe(result.get());
                } else {
                    tables.put(tableName, table);
                }
            }
        }

        // alterTable (needs to be fully defined)
        if (batch.containsKey("alter_table")) {
            for (var tableEntry : ((Map<String, Object>) batch.get("alter_table")).entrySet()) {
                String tableName = tableEntry.getKey();

                if (!tableDMLs.containsKey(tableName)) {
                    tableDMLs.put(tableName, new LinkedHashMap<>());
                }

                Table table = buildTable(tableName, (Map<String, Object>) tableEntry.getValue());

                Optional<String> result = client.alterTable(DEFAULT_SCHEMA, table, config);
                if (result.isPresent()) {
                    LOG.severe(result.get());
                } else {
                    tables.put(tableName, table);
                }
            }
        }

        // Upsert, Update, Delete, Truncate (with timestamp)
        if (batch.containsKey("ops")) {
            separateOpsToTables((List<Map<String, Object>>) batch.get("ops"), tableDMLs, tableTruncates);

            // Create batch files per table
            for (var entry : tableDMLs.entrySet()) {
                String table = entry.getKey();
                // At this point we should have a Table object for each table in the ops
                if (!tables.containsKey(table)) {
                    throw new RuntimeException("Table definition is missing");
                }
                Map<String, List<Object>> tableDML = entry.getValue();
                List<Column> columns = tables.get(table).getColumnsList();
                CsvSchema csvSchema = buildCsvSchema(columns);

                Map<String, ByteString> keys = new HashMap();
                List<String> replaceList = new ArrayList<>();
                List<String> updateList = new ArrayList<>();
                List<String> deleteList = new ArrayList<>();

                // separate batch file per op-type
                for (var entry2 : tableDML.entrySet()) {
                    String opName = entry2.getKey().toLowerCase();
                    List<Object> rows = entry2.getValue();

                    SecretKey key = SdkCrypto.newEphemeralKey();
                    String extension = (plainText) ? "csv" : "csv.zst.aes";
                    String filename = String.format("%s_%s_%s.%s", table, batchName, opName, extension);
                    Path path = Paths.get(workingDir, filename);
                    writeFile(path, key, csvSchema, opName, columns, rows, table, plainText);
                    keys.put(path.toString(), ByteString.copyFrom(key.getEncoded()));

                    Path grpcPath = Paths.get(grpcWorkingDir, filename);
                    switch (opName) {
                        case "upsert" -> replaceList.add(grpcPath.toString());
                        case "update" -> updateList.add(grpcPath.toString());
                        case "delete" -> deleteList.add(grpcPath.toString());
                    }
                }

                // Send batch files first
                client.writeBatch(
                        config,
                        DEFAULT_SCHEMA,
                        table,
                        columns,
                        replaceList,
                        updateList,
                        deleteList,
                        keys,
                        "CSV_ZSTD",
                        DEFAULT_NULL_STRING,
                        DEFAULT_UPDATE_UNMODIFIED);

                // Then truncate if any
                if (tableTruncates.containsKey(table)) {
                    client.truncate(
                            DEFAULT_SCHEMA,
                            table,
                            DELETED_COLUMN,
                            SYNCED_COLUMN,
                            tableTruncates.get(table),
                            true,
                            config);
                }
            }
        }
    }

    private void writeFile(
            Path path,
            SecretKey key,
            CsvSchema csvSchema,
            String opName,
            List<Column> columns,
            List<Object> rows,
            String table,
            boolean plainText)
            throws IOException {
        try (OutputStream out = createOutputStream(path, key, plainText);
                SequenceWriter sw =
                        CSV.writer(csvSchema).writeValues(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            Instant now = Instant.now();
            for (var row : rows) {
                Map<String, Object> data = (Map<String, Object>) row;

                data.put(SYNCED_COLUMN, now);

                if (opName.equals("upsert")) {
                    data.put(DELETED_COLUMN, false);
                } else if (opName.equals("update")) {
                    data.put(DELETED_COLUMN, false);
                } else if (opName.equals("delete")) {
                    data.put(DELETED_COLUMN, true);
                }

                for (var c : columns) {
                    if (!data.containsKey(c.getName())) {
                        if (opName.equals("upsert")) {
                            throw new RuntimeException(
                                    String.format(
                                            "Column '%s' is missing in op '%s' for table: %s",
                                            c.getName(), opName, table));
                        } else if (opName.equals("update")) {
                            data.put(c.getName(), DEFAULT_UPDATE_UNMODIFIED);
                        } else if (opName.equals("delete")) {
                            data.put(c.getName(), null);
                        } else {
                            throw new RuntimeException("Unrecognized op: " + opName);
                        }
                    }
                }

                sw.write(data);
            }

            sw.flush();
        }
    }

    private OutputStream createOutputStream(Path path, SecretKey secretKey, boolean plainText) throws IOException {
        OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE);
        if (plainText) {
            return outputStream;
        }
        CipherOutputStream cipherStream = SdkCrypto.encryptWrite(outputStream, secretKey);
        return new ZstdOutputStream(cipherStream, -5);
    }

    private CsvSchema buildCsvSchema(List<Column> columns) {
        CsvSchema.Builder builder = CsvSchema.builder();
        for (Column c : columns) {
            builder.addColumn(c.getName(), csvType(c.getType()));
        }
        builder.addColumn(DELETED_COLUMN, CsvSchema.ColumnType.BOOLEAN);
        builder.addColumn(SYNCED_COLUMN, CsvSchema.ColumnType.STRING);
        return builder.setUseHeader(true).setNullValue(DEFAULT_NULL_STRING).build();
    }

    private Table buildTable(String table, Map<String, Object> tableEntry) {
        Table.Builder tableBuilder = Table.newBuilder().setName(table);
        List<Column> columns = new ArrayList<>();

        List<String> pkeys =
                (tableEntry.containsKey("primary_key"))
                        ? (List<String>) tableEntry.get("primary_key")
                        : Collections.emptyList();

        if (!tableEntry.containsKey("columns")) {
            throw new RuntimeException("Table definition does not contain any columns");
        }

        for (var columnEntry : ((Map<String, Object>) tableEntry.get("columns")).entrySet()) {
            String columnName = columnEntry.getKey();

            if (columnName.equals(DELETED_COLUMN) || columnName.equals(SYNCED_COLUMN)) {
                throw new RuntimeException(String.format("%s is a Fivetran system column name", columnName));
            }

            String stringType = (String) columnEntry.getValue();
            Column.Builder columnBuilder =
                    Column.newBuilder().setName(columnName).setType(strToDataType(stringType.toUpperCase()));

            if (pkeys.contains(columnName)) {
                columnBuilder.setPrimaryKey(true);
            }

            columns.add(columnBuilder.build());
        }

        return tableBuilder.addAllColumns(columns).build();
    }

    private CsvSchema.ColumnType csvType(DataType type) {
        switch (type) {
            case BOOLEAN:
                return CsvSchema.ColumnType.BOOLEAN;
            case STRING:
            case NAIVE_DATE:
            case NAIVE_DATETIME:
            case UTC_DATETIME:
            case JSON:
            case UNSPECIFIED:
            case BINARY:
            case XML:
                return CsvSchema.ColumnType.STRING;
            case SHORT:
            case INT:
            case LONG:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return CsvSchema.ColumnType.NUMBER;
            default:
                throw new RuntimeException("Unknown type " + type);
        }
    }

    private DataType strToDataType(String type) {
        try {
            return DataType.valueOf(type);
        } catch (Exception e) {
            throw new RuntimeException("Unsupported data type: " + type);
        }
    }

    private void separateOpsToTables(
            List<Map<String, Object>> ops,
            Map<String, Map<String, List<Object>>> tableDMLs,
            Map<String, Instant> tableTruncates) {

        for (var opEntry : ops) {
            if (opEntry.size() > 1) {
                throw new RuntimeException("Each operation entry should have a single operation in it");
            }
            String opName = opEntry.keySet().toArray()[0].toString();
            Object op = opEntry.values().toArray()[0];

            if (opName.startsWith("ups") | opName.startsWith("upd") | opName.startsWith("del")) {
                for (var entry2 : ((Map<String, Object>) op).entrySet()) {
                    String table = entry2.getKey();
                    if (!tableDMLs.containsKey(table)) {
                        throw new RuntimeException("Unknown table: " + table);
                    }

                    List<Object> rows = (List<Object>) entry2.getValue();
                    Map<String, List<Object>> tableDML = tableDMLs.get(table);
                    if (tableDML.containsKey(opName)) {
                        tableDML.get(opName).addAll(rows);
                    } else {
                        tableDML.put(opName, new ArrayList<>(rows));
                    }
                }

            } else if (opName.startsWith("tru")) {
                Instant now = Instant.now();
                if (!(op instanceof Collection<?>)) {
                    throw new RuntimeException("Truncate should have a list of table name(s)");
                }

                for (String table : (Collection<String>) op) {
                    if (tableTruncates.containsKey(table)) {
                        LOG.warning("Another truncate for table: " + table);
                    }

                    tableTruncates.put(table, now);
                }

            } else {
                throw new RuntimeException(
                        "Unexpected entry: " + opName + " | " + op.toString() + " | " + op.getClass());
            }
        }
    }

    private static CsvMapper createCsvMapper() {
        CsvMapper mapper = new CsvMapper();

        // Set up time serializers
        JavaTimeModule dates = new JavaTimeModule();
        DateTimeFormatter format =
                new DateTimeFormatterBuilder()
                        .appendValue(YEAR, 4, 10, SignStyle.NEVER)
                        .appendLiteral('-')
                        .appendValue(MONTH_OF_YEAR, 2)
                        .appendLiteral('-')
                        .appendValue(DAY_OF_MONTH, 2)
                        .toFormatter();

        dates.addSerializer(LocalDate.class, new LocalDateSerializer(format));
        dates.addSerializer(Instant.class, new InstantFormattedSerializer(DateTimeFormatter.ISO_INSTANT));

        mapper.registerModule(dates);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_EMPTY_STRINGS, true);

        // Jackson CSV needs this for some reason
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);

        return mapper;
    }
}
