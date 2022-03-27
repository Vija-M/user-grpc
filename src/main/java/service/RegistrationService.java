package service;

import com.proto.user.User;

public class RegistrationService {

    public boolean register(User user) {
        return true;
    }

    public boolean userAlreadyExists(User user) {
        long id = 8;
        String username = "Vija";
        boolean exists = false;

        if (user.getUserId() == id || user.getUsername().equals(username)) {
            exists = true;
        }
        return exists;
    }
}
