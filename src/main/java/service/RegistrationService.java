package service;

import com.proto.user.User;
import repositories.UserRepository;

import java.sql.SQLException;

public class RegistrationService {

    UserRepository repo = new UserRepository();

    public boolean register(User user) throws SQLException {
        return repo.createUser(user);
    }

    }
