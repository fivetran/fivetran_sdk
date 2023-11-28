package client.connector;

import fivetran_sdk.*;
import fivetran_sdk.Operation;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SdkConnectorClient {
    public static final String DEFAULT_GRPC_HOST = "127.0.0.1";
    public static final int DEFAULT_GRPC_PORT = 50051;
    private static final int MAX_RETRY = 30;
    private static final int INITIAL_WAIT_TIME = 500; // msec

    private final ManagedChannel channel;
    private ConnectorGrpc.ConnectorBlockingStub blockingStub = null;

    public SdkConnectorClient(ManagedChannel channel) {
        this.channel = channel;
    }

    @FunctionalInterface
    public interface OperationConsumer extends Consumer<Operation> {}

    @FunctionalInterface
    public interface LogEntryConsumer extends Consumer<LogEntry> {}

    public SchemaResponse schema(Map<String, String> config, String stateJson) {
        ConnectorGrpc.ConnectorBlockingStub conn = getBlockingStub();
        SchemaRequest request = SchemaRequest.newBuilder().putAllConfiguration(config).build();
        return conn.schema(request);
    }

    public ConfigurationFormResponse configurationForm() {
        ConnectorGrpc.ConnectorBlockingStub conn = getBlockingStub();
        ConfigurationFormRequest configFormRequest = ConfigurationFormRequest.newBuilder().build();
        return conn.configurationForm(configFormRequest);
    }

    public Optional<String> test(String testName, Map<String, String> config) {
        ConnectorGrpc.ConnectorBlockingStub conn = getBlockingStub();
        TestRequest request = TestRequest.newBuilder().setName(testName).putAllConfiguration(config).build();

        TestResponse response = conn.test(request);

        TestResponse.ResponseCase responseCase = response.getResponseCase();

        if (responseCase == TestResponse.ResponseCase.SUCCESS) {
            return Optional.empty();
        } else if (responseCase == TestResponse.ResponseCase.FAILURE) {
            return Optional.of(response.getFailure());
        }

        throw new RuntimeException("Unknown test response: " + responseCase);
    }

    public void update(
            Map<String, String> config,
            String stateJson,
            Selection selection,
            OperationConsumer operationConsumer,
            LogEntryConsumer logEntryConsumer) {
        ConnectorGrpc.ConnectorBlockingStub conn = getBlockingStub();

        UpdateRequest.Builder requestBuilder =
                UpdateRequest.newBuilder().setStateJson(stateJson).setSelection(selection).putAllConfiguration(config);
        UpdateRequest request = requestBuilder.build();

        Iterator<UpdateResponse> it = conn.update(request);
        while (it.hasNext()) {
            UpdateResponse response = it.next();

            if (response.hasLogEntry()) {
                logEntryConsumer.accept(response.getLogEntry());

            } else if (response.hasOperation()) {
                operationConsumer.accept(response.getOperation());
            }
        }
    }

    public static boolean walkSchemaResponse(
            SchemaResponse response, BiConsumer<Optional<String>, List<Table>> consumer) {
        SchemaResponse.ResponseCase responseCase = response.getResponseCase();
        switch (responseCase) {
            case WITH_SCHEMA:
                SchemaList schemaList = response.getWithSchema();
                for (Schema schema : schemaList.getSchemasList()) {
                    consumer.accept(Optional.of(schema.getName()), schema.getTablesList());
                }
                break;

            case WITHOUT_SCHEMA:
                TableList tableList = response.getWithoutSchema();
                consumer.accept(Optional.empty(), tableList.getTablesList());
                break;

            case SCHEMA_RESPONSE_NOT_SUPPORTED:
                return false;

            default:
                throw new RuntimeException("Unknown response case: " + responseCase);
        }

        return true;
    }

    private ConnectorGrpc.ConnectorBlockingStub getBlockingStub() {
        if (blockingStub == null) {
            waitForServer(channel);
            blockingStub = ConnectorGrpc.newBlockingStub(channel);
        }
        return blockingStub;
    }

    public static void waitForServer(ManagedChannel channel) {
        try {
            int retry = 0;
            long waitTime = INITIAL_WAIT_TIME;
            while (retry++ <= MAX_RETRY) {
                ConnectivityState connectivityState = channel.getState(true);
                if (connectivityState == ConnectivityState.READY) {
                    return;
                }
                Thread.sleep(waitTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        throw new RuntimeException("Could not connect to server");
    }

    public static ManagedChannel createChannel(String host, int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().idleTimeout(5, TimeUnit.SECONDS).build();
    }

    public static void closeChannel(ManagedChannel channel) {
        try {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
