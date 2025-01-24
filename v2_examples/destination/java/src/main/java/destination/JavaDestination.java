package destination;

import io.grpc.*;

import java.io.IOException;

/**
 * Example Plugin Connector (gRPC server)
 * In production, it will be stored as a container image
 */
public class JavaDestination {

    public static void main(String[] args) throws InterruptedException, IOException {
        Server server = ServerBuilder
                .forPort(50052)
                .addService(new DestinationServiceImpl()).build();

        server.start();
        System.out.println("Destination gRPC server started");
        server.awaitTermination();
    }
}
