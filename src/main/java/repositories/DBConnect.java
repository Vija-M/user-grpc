package repositories;

import java.sql.*;

public class DBConnect {

    public Connection connection;
    private String jdbcURL = "jdbc:h2:mem:test";

    public DBConnect() throws SQLException {
        connection = DriverManager.getConnection(jdbcURL);
        Statement statement = connection.createStatement();
        statement.executeUpdate("Create table if user not exists (ID int primary key, username varchar(20), password varchar(20))");
    }


    public boolean executeUpdateStatement(String sql) throws SQLException {
        System.out.println("in DB connection updates");
        Statement statement = connection.createStatement();    // for INSERT, UPDATE, DELETE or DDL
        int rows = 0;
        try{
            rows = statement.executeUpdate(sql);
        } catch (SQLException e){
            System.out.println("SQL exception: "+ e.getLocalizedMessage());
        }
        return rows>0;
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }

}
