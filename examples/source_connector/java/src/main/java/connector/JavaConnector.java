package connector;

import io.grpc.*;

import java.io.IOException;

/**
 * Example Plugin Connector (gRPC server)
 * In production, it will be stored as a container image
 */
public class JavaConnector {

    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 50051;
        for(int i=0;i<args.length;i++) if (args[i].equals("--port")) port = Integer.parseInt(args[i + 1]);
        Server server = ServerBuilder
                .forPort(port)
                .addService(new ConnectorServiceImpl()).build();

        server.start();
        System.out.println("gRPC server started on port " + port);
        server.awaitTermination();
    }
}
