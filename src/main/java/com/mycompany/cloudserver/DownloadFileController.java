// Importing necessary packages
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
import javafx.scene.control.Spinner;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// Defining the class DownloadFileController
public class DownloadFileController {
    
    // Declaring a private object variable of User class
    private User user;
    
    // Defining the spinner element using fxml
    @FXML
    private Spinner spinner;
   
    // Defining the secondary button element using fxml
    @FXML
    private Button buttonSecondary;
    
    // Handler function for download button press event
    @FXML
    private void downloadBtnHandler(ActionEvent event) throws SQLException , IOException, ClassNotFoundException, InterruptedException {
        
        // Creating a connection to the sqlite database
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        
        // Extracting value of the selected file from spinner element
        String name = spinner.getValue().toString();
        
        // If no file selected, show an alert dialog box
        if(name.isEmpty()){
            displayDialogue("Incomplete Data","Select a file to delete");
        }
        else {
            // Create an instance of FileManagement class and call its method fileDownload()
            FileManagement filemanagement = new FileManagement(connection);
            try{
                filemanagement.fileDownload(name, this.user.getUsername());
                displayDialogue("Success!","File Downloaded");
            } catch(IOException e) {
                // Catch block for handling IOException
                displayDialogue("Internal Server Error!","IO Exception ");
            } catch (SQLException e) {
                // Catch block for handling SQLException
                displayDialogue("Internal Server Error!","SQL Exception ");
            } catch (IllegalArgumentException e) {
                // Catch block for handling IllegalArgumentException
                displayDialogue("Internal Server Error!","File not found ");
            }
        }
    }
    
    // Method to display an alert dialog with given messages as header and content
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
    
    // Defining the text field element using fxml
    @FXML
    private TextField textFieldNew;
    
    // Functionality of switch button to switch to a secondary stage
    @FXML
    private void switchToSecondary(){
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            // Create a new instance of DbConnection class
            DbConnection myObj = new DbConnection();
            
            
            // Load the FXML file for secondary stage and create a new stage
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("secondary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 1000);
            
            // Set the scene and show the stage
            secondaryStage.setScene(scene);
            
            // Get the instance of SecondaryController and call its initialisation method
            SecondaryController controller = loader.getController();
            controller.initialise(user);
            
            secondaryStage.setTitle("Welcome");
            secondaryStage.show();
            
            // Close the primary stage
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to initialise the controller with user data and retrieve file list for that user from database
    public void initialise(User userdata) throws SQLException, ClassNotFoundException {
        
        // Create a connection to the sqlite database
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        
        // Store user data in the class variable
        this.user = userdata;
        
        // Create an instance of FileManagement class to get file list for that user
        FileManagement filemanagement = new FileManagement(connection);
        
        // Retrieve file list from database
        ObservableList<String> files = FXCollections.observableArrayList(filemanagement.getFilesForUser(this.user.getUsername())); 
        
        // Set spinner value factory to display list of files
        SpinnerValueFactory<String> fact = new SpinnerValueFactory.ListSpinnerValueFactory<String>(files);    
        spinner.setValueFactory(fact);    
    }
}
