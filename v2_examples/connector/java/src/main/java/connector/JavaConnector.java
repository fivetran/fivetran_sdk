package connector;

import io.grpc.*;

import java.io.IOException;

/**
 * Example Plugin Connector (gRPC server)
 * In production, it will be stored as a container image
 */
public class JavaConnector {

    public static void main(String[] args) throws InterruptedException, IOException {
        Server server = ServerBuilder
                .forPort(50051)
                .addService(new ConnectorServiceImpl()).build();

        server.start();
        System.out.println("gRPC server started");
        server.awaitTermination();
    }
}
