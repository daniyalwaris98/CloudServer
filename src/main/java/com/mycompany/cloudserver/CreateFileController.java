// Importing required packages for the class 
package com.mycompany.cloudserver;

import java.io.IOException;
import java.sql.Connection;   
import java.sql.DriverManager;   // Required libraries to be imported
import java.sql.SQLException;
import java.util.Optional;
import javafx.event.ActionEvent;        // JavaFX package
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;          // JavaFX package
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;       // JavaFX package
import javafx.scene.control.TextField;        // JavaFX package
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @brief Controller for Create File screen
 *
 * This class acts as the controller for the Create File screen. It contains methods for saving a new file on the server,
 * displaying a displayDialogue box, switching to the home screen and initializing user data.
 */
public class CreateFileController {

    private User user;

    // Injecting instance variables with FXML ids from the scene graph
    @FXML  
    private TextField fileNameTextField;

    @FXML
    private TextField fileContentTextField;

    @FXML
    private Button buttonSecondary;

    /**
     * @brief save new File to server
     * 
     * This method performs saving of a newly created file on the server. It first establishes the database connection,
     * then retrieves the filename and content entered by the user from the respective fields, checks that both fields are filled, 
     * then creates an instance of FileManagement class and tries to create the file using it. It shows a displayDialogue box with success or error message accordingly
     * 
     * @param event holds the event object passed to the invoking method
     */
    @FXML
    private void saveBtnHandler(ActionEvent event) throws SQLException, IOException, ClassNotFoundException {
        // Establishing the database connection
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        String name = fileNameTextField.getText();
        String content = fileContentTextField.getText();

        // Checking that both fields are filled
        if (name.isEmpty() || content.isEmpty()) {
            displayDialogue("Incomplete Data!", "Both Fields are required");  // Display error message
        } else {
            // Creating an instance of FileManagement class and trying to create the file
            FileManagement filemanagement = new FileManagement(connection);
            try {
                filemanagement.createFile(name, this.user.getUsername(), content);  // Create the file
                displayDialogue("Success!", "File Created");   // Display success message
            } catch (SQLException e) {
                displayDialogue("Internal Server Error!", "Something went wrong");  // Display error message
            } finally {
                // Closing the database connection
                try {
                    if (connection != null) {                       
                        connection.close();
                    }
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            }

        }

    }
    
    /**
     * @brief Display message displayDialogue
     * 
     * This method displays a displayDialogue box with the given messages as header and content.  
     * 
     * @param headerMsg the string that will be displayed in the header of the displayDialogue box
     * @param contentMsg the string that will be displayed as content of the displayDialogue box
     */
    private void displayDialogue(String headerMsg, String contentMsg) {
        Stage secondaryStage = new Stage();    // Creating a new stage
        Group root = new Group();               // Creating a new group
        Scene scene = new Scene(root, 300, 300, Color.DARKGRAY);    // Creating a new scene
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);      // Creating a new alert object
        alert.setTitle("Confirmation Dialog");   // Setting the title of the alert
        alert.setHeaderText(headerMsg);           // Setting the header text of the alert
        alert.setContentText(contentMsg);         // Setting the content text of the alert
        Optional<ButtonType> result = alert.showAndWait();    // Displaying the alert and waiting for the user to click OK
    }
    

    /**
     * @brief change to home screen
     * 
     * This method changes the current view to home page (Secondary view). It first makes a database connection and then accesses the secondary view FXML file. 
     * The user information is also passed from CreateFileController to SecondaryController using the initialize method.
     * 
     */
    @FXML
    private void switchToSecondary() {
        Stage secondaryStage = new Stage();    // Creating a new stage
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();   // Getting the primary stage object
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("secondary.fxml"));     // Loading the FXML file for the home page
            Parent root = loader.load();                                     // Creating a new parent object
            Scene scene = new Scene(root, 1000, 1000);                        // Creating a new scene object
            secondaryStage.setScene(scene);
            SecondaryController controller = loader.getController();
            controller.initialise(user);                // Initializing user data in SecondaryController

            secondaryStage.setTitle("Welcome");
            secondaryStage.show();                      // Displaying the secondary stage
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief initialize data into controller
     * 
     * This method takes user details as argument and initializes the instance variable "user" of the current object with it.
     *
     * @param userdata an instance of User class that holds the user data
     */
    public void initialise(User userdata) {
        this.user = userdata;
    }
}
