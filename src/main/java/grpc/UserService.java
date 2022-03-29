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
    public void userCreation(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        System.out.println("Let's start user registration" + request.toString());
    boolean created = false;
    try {
        created = registrationService.register(request.getUser());
    registrationService.register(request.getUser());
} catch (SQLException e) {
        System.out.println("Data base error: " + e.getLocalizedMessage());
    }
        UserResponse response = UserResponse.newBuilder().setCreated(created? true : false).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

 }
