// Importing required libraries and packages
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
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// Defining the controller class for copying files from one folder to another
public class CopyFileController {
    
    // Initializing the User object in this class
    private User user;
    
    // Initializing UI components using @FXML annotation
    @FXML
    private TextField oldfilePathTextField;
    
    @FXML
    private TextField textFieldPath;

    @FXML
    private Button buttonSecondary;
    
    @FXML
    private Spinner spinner;
    
    /**
     * Handling the click event of Copy button to copy files from one folder to another
     * @param event - represents the event associated with this method
     * @throws SQLException - if there is any SQL-related error
     * @throws IOException - if there is any IO-related error
     * @throws ClassNotFoundException - if the specified class was not found
     * @throws InterruptedException - if a thread is interrupted while waiting
     */
    @FXML
    private void copyBtnHandler(ActionEvent event) throws SQLException , IOException, ClassNotFoundException, InterruptedException {
        // Creating a connection to SQLite database using DriverManager
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        
        // Getting the selected file and new path from the UI components
        String name = spinner.getValue().toString();
        String newPath = textFieldPath.getText();
        
        // Creating a FileManagement object and copying the file using its copyFile method
        FileManagement filemanagement = new FileManagement(connection);
        try{
            filemanagement.copyFile(name, newPath, this.user.getUsername());
            
            // Displaying a success alert message
            displayDialogue("Success!","File Deleted");
        } catch(IOException e) {
            // Handling IO-related error and displaying an alert message
            displayDialogue("Internal Server Error!","IO Exception ");
        } catch (SQLException e) {
            // Handling SQL-related error and displaying an alert message
            displayDialogue("Internal Server Error!","SQL Exception ");
        } catch (IllegalArgumentException e) {
            // Handling error if the specified file is not found and displaying an alert message
            displayDialogue("Internal Server Error!","File not found ");
        }        
    }
    
    /**
     * Displaying message as an alert dialog
     * @param headerMsg - represents the header of the alert message
     * @param contentMsg - represents the content/body of the alert message
     */
    private void displayDialogue(String headerMsg, String contentMsg) {
        Stage secondaryStage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 300, 300, Color.DARKGRAY);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);
        Optional<ButtonType> result = alert.showAndWait();
    }
   
    /**
     * Switching to home screen by changing the currently displayed screen
     */
    @FXML
    private void switchToSecondary(){
        // Creating stages for primary and secondary screens
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        
        try {
            // Creating a new instance of DbConnection
            DbConnection myObj = new DbConnection();
            
            // Loading the FXML file of secondary screen
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("secondary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 1000);
            
            // Setting the scene of secondary stage and showing it, and closing the primary stage
            secondaryStage.setScene(scene);
            SecondaryController controller = loader.getController();
            controller.initialise(user);
            
            secondaryStage.setTitle("Welcome");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            // Handling any error occurred and printing the stack trace
            e.printStackTrace();
        }
    }

    /**
     * Initializing the UI components by setting the value factory of the spinner using the files retrieved from the database for the logged-in user
     * @param userdata - represents the user data object
     **/
    public void initialise(User userdata) throws SQLException, ClassNotFoundException {
        // Creating a connection to SQLite database using DriverManager
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        
        // Setting the user object
        this.user = userdata;
        
        // Creating a new TextField and FileManagement object
        this.textFieldPath = new TextField();
        FileManagement filemanagement = new FileManagement(connection);
        
        // Retrieving files associated with the logged-in user and displaying them in the spinner
        ObservableList<String> files = FXCollections.observableArrayList(filemanagement.getFilesForUser(this.user.getUsername()));
        SpinnerValueFactory<String> fact = new SpinnerValueFactory.ListSpinnerValueFactory<String>(files);
    
        spinner.setValueFactory(fact);
    }
}
