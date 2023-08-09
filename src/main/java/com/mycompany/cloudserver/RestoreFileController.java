// Import Required Packages
package com.mycompany.cloudserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RestoreFileController {

    private User currentUser; // declaring a private instance variable of the type User

    @FXML
    private Button btnSecondary; // FXML Annotation for Secondary Button
    
    @FXML
    private Spinner spnFiles; //FXML Annotation for Spinner 
    
    // Method to handle the 'Restore' Button Click Event
    @FXML 
    private void buttonRestoreHandler(ActionEvent event) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db"); // Establishing a Connection on database named "comp20081.db"
            String username = this.currentUser.getUsername(); // storing username in a local variable
            String filename = spnFiles.getValue().toString(); // retrieving the selected value from spinner and casting it to String
        
            if (filename.isEmpty()) { // validating Selected value is not empty
                displayAlert("Incomplete Data", "Select a file to delete"); // showing Alert Box with message on Screen
            }
            
            FileManagement userFileManager = new FileManagement(connection); // creating an object of class's type 'FileManagement'
            try {
                userFileManager.fileStore(filename, this.currentUser.getUsername()); // calling method 'fileStore' of class 'FileManagement'
                displayAlert("Success", "File Restore");  // Showing Success Alert
            } catch (IOException e) { // Exception Handling
                displayAlert("Internal Server Error", "IO Exception ");
            } catch (SQLException e) { // Exception Handling
                displayAlert("Internal Server Error", "SQL Exception ");
            } catch (IllegalArgumentException e) { // Exception Handling
                displayAlert("Internal Server Error", "File not found ");
            } finally {
                if (connection != null) { // closing connection
                    connection.close();
                }
            }
        } catch (Exception e) { // General Exception Handling
            
        }
    }
    
    // Method to create Alert Box
    private void displayAlert(String headerMsg, String contentMsg) {
        Stage dialogStage = new Stage(); // Creating New Stage
        Group root = new Group();  // Creating Group of Nodes
        Scene dialogScene = new Scene(root, 300, 300, Color.DARKGRAY);  // Creating New Scene 
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Creating a Confirmation Dialog Box 
        alert.setTitle("Confirmation Dialog"); // Setting Title of Confirmation Box
        alert.setHeaderText(headerMsg); // Displaying Header Message in Confirmation Box
        alert.setContentText(contentMsg); // Displaying Content Message in Confirmation Box
        Optional<ButtonType> result = alert.showAndWait(); // Waiting for User Response
    }
    
    // FXML Annotation for Secondary Button Click Event
    @FXML 
    private void switchToSecondary() {
        Stage secondaryStage = new Stage(); // creating new stage instance
        Stage primaryStage = (Stage) btnSecondary.getScene().getWindow(); // retrieving the Main Stage's Instance
        try {
            DbConnection myObj = new DbConnection(); // creating an instance of DbConnection
            FXMLLoader loader = new FXMLLoader(); // Creating a new instance of FXMLLoader
            loader.setLocation(getClass().getResource("secondary.fxml")); // Loading Secondary View in FXMLLoader Object
            Parent root = loader.load(); // creating Parent Node of Secondary View
            Scene scene = new Scene(root, 1000, 1000); // Creating a Scene with Parent Node and Size
            secondaryStage.setScene(scene); // Seting Up the Secondary Stage
            SecondaryController controller = loader.getController(); // Creating an instance of Secondary Controller loaded via FXMLLoader 
            controller.initialise(currentUser); // Initializing Loaded Secondary View's Data 
            secondaryStage.setTitle("Welcome"); // Setting Title of Secondary Window      
            secondaryStage.show(); // Displaying Secondary View
            primaryStage.close(); // Closing Main Application Screen    

        } catch (Exception e) { // Exception Handling
            e.printStackTrace(); // Printing Stack Trace for Debugging
        }
    }

    // Method to Initialize Controller
    public void initialise(User user) throws SQLException, ClassNotFoundException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        this.currentUser = user; // setting User Instance variable with incoming paramter value
        FileManagement userFileManager = new FileManagement(connection);
        ObservableList<String> files = FXCollections.observableArrayList(userFileManager.getFilesForUser(this.currentUser.getUsername()));
        SpinnerValueFactory<String> fact = new SpinnerValueFactory.ListSpinnerValueFactory<String>(files);

        spnFiles.setValueFactory(fact); // displaying retrieved data in Spinner of UI
    } 
}
