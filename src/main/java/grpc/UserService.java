package grpc;

import com.proto.user.UserRequest;
import com.proto.user.UserResponse;
import com.proto.user.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import service.RegistrationService;

import java.sql.SQLException;

public class UserService extends UserServiceGrpc.UserServiceImplBase {

    RegistrationService registrationService = new RegistrationService();

    @Override
    public void register(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        System.out.println("Let's start user registration" + request.toString());
        boolean created = false;
        try {
            created = registrationService.register(request.getUser());
            registrationService.register(request.getUser());
        } catch (SQLException e) {
            System.out.println("Data base error: " + e.getLocalizedMessage());
        }
        UserResponse response = UserResponse.newBuilder().setCreated(created ? true : false).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void serverStreamGetCreatedUsers(UserRequest request, StreamObserver<UserResponse> responseStreamObserver) {
        for (int i = 1; i <= 5; i++) {
            UserResponse response = UserResponse.newBuilder().setCreated(true).build();
            responseStreamObserver.onNext(response);
        }
        responseStreamObserver.onCompleted();
    }

    @Override
    public StreamObserver<UserRequest> userStreamCreateUsers(StreamObserver<UserResponse> response) {
        return new StreamObserver<UserRequest>() {
            int total;
            int counter;

            @Override
            public void onNext(UserRequest request) {
                boolean registered = false;
                try {
                    registered = registrationService.register(request.getUser());
                } catch (SQLException ex) {
                    System.out.println("SQL error " + ex.getLocalizedMessage());
                }
                total++;
                if (registered) counter++;
                System.out.println(total + " total and counter: " + counter);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error + " + throwable.getLocalizedMessage());
            }

            @Override
            public void onCompleted() {
                UserResponse userResponse = UserResponse.newBuilder().setCreated(total == counter ? true : false).build();
                response.onNext(userResponse);
                response.onCompleted();
            }
        };
    }
    @Override
    public StreamObserver<UserRequest> biStreamCreateAndGet(StreamObserver<UserResponse> response) {
        return new StreamObserver<>() {
            int total;
            int counter;

            @Override
            public void onNext(UserRequest request) {
                boolean registered = false;
                try {
                    registered = registrationService.register(request.getUser());
                } catch (SQLException ex) {
                    System.out.println("SQL error " + ex.getLocalizedMessage());
                }
                total++;
                if (registered) counter++;
                UserResponse registeredUser = UserResponse.newBuilder().setCreated(registered ? true : false).build();
                response.onNext(registeredUser);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getLocalizedMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("From total incoming requests: " + total + " succeeded: " + counter);
                response.onCompleted();
            }
        };
    }

}


