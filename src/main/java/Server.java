import grpc.UserService;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Server {

    public static void main(String[] args) throws IOException, InterruptedException {
        io.grpc.Server server = ServerBuilder
                .forPort(5000)
                .addService(new UserService()).build();

        server.start();
        System.out.println("Server started");
        server.awaitTermination();
    }

}
