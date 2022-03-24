package service;

import com.proto.user.User;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationServiceTest {
    RegistrationService subject = new RegistrationService();

     @Test
    public void test () {
        assertTrue(subject.register(User.newBuilder().setUserId(1).setUsername("Vija").setPassword("123qwe").build()));

    }
}