package service;

import grpc.UserService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class server {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder
                .forPort(9090)
                .addService(new UserService()).build();

        server.start();
        System.out.println("Server started");
        server.awaitTermination();
    }


}
