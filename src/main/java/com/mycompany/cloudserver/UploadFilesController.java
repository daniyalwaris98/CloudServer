// Importing required libraries and packages
package com.mycompany.cloudserver;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Setting up the controller class for upload files functionality 
public class UploadFilesController {

    // Declaring the variables to be used in the methods of this class
    private User user;
    private File selectedFile;

    // Injecting the elements from the FXML file
    @FXML
    private Button buttonSecondary;
    @FXML
    private Label fileText;
    @FXML
    private TextField textFieldNew;
    @FXML
    private Button selectBtn;

    // Method to refresh the button and reset text fields on click
    @FXML
    private void RefreshBtnHandler(ActionEvent event) {
        Stage primaryStage = (Stage) textFieldNew.getScene().getWindow();
        textFieldNew.setText((String) primaryStage.getUserData());
    }

    // Method to handle file uploading functionality
    @FXML
    private void uploadBtnHandler(ActionEvent event) throws SQLException, IOException, ClassNotFoundException {
        
        // Creating a database connection
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        
        // Checking if a file is selected or not
        if (this.selectedFile != null) {
            try {
                // Uploading the selected file to the database
                FileManagement filemanagement = new FileManagement(connection);
                filemanagement.uploadFile(this.selectedFile.getName(), this.user.getUsername());
            } catch(IOException e) {
                // Display an error message if the file could not be uploaded due to IO exception
                System.out.println(e);
                displayDialogue("Internal Server Error!","IO Exception ");
            }  catch (IllegalArgumentException e) {
                // Display an error message if the file cannot be found
                displayDialogue("Internal Server Error!","File not found ");
            } 
        } else {
            // Display an error message if no file is selected
            displayDialogue("No File", "Choose a file");
        }
    }

    // Method to display a pop-up dialog box with necessary information
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

    // Method to handle file selection event and store the selected file in a variable
    @FXML
    private void selectBtnHandler(ActionEvent event) throws IOException {
        Stage primaryStage = (Stage) selectBtn.getScene().getWindow();
        primaryStage.setTitle("Select a File");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        this.selectedFile = selectedFile;
        if (selectedFile != null) {
            fileText.setText((String) selectedFile.getCanonicalPath());
        }
    }

    // Method to switch to a secondary stage once clicked
    @FXML
    private void switchToSecondary() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            // Redirecting to the secondary stage
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("secondary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 1000);
            secondaryStage.setScene(scene);
            SecondaryController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Welcome");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            // Display an error message if there was any issue switching to the secondary stage
            e.printStackTrace();
        }
    }

    // Method to set the user data
    public void initialise(User userdata) {
        this.user = userdata;
    }
}
