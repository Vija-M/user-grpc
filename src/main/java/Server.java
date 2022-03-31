import grpc.UserService;
import io.grpc.*;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;

public class Server {
    private static final int port = 9090;
    private static io.grpc.Server server;
    private static boolean sslConnection = true;  // ssl or simple connection

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 1) sslConnection = args[0].equals("true") ? true : false;
        if (!sslConnection) {
            simpleServerStart();
        } else {
            mtlsServerStart(); //mutual TLS
        }
        server.awaitTermination();
    }

    public static void simpleServerStart() throws IOException, InterruptedException {
        server = ServerBuilder.forPort(port).addService(new UserService()).build();
        server.start();
        System.out.println("Server started");
    }

    public static void mtlsServerStart() throws IOException, InterruptedException {
        SslContext sslContext = Server.loadTLSCredentials();

        server = NettyServerBuilder.forPort(9090).sslContext(sslContext)
                .addService(new UserService()).build();
        server.start();

        System.out.println("mTLS server started");
    }

    public static void shutdownServer() {
        server.shutdown();
    }

    public static SslContext loadTLSCredentials() throws SSLException {
        File serverCertFile = new File("certif/server-cert.pem");
        File serverKeyFile = new File("certif/server-key.pem");
        File clientCACertFile = new File("certif/ca-cert.pem");

        SslContextBuilder ctxBuilder = SslContextBuilder.forServer(serverCertFile, serverKeyFile)
                .clientAuth(ClientAuth.REQUIRE).trustManager(clientCACertFile);

        return GrpcSslContexts.configure(ctxBuilder).build();
    }
}


