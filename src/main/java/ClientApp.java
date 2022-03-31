import com.proto.user.User;
import com.proto.user.UserRequest;
import com.proto.user.UserResponse;
import com.proto.user.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientApp {
    private static final String host = "localhost";
    private static final int port = 9090;
    private static ManagedChannel channel;
    private static ManagedChannel mtlsChannel;
    private static UserServiceGrpc.UserServiceBlockingStub blockingStub;
    private static UserServiceGrpc.UserServiceStub nonBlockingStub;
    private static UserServiceGrpc.UserServiceBlockingStub mtlsBlockingStub;
    private static UserServiceGrpc.UserServiceStub mtlsNonBlockingStub;
    private static List<UserRequest> userRequestList;
    private static UserRequest userRequest;
    private static boolean sslConnection = true;

    public ClientApp() {
        System.out.println("simple created");
        channel = OkHttpChannelBuilder.forAddress(host, port)
                .usePlaintext().build();
        blockingStub = UserServiceGrpc.newBlockingStub(channel);
        nonBlockingStub = UserServiceGrpc.newStub(channel);
 //       userRequestList = createUserRequestList();
   //     userRequest = createUserRequest();
    }

    public ClientApp(SslContext sslContext) {
        System.out.println("ssl created");
        mtlsChannel = NettyChannelBuilder.forAddress(host, port)
                .sslContext(sslContext).build();
        mtlsBlockingStub = UserServiceGrpc.newBlockingStub(mtlsChannel);
        mtlsNonBlockingStub = UserServiceGrpc.newStub(mtlsChannel);
    }


    public static void main(String[] args) throws InterruptedException , SSLException {
        if(args.length==1) sslConnection = args[0].equals("true")? true : false;

        if(!sslConnection) {
        new ClientApp();
        callToServer(userRequest);
        serverSideStream(userRequest);
        userSideStream(userRequestList);
        bidirectStream(userRequestList);
        } else {
            SslContext sslContext = ClientApp.loadTLSCredentials();
            new ClientApp(sslContext);
            mtlsCallToServer(userRequest);
    }}

    public static UserResponse callToServer(UserRequest request) {
        System.out.println("Unidirectional: created user will be sent over: " + request);
        UserResponse response = blockingStub.register(request);
        System.out.println("Unidirectional: registered user: " + response.getCreated());
        return response;
    }

    public static UserResponse mtlsCallToServer(UserRequest request) {
        System.out.println("Unidirectional (mTLS): created user will be sent over: " + request);
        UserResponse response = mtlsBlockingStub.register(request);
        System.out.println("Unidirectional (mTLS): registered user: " + response.getCreated());
        return response;
    }

    public static void serverSideStream(UserRequest request) {
        Iterator<UserResponse> responses;
        try {
            System.out.println("Created user will be sent over: " + request);
            responses = blockingStub.serverStreamGetCreatedUsers(request);
            for (int i = 1; responses.hasNext(); i++) {
                UserResponse resp = responses.next();
                System.out.println("Server streaming - Response " + resp.getCreated());
            }
        } catch (StatusRuntimeException exc) {
            System.out.println("Error: " + exc.getLocalizedMessage());
        }
    }


    public static boolean userSideStream(List<UserRequest> userRequestList) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final boolean[] allDone = new boolean[1];
        StreamObserver<UserResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(UserResponse allRegistered) {
                allDone[0] = allRegistered.getCreated();
                System.out.println("User-side streaming - created users " + allRegistered.getCreated());
            }

            @Override
            public void onCompleted() {
                System.out.println("User-side streaming is done");
                finishLatch.countDown();
            }

            @Override
            public void onError(Throwable thr) {
                System.out.println("Error " + thr.getLocalizedMessage());
                finishLatch.countDown();
            }
        };

        StreamObserver<UserRequest> requestObserver = nonBlockingStub.userStreamCreateUsers(responseObserver);
        try {

            for (UserRequest req : userRequestList) {
                System.out.println("User to be sent over: " + req.getUser());
                requestObserver.onNext(req);
                if (finishLatch.getCount() == 0) {
                    return allDone[0];
                }
            }
        } catch (RuntimeException exc) {
            requestObserver.onError(exc);
            throw exc;
        }
        requestObserver.onCompleted();
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.out.println("1 minute is not enough for to finish process");
        }
        return allDone[0];
    }


    public static List<Boolean> bidirectStream(List<UserRequest> userRequestList) throws InterruptedException {
        System.out.println("Bidirectional streaming");
        final CountDownLatch finishLatch = new CountDownLatch(1);
        List<Boolean> successList = new ArrayList<>();

        StreamObserver<UserResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(UserResponse response) {
                successList.add(response.getCreated());
                System.out.println("User getting response: " + response.getCreated());
            }

            @Override
            public void onCompleted() {
                System.out.println("Bidirectional streaming is done");
                finishLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
                System.out.println("Error " + t.getLocalizedMessage());
            }
        };

        StreamObserver<UserRequest> requestObserver = nonBlockingStub.biStreamCreateAndGet(responseObserver);
        try {
            for (UserRequest req : userRequestList) {
                System.out.println("Created user will be sent over: " + req.getUser());
                requestObserver.onNext(req);
                Thread.sleep(200);
                if (finishLatch.getCount() == 0) {
                    return successList;
                }
            }
        } catch (RuntimeException exc) {
            requestObserver.onError(exc);
            throw exc;
        }
        requestObserver.onCompleted();

        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.out.println("1 minute is not enough to finish process");
        }
        return successList;
    }


    private static List<UserRequest> createUserRequestList() {
        List<UserRequest> users = new ArrayList<>();
        users.add(UserRequest.newBuilder().setUser(User.newBuilder().setUserId(1l).setUsername("anna").setPassword("password1").build()).build());
        users.add(UserRequest.newBuilder().setUser(User.newBuilder().setUserId(2l).setUsername("alla").setPassword("password2").build()).build());
        users.add(UserRequest.newBuilder().setUser(User.newBuilder().setUserId(3l).setUsername("aija").setPassword("password3").build()).build());
        return users;
    }

    private static UserRequest createUserRequest() {
        User user = User.newBuilder().setUserId(1l).setUsername("olga").setPassword("qwerty").build();
        UserRequest userRequest = UserRequest.newBuilder().setUser(user).build();
        return userRequest;
    }



    public static SslContext loadTLSCredentials() throws SSLException {
        System.out.println("ssl loaded");
        File serverCACertFile = new File("certif/ca-cert.pem");
        File clientCertFile = new File("certif/client-cert.pem");
        File clientKeyFile = new File("certif/client-key.pem");

        return GrpcSslContexts.forClient()
                .keyManager(clientCertFile, clientKeyFile)
                .trustManager(serverCACertFile)
                .build();
    }
}
