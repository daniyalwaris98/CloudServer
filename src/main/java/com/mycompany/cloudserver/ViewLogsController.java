// Import necessary libraries/packages
package com.mycompany.cloudserver;
import java.sql.ResultSet;     
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

// Define a class named ViewLogsController
public class ViewLogsController {
    
    // Declare a private variable of type User
    private User user;  
    
    // FXML annotation for a button named buttonSecondary 
    @FXML
    private Button buttonSecondary;

    // Method called when buttonSecondary is clicked, opens and sets up the second stage
    @FXML
    private void switchToSecondary(){
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        
        // Try-catch block to handle any exceptions with creating/loading scene or initialising the controller
        try {
            DbConnection dbConnObj = new DbConnection();      // Create an instance of DbConnection        
            FXMLLoader loader = new FXMLLoader();         // Create a new instance of FXMLLoader
            loader.setLocation(getClass().getResource("secondary.fxml"));       // Load the secondary.fxml file
            Parent root = loader.load();                  // Load the root element of the FXML file
            Scene scene = new Scene(root, 1000,1000);      // Create a scene with root element and size
                        
            secondaryStage.setScene(scene);        // Set the scene to the secondaryStage
            SecondaryController controller = loader.getController();
            controller.initialise(user);          // Call initialise() method from SecondaryController class and pass user data
            
            secondaryStage.setTitle("Welcome");   // Set the title of the secondaryStage
            secondaryStage.show();                // Show the secondaryStage
            primaryStage.close();                 // Close the primary stage

        } catch (Exception e) {     // Handle exception by printing its stack trace
            e.printStackTrace();
        }
    }

    // Method to initialise TableView's columns and get the data from DbConnection
    public void initialise(User userData) {
        this.user = userData;               // Pass the user data to the instance variable
        DbConnection dbConnObj = new DbConnection();      // Create an instance of DbConnection      
        ObservableList<User> data;          // Declare an observable list of type User
        
        // Try-catch block to handle any exceptions with creating/listing table columns or getting table data from DbConnection
        try {
            data = dbConnObj.getDataFromTable();       // Call getDataFromTable() method from DbConnection class         
            
            TableColumn usernameCol = new TableColumn("Username");    // Create a column for username data
            usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));  // Set cell value factory for username
            
            TableColumn passwordCol = new TableColumn("Password");    // Create a column for password data
            passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));  // Set cell value factory for password
            
            TableColumn emailCol = new TableColumn("Email");          // Create a column for email data
            emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));        // Set cell value factory for email
            
            TableColumn firstNameCol = new TableColumn("First Name"); // Create a column for first name data
            firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstname"));  // Set cell value factory for firstname
            
            TableColumn lastNameCol = new TableColumn("Last Name");   // Create a column for last name data
            lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastname"));   // Set cell value factory for lastname
            
            TableColumn isAdminCol = new TableColumn("Admin");        // Create a column for isAdmin data
            isAdminCol.setCellValueFactory(new PropertyValueFactory<>("isAdmin"));    // Set cell value factory for isAdmin
            
            TableColumn isLoggedInCol = new TableColumn("Logged In");  // Create a column for isLoggedIn data
            isLoggedInCol.setCellValueFactory(new PropertyValueFactory<>("isLoggedIn"));  // Set cell value factory for isLoggedIn
            
        } catch (ClassNotFoundException ex) {       // Handle exception by printing its stack trace
            Logger.getLogger(SecondaryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
