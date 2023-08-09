/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cloudserver;

import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author ntu-user 
 */
public class UserManagement {

    private Connection connection;
    private DbConnection con;
    private PreparedStatement stmt;
    private SessionManager sessionManager;

    /**
     * @brief get table name Load the SQLite driver Connect to the database
     * Create a session manager
     *
     */
    public void UserManager() {
        try {
            Connection connection = null;
            SessionManager sessionManager = new SessionManager();

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            sessionManager = new SessionManager();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param firstName
     * @param lastName
     * @param email
     * @param username
     * @param pass
     * @throws java.security.spec.InvalidKeySpecException
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     * @brief method to create user Insert statement into DB Create a session
     * manager
     * @return account created successfully as String
     */
    public String createUser(
            String firstName, String lastName, String email, String username, String pass) throws InvalidKeySpecException, SQLException, ClassNotFoundException {
        PreparedStatement stmt = null;
        DbConnection con = new DbConnection();
        Connection connection = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            String encryptedPassword = con.generateSecurePassword(pass);

            stmt = connection.prepareStatement("INSERT INTO users (firstName, lastName, email, username, pass) "
                    + "VALUES (?, ?, ?, ?, ?)");
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, username);
            stmt.setString(5, encryptedPassword);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return "User: " + username + "'s account has been created successfully.";
    }

    /**
     * @param username
     * @return
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     * @brief delete user method
     */
    public String deleteUser(String username) throws SQLException, ClassNotFoundException {
        PreparedStatement stmt = null;
        Connection connection = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            stmt = connection.prepareStatement("DELETE FROM users WHERE username = ?");
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            stmt.close();
            connection.close();
        }
        return "User: " + username + "'s account has been deleted successfully.";
    }

    /**
     *
     * @brief get table name
     * @param id
     * @param firstName
     * @param email
     * @param lastName
     * @param username
     * @param pass
     * @param isAdmin
     * @param isLoggedIn
     * @return user update information as String
     * @throws java.security.spec.InvalidKeySpecException
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     *
     */
    public String updateUser(int id, String firstName, String lastName, String email, String username, String pass, String isAdmin, String isLoggedIn) throws InvalidKeySpecException, SQLException, ClassNotFoundException {

        PreparedStatement stmt = null;
        DbConnection con = new DbConnection();
        Connection connection = null;

        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            String encryptedPassword = con.generateSecurePassword(pass);

            stmt = connection.prepareStatement(
                    "UPDATE users SET firstName = ?, lastName = ?, email = ?, username = ?, pass = ?, isAdmin = ?, isLoggedIn = ? WHERE id = ?");
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, username);
            stmt.setString(5, encryptedPassword);
            stmt.setString(6, isAdmin);
            stmt.setString(7, isLoggedIn);
            stmt.setInt(8, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            stmt.close();
            connection.close();
        }
        return "User: " + username + "'s account has been updated successfully.";
    }

    /**
     * @brief user login method
     * @param username
     * @param password
     * @throws InvalidKeySpecException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void userLogin(String username, String password) throws InvalidKeySpecException, SQLException, ClassNotFoundException {
        PreparedStatement stmt = null;
        DbConnection con = new DbConnection();
        Connection connection = null;

        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            String encryptedPassword = con.generateSecurePassword(password);

            stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND pass = ?");
            stmt.setString(1, username);
            stmt.setString(2, encryptedPassword);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new IllegalArgumentException("Invalid Login details");
            }

            if (rs.getInt("isLoggedIn") == 1) {
                throw new IllegalArgumentException("User is already loggedin");
            }

            stmt = connection.prepareStatement("UPDATE users SET isLoggedIn = ? WHERE username = ?");
            stmt.setInt(1, 1);
            stmt.setString(2, username);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            stmt.close();
            connection.close();
        }
    }

    /**
     * @brief user logout method
     * @param username
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void userLogout(String username) throws SQLException, ClassNotFoundException {
        PreparedStatement stmt = null;
        DbConnection con = new DbConnection();
        Connection connection = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            stmt = connection.prepareStatement("UPDATE users SET isLoggedIn = 0 WHERE username = ?");
            stmt.setString(1, username);
            stmt.executeUpdate();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief Session manager for login, logout and isLoggedIn Login adds
     * session to the map of active sessions, logout removes and isLoggedin
     * checks
     */
    public class SessionManager {

        private Map<String, String> activeSessions;

        public SessionManager() {
            activeSessions = new HashMap<>();
        }

        public void login(String username, String sessionId) {

            activeSessions.put(username, sessionId);
        }

        public void logout(String username) {

            activeSessions.remove(username);
        }

        public boolean isLoggedIn(String username, String sessionId) {

            String activeSessionId = activeSessions.get(username);
            return activeSessionId != null && activeSessionId.equals(sessionId);
        }
    }

    private String generateSessionId() {
        // Generate a random UUID
        return UUID.randomUUID().toString();
    }
}
