/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cloudserver;

/**
 *
 * @author ntu-user
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author ntu-user
 */
public class DbConnection {

    Connection dbConnect = null;
    private int timeout = 30;
    private Random random = new SecureRandom();
    private String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private int iterations = 10000;
    private int keylength = 256;
    private String mySaltValue;
    private String fileName = "jdbc:sqlite:comp20081.db";
    private String dataBaseName = "comp20081.db";
    private String dataBaseTableName = "users";
    private String errorLogsTable = "errors";
    Connection connection = null;
    private String saltValue;

    /**
     * @brief constructor - for encryption, this generates the salt file if it
     * doesn't exist or load it from the file .salt
     */
    DbConnection() {
        try {
            File mySalt = new File(".salt");
            if (!mySalt.exists()) {
                mySaltValue = this.getSaltvalue(30);
                try ( FileWriter myFileWriter = new FileWriter(mySalt)) {
                    myFileWriter.write(mySaltValue);
                }
            } else {
                Scanner myFileReader = new Scanner(mySalt);
                while (myFileReader.hasNextLine()) {
                    mySaltValue = myFileReader.nextLine();
                }
            }
        } catch (IOException e) {
        }
    }


    /* This method generates the salt value */
    private String getSaltvalue(int saltlength) {
        StringBuilder FinalValue = new StringBuilder(saltlength);

        for (int i = 0; i < saltlength; i++) {
            FinalValue.append(characters.charAt(random.nextInt(characters.length())));
        }

        return new String(FinalValue);
    }

    /* This method generates the hash value */
    private byte[] hash(char[] password, byte[] mySalt) throws InvalidKeySpecException {
        PBEKeySpec MySpec = new PBEKeySpec(password, mySalt, iterations, keylength);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory mySecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return mySecretKeyFactory.generateSecret(MySpec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("An error occurred while hashing the password: " + e.getMessage(), e);
        } finally {
            MySpec.clearPassword();
        }
    }

    /* This method generates the encrypted password */
    public String generateSecurePassword(String password) throws InvalidKeySpecException {
        String finalval = null;

        byte[] securePassword = hash(password.toCharArray(), mySaltValue.getBytes());

        finalval = Base64.getEncoder().encodeToString(securePassword);

        return finalval;
    }

    public void log(String message) {
        System.out.println(message);
    }

    void close() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    
    
    /**
     * @brief get data from the Database method
     * @throws ClassNotFoundException
     * @return ResultSet
     */
    public ObservableList<User> getDataFromTable() throws ClassNotFoundException {
        ObservableList<User> result = FXCollections.observableArrayList();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("select * from " + this.dataBaseTableName);
            while (rs.next()) {
                // read the result set
                result.add(initializeUser(rs));
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        return result;
    }
    
    
    public ObservableList<User> getErrorsFromTable() throws ClassNotFoundException {
        ObservableList<User> result = FXCollections.observableArrayList();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("select * from " + this.errorLogsTable);
            while (rs.next()) {
                // read the result set
                result.add(initializeUser(rs));
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        return result;
    }

    
    /**
     * @brief initialize user
     * @param rs
     * @throws ClassNotFoundException
     * @return ResultSet
     */
    public User initializeUser(ResultSet rs) throws ClassNotFoundException {
        User user = null;
        try {

            String username = rs.getString("username");
            String firstname = rs.getString("firstname");
            String lastname = rs.getString("lastname");
            String password = rs.getString("pass");
            String email = rs.getString("email");
            Integer isAdmin = rs.getInt("isAdmin");
            Integer isLoggedIn = rs.getInt("isLoggedIn");

            user = new User(username, password, firstname, lastname, email, isLoggedIn, isAdmin);

        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return user;
    }

    /**
     * @brief decode password method to validate user
     * @throws java.security.spec.InvalidKeySpecException
     * @throws java.lang.ClassNotFoundException
     * @param username name as type String
     * @param password plain password of type String
     * @return user if the credentials are valid, otherwise null
     */
    public User validateUser(String username, String password) throws InvalidKeySpecException, ClassNotFoundException {
        User user = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            String inPass = generateSecurePassword(password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND pass = ?");
            statement.setQueryTimeout(timeout);
            statement.setString(1, username);
            statement.setString(2, inPass);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                user = initializeUser(rs);
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return user;
    }

    /**
     * @brief check if an email is in use
     * @param email
     * @return
     * @throws InvalidKeySpecException
     * @throws ClassNotFoundException
     */

    public Boolean checkEmailExists(String email) throws InvalidKeySpecException, ClassNotFoundException {
        Boolean flag = false;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email = ?");
            statement.setQueryTimeout(timeout);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                flag = true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return flag;

    }

    /**
     * @brief check if a username is in use
     * @param username
     * @return
     * @throws InvalidKeySpecException
     * @throws ClassNotFoundException
     */

    public Boolean checkUserExists(String username) throws InvalidKeySpecException, ClassNotFoundException {
        Boolean flag = false;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            statement.setQueryTimeout(timeout);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                flag = true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return flag;

    }


    /**
     * @brief create a new user
     * @param username
     * @param password
     * @param firstname
     * @param lastname
     * @param email
     * @param isAdmin
     * @return
     * @throws InvalidKeySpecException
     * @throws ClassNotFoundException
     */

    public Boolean createUser(String username, String password, String firstname, String lastname, String email, Integer isAdmin) throws InvalidKeySpecException, ClassNotFoundException {
        // User user = null;

        Boolean flag = false;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            String inPass = generateSecurePassword(password);

            // Update the user in the database
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (firstname, lastname, email, username, pass, isAdmin, isLoggedIn) "
                    + "VALUES (?, ?, ?, ?, ?,?,?)");
            statement.setString(1, firstname);
            statement.setString(2, lastname);
            statement.setString(3, email);
            statement.setString(4, username);
            statement.setString(5, inPass);
            statement.setInt(6, isAdmin);
            statement.setInt(7, 0);
            statement.executeUpdate();

            flag = true;

        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return flag;
    }

    /**
     * @brief update a user
     * @param username
     * @param password
     * @param firstname
     * @param lastname
     * @param email
     * @return
     * @throws InvalidKeySpecException
     * @throws ClassNotFoundException
     */
    public Boolean updateUser(String username, String password, String firstname, String lastname, String email) throws InvalidKeySpecException, ClassNotFoundException {
        // User user = null;

        Boolean flag = false;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Update the user in the database
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET firstname = ?, lastname = ?, email = ?, pass = ? WHERE username = ?");
            statement.setString(1, firstname);
            statement.setString(2, lastname);
            statement.setString(3, email);
            statement.setString(4, password);
            statement.setString(5, username);
            statement.executeUpdate();

            flag = true;

        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return flag;
    }

    /**
     * @brief mark a user as logged In
     * @param user
     * @return
     * @throws ClassNotFoundException
     */

    public User loginUser(User user) throws ClassNotFoundException {
        try {
            String username = user.getUsername();

            // Load the SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Connect to the database
            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET isLoggedIn = ? WHERE  username = ?");
            stmt.setInt(1, 1);
            stmt.setString(2, username);
            stmt.executeUpdate();
            user.setIsLoggedIn(1);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return user;

    }


    /**
     * @brief mark a user as logged out
     * @param user
     * @return
     * @throws ClassNotFoundException
     */
    public User logoutUser(User user) throws ClassNotFoundException {
        try {
            String username = user.getUsername();

            // Load the SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Connect to the database
            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET isLoggedIn = ? WHERE username = ?");
            stmt.setInt(1, 0);
            stmt.setString(2, username);
            stmt.executeUpdate();
            user.setIsLoggedIn(0);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return user;
    }

    /**
     * @brief mark a user as an admin
     * @param user
     * @return
     * @throws ClassNotFoundException
     */
    public User makeUserAdmin(User user) throws ClassNotFoundException {
        try {
            String username = user.getUsername();

            // Load the SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Connect to the database
            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET isAdmin = ? WHERE username = ?");
            stmt.setInt(1, 1);
            stmt.setString(2, username);
            stmt.executeUpdate();
            user.setIsLoggedIn(1);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return user;

    }

    /**
     * @brief mark admin user as non-admin
     * @param user
     * @return
     * @throws ClassNotFoundException

     */
    public User removeUserAdmin(User user) throws ClassNotFoundException {
        try {
            String username = user.getUsername();

            // Load the SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Connect to the database
            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET isAdmin = ? WHERE username = ?");
            stmt.setInt(1, 0);
            stmt.setString(2, username);
            stmt.executeUpdate();
            user.setIsLoggedIn(0);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return user;
    }
    

    /**
     * @brief get table name
     * @return table name as String
     */
    public String getTableName() {
        return this.dataBaseTableName;
    }

    public static void main(String[] args) throws Exception {

        //stry {
        DbConnection conn = new DbConnection();
        System.out.print(conn);
    }

    void logger(String username, String name, String createFile, String toString, String string, SQLException e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
