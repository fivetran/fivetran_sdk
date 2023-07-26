package connector;

import io.grpc.*;

import java.io.IOException;

/**
 * Example Plugin Connector (gRPC server)
 * In production, it will be stored as a container image
 */
public class JavaWriter {

    public static void main(String[] args) throws InterruptedException, IOException {
        Server server = ServerBuilder
                .forPort(50052)
                .addService(new WriterServiceImpl()).build();

        server.start();
        System.out.println("Writer gRPC server started");
        server.awaitTermination();
    }
}
