package service;

import com.proto.user.UserRequest;
import com.proto.user.UserResponse;
import com.proto.user.User_serviceGrpc;
import io.grpc.stub.StreamObserver;

public class UserService extends User_serviceGrpc.User_serviceImplBase {
    @Override
    public void userCreation(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        super.userCreation(request, responseObserver);
        System.out.println("Let's start user registration" + request.toString());
    }
 }
