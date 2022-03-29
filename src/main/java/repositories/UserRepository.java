package repositories;

import com.proto.user.User;

import java.sql.SQLException;

public class UserRepository {

    public boolean createUser(User user) throws SQLException {
        DBConnect connection = new DBConnect();

        String sql = "Insert into user (ID, username, password) values ";
        sql += String.format("(%s, '%s', '%s')", user.getUserId(), user.getUsername(), user.getPassword());
        boolean created = false;
        try {
            created = connection.executeUpdateStatement(sql);
        } catch (SQLException ex) {
            throw ex;
        } finally {
            connection.closeConnection();
        }
        return created;
    }

}
