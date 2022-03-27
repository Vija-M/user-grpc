package grpc;

import com.proto.user.UserRequest;
import com.proto.user.UserResponse;
import com.proto.user.UserServiceGrpc;
import io.grpc.Metadata;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.reflection.v1alpha.ErrorResponse;
import io.grpc.stub.StreamObserver;
import service.RegistrationService;
import io.grpc.Status;

public class UserService extends UserServiceGrpc.UserServiceImplBase {

    RegistrationService registrationService = new RegistrationService();

    @Override
    public void userCreation(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        System.out.println("Let's start user registration" + request.toString());
Metadata metadata = userExists(request);
if (metadata.keys().size()>0) {
    responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Already exist").asRuntimeException(metadata));
} else {
    registrationService.register(request.getUser());
}
        UserResponse response = UserResponse.newBuilder().setCreated(true).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Metadata userExists(UserRequest request) {
        Metadata metadata = new Metadata();
        if (registrationService.userAlreadyExists(request.getUser())) {

            Metadata.Key<ErrorResponse> errorResponseKey = ProtoUtils.keyForProto(ErrorResponse.getDefaultInstance());
            ErrorResponse errorResponse = ErrorResponse.newBuilder().build();
            metadata.put(errorResponseKey, errorResponse);
        }
        return metadata;
    }
 }
