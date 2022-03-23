package service;

import com.proto.user.UserRequest;
import com.proto.user.UserResponse;
import com.proto.user.User_serviceGrpc;
import io.grpc.stub.StreamObserver;

public class UserService extends User_serviceGrpc.User_serviceImplBase {
    @Override
    public void userCreation(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        super.userCreation(request, responseObserver);
    }
    //@Component(immediate = true)
    //public class GreeterService extends GreeterGrpc.AbstractGreeter implements BindableService{
    //
    //    private static final Logger LOG = LoggerFactory.getLogger(GreeterService.class);
    //
    //    @Override
    //    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    //        LOG.info("sayHello endpoint received request from " + request.getName());
    //        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
    //        responseObserver.onNext(reply);
    //        responseObserver.onCompleted();
    //    }
}
