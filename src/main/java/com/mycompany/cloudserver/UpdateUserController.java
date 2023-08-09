package com.mycompany.cloudserver;

// Import required libraries
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import javafx.collections.*;
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

// UpdateUserController class definition
public class UpdateUserController {

    // Declare instance variable 'user' of type User
    private User user;

    // Define @FXML annotated fields for UI elements
    @FXML
    private TextField textFieldFirstName;

    @FXML
    private TextField textFieldsLastName;

    @FXML
    private PasswordField fieldPassword;

    @FXML
    private PasswordField fieldPasswordAgain;

    @FXML
    private TextField textFieldEmail;

    @FXML
    private Button buttonSecondary;

    @FXML
    private Button buttonRefresh;

    @FXML
    private TextField textFieldNew;

    // Define Refresh button event handler
    @FXML
    private void refreshBtnHandler(ActionEvent event) {
        // Get the primary stage
        Stage primaryStage = (Stage) textFieldNew.getScene().getWindow();
        // Set text to user data
        textFieldNew.setText((String) primaryStage.getUserData());
    }

    // Define 'switchToPrimary' event handler
    @FXML
    private void switchToPrimary() {
        // Create new secondary stage
        Stage secondaryStage = new Stage();
        // Get the primary stage
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            // Create new DbConnection object
            DbConnection myObj = new DbConnection();
            // Logout the current user
            myObj.logoutUser(this.user);

            // Load FXML file for primary stage
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("primary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);

            // Set the scene for secondary stage and show it
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Login");
            secondaryStage.show();

            // Close the primary stage
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Define 'switchToSecondary' event handler
    @FXML
    private void switchToSecondary() {
        // Create a new secondary stage
        Stage secondaryStage = new Stage();
        // Get the primary stage
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            // Create a new DbConnection object
            DbConnection myObj = new DbConnection();

            // Load FXML file for secondary stage
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("secondary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 1000);

            // Initialize SecondaryController with current user data
            SecondaryController controller = loader.getController();
            controller.initialise(user);

            // Set the scene for secondary stage and show it
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Welcome");
            secondaryStage.show();

            // Close the primary stage
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Define 'displayDialogue' method to display Alert dialogues
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

    // Define 'updateBtnHandler' event handler for Update button
    @FXML
    private void updateBtnHandler() throws InvalidKeySpecException, ClassNotFoundException {

        // Get text from UI elements
        String password = fieldPassword.getText();
        String confirm = fieldPasswordAgain.getText();
        String email = textFieldEmail.getText();
        String firstName = textFieldFirstName.getText();
        String lastName = textFieldsLastName.getText();

        // Create new DbConnection object
        DbConnection myObj = new DbConnection();

        // Check if required fields are missing and display appropriate dialogues
        if (email.isEmpty() || lastName.isEmpty() || firstName.isEmpty()) {
            displayDialogue("Incomplete fields", "All non-password fields are required!");
        }else if(!(password.isEmpty() && confirm.isEmpty()) && ((!password.isEmpty() && confirm.isEmpty()) || (password.isEmpty() && !confirm.isEmpty()))){
            displayDialogue("Incomplete fields", "Supply both password fields or leave them empty");
        }else if(!(password.isEmpty() && confirm.isEmpty()) && !password.equals(confirm)){
            displayDialogue("Invalid Data", "Passwords don't match!");
        }else {
            // Get current user password
            String inPassword = this.user.getPassword();
            // Generate secure password if password field is not empty
            if(!password.isEmpty()){
                inPassword = myObj.generateSecurePassword(password);
            }
            // Call updateUser method of DbConnection class to update current user details
            Boolean update = myObj.updateUser(this.user.getUsername(), inPassword, firstName, lastName, email);

            // If update is successful, update User object and display success displayDialogue
            if(update) {
                this.user.setPassword(inPassword);
                this.user.setEmail(email);
                this.user.setFirstName(firstName);
                this.user.setLastname(lastName);
                fieldPassword.setText("");
                fieldPasswordAgain.setText("");

                displayDialogue("Success!", "User Info Updated Successfully");
            }else{
                // If update fails, display failure displayDialogue
                displayDialogue("Failure!", "Something went wrong!");
            }
        }

    }

    // Define 'initialize' method to initialize UI elements with current user data
    public void initialize(User userdata) {
        this.user = userdata;
        DbConnection myObj = new DbConnection();
        ObservableList<User> data;

        textFieldFirstName.setText(this.user.getFirstName());
        textFieldsLastName.setText(this.user.getLastname());
        textFieldEmail.setText(this.user.getEmail());
    }
}
