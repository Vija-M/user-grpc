package grpc;

import com.proto.user.User;
import com.proto.user.UserRequest;
import com.proto.user.UserResponse;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import service.RegistrationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

class UserServiceTest {
    @InjectMocks
    private UserService service;

    @Mock
    private RegistrationService registrationService;

    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void successfulRegistration() throws Exception {
        userRequest = createUserReq(8, "Tim", "ytrewq");
        userResponse = createUserResponse(true);
        when(registrationService.register(any(User.class))).thenReturn(true);

        StreamRecorder<UserResponse> responseObserver = StreamRecorder.create();
        service.register(userRequest, responseObserver);
        verify(registrationService, times(1)).register(any(User.class));
        List<UserResponse> results = responseObserver.getValues();
        UserResponse response = results.get(0);
        assertEquals(userResponse, response);
    }

    private UserRequest createUserReq(long userId, String username, String password) {
        User user = User.newBuilder().setUsername(username).setUserId(userId).setPassword(password).build();

        UserRequest request = UserRequest.newBuilder().setUser(user).build();
        return request;
    }

    private UserResponse createUserResponse(boolean registered) {
        return UserResponse.newBuilder().setCreated(registered).build();
    }
}