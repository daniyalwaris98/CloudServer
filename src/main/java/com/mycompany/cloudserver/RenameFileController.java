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
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RenameFileController { // Class Declaration
    
    private User currentUser; // Declaring a private instance variable of the type User
    
    @FXML
    private Button buttonSecondary; // FXML Annotation for Secondary Button
    
    @FXML
    private Label lblFileText; //FXML Annotation for Label
    
    @FXML
    private TextField txtNewName; //FXML Annotation for Text Field
    
    @FXML
    private Spinner spnFiles; //FXML Annotation for Spinner 
    
    // Method to handle the 'Rename' Button Click Event
    @FXML 
    private void renameBtnHandler(ActionEvent event) throws SQLException , IOException, InterruptedException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db"); // Establishing a Connection on database named "comp20081.db"
        String oldName = spnFiles.getValue().toString(); // retrieving the selected value from spinner and casting it to String
        String newName = txtNewName.getText(); //storing new file name in a local variable
        
        if(oldName.isEmpty() || newName.isEmpty()){ // validating both fields are not empty
            displayAlert("Incomplete Data", "Both fields are required"); // showing Alert Box with message on Screen
        } else {
            FileManagement filemanagement = new FileManagement(connection); // creating an object of class's type 'FileManagement'
            
            try{
                filemanagement.renameFile(oldName, newName, this.currentUser.getUsername()); // calling method 'renameFile' of class 'FileManagement'
                displayAlert("Success", oldName + " has been renamed as " + newName); // Showing Success Alert
            } catch(IOException e) { // Exception Handling
                
                displayAlert("Internal Server Error","IO Exception "); //Showing IO Exception Alert
            } catch (SQLException e) { // Exception Handling
                displayAlert("Internal Server Error","SQL Exception "); //Showing SQL Exception Alert
            } catch (IllegalArgumentException e) { // Exception Handling
                displayAlert("Internal Server Error","File not found "); //Showing File not found Alert
            }
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
    private void switchToSecondary(){
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
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db"); // Establishing a Connection on database named "comp20081.db"
        this.currentUser = user; // setting User Instance variable with incoming parameter value
        FileManagement filemanagement = new FileManagement(connection); // creating an object of class's type 'FileManagement'
        ObservableList<String> files = FXCollections.observableArrayList(filemanagement.getFilesForUser(this.currentUser.getUsername())); // getting all files associated with current user
        SpinnerValueFactory<String> fact = new SpinnerValueFactory.ListSpinnerValueFactory<String>(files); // creating a new Spinner with retrieved data
        
        spnFiles.setValueFactory(fact);  // displaying retrieved data in Spinner of UI
    }
}
