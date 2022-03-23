package grpc;

import com.proto.user.UserRequest;
import com.proto.user.UserResponse;
import com.proto.user.UserServiceGrpc;
import io.grpc.stub.StreamObserver;

public class UserService extends UserServiceGrpc.UserServiceImplBase {
    @Override
    public void userCreation(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        System.out.println("Let's start user registration" + request.toString());
        UserResponse response = UserResponse.newBuilder().setCreated(true).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
 }
