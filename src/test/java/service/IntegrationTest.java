package service;

import com.proto.user.User;
import com.proto.user.UserRequest;
import com.proto.user.UserResponse;
import grpc.UserService;
import io.grpc.*;
import org.junit.jupiter.api.Test;

class IntegrationTest {

    private static final int port = 5000;
    private static Server server = ServerBuilder
            .forPort(port)
            .addService(new UserService()).build();

    @Test
    void register() throws Exception{
        server.start();
        UserRequest request = newUserRequest(21l, "Anna", "anna");
        server.shutdown();
    }

    private UserRequest newUserRequest(long userId, String username, String password) {
        User user = User.newBuilder()
                .setUsername(username)
                .setUserId(userId)
                .setPassword(password)
                .build();

        UserRequest request = UserRequest.newBuilder()
                .setUser(user)
                .build();
        return request;
    }


}