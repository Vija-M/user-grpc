import com.proto.user.User;
import com.proto.user.UserRequest;
import com.proto.user.UserResponse;
import com.proto.user.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientApp {
    private static final String host = "localhost";
    private static final int port = 9090;
    private static ManagedChannel channel;
    private static UserServiceGrpc.UserServiceBlockingStub blockingStub;
    private static UserServiceGrpc.UserServiceStub nonBlockingStub;
    private static List<UserRequest> userRequestList;
    private static UserRequest userRequest;

    static {
        channel = OkHttpChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = UserServiceGrpc.newBlockingStub(channel);
        nonBlockingStub = UserServiceGrpc.newStub(channel);
        userRequestList = createUserRequestList();
        userRequest = createUserRequest();
    }

    public static void main(String[] args) throws InterruptedException {
        callToServer(userRequest);
        serverSideStream(userRequest);
        userSideStream(userRequestList);
      //  bidirectionStream(userRequestList);
    }
    public static UserResponse callToServer(UserRequest request) {
        System.out.println("Unidirectional - User created and will be sent over: " + request);
        UserResponse response = blockingStub.register(request);
        System.out.println("Unidirectional - User registered: " + response.getCreated());
        return response;
    }
    public static void serverSideStream(UserRequest request) {
        Iterator<UserResponse> responses;
        try {
            System.out.println("User created and will be sent over: " + request);
            responses = blockingStub.serverStreamGetCreatedUsers(request);
            for (int i = 1; responses.hasNext(); i++) {
                UserResponse resp = responses.next();
                System.out.println("Server streaming - Response "+ resp.getCreated());
            }
        } catch (StatusRuntimeException exc) {
            System.out.println("Error: "+ exc.getLocalizedMessage());
        }
    }


    public static boolean userSideStream(List<UserRequest> userRequestList) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final boolean[] allDone = new boolean[1];
        StreamObserver<UserResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(UserResponse allRegistered) {
                allDone[0] = allRegistered.getCreated();
                System.out.println("Client side streaming - all users registered? "+ allRegistered.getCreated());
            }

            @Override
            public void onCompleted() {
                System.out.println("Client side streaming completed");
                finishLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error "+ t.getLocalizedMessage());
                finishLatch.countDown();
            }
        };

        StreamObserver<UserRequest> requestObserver = nonBlockingStub.userStreamCreateUsers(responseObserver);
        try {

            for (UserRequest r : userRequestList) {
                System.out.println("User to be sent over: "+ r.getUser());
                requestObserver.onNext(r);
                if (finishLatch.getCount() == 0) {
                    return allDone[0];
                }
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
        requestObserver.onCompleted();
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.out.println("Process didn't finish within 1 minute");
        }
        return allDone[0];
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

}
