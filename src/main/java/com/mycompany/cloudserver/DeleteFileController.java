// Package declaration
package com.mycompany.cloudserver;

// Import statements
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// Class declaration
public class DeleteFileController {

    // Private variable declaration
    private User user;

    // FXML elements declaration
    @FXML
    private Button buttonSecondary;

    @FXML
    private Spinner spinner;

    /**
     * A method to delete file from the server.
     * @param event an ActionEvent object indicating the event source and type
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @FXML
    private void deleteBtnHandler(ActionEvent event) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        // Establishing a connection with the database
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        // Retrieving the selected file
        String name = spinner.getValue().toString();

        if (name.isEmpty()) {
            // Alerting user when no file is selected for deletion
            displayDialogue("Incomplete Data!", "Select a file to delete");
        } else {
            // Deleting the file
            FileManagement filemanagement = new FileManagement(connection);
            try {
                filemanagement.deleteFile(name, this.user.getUsername());
                // Displaying confirmation message on successful deletion of file
                displayDialogue("Success!", "File Deleted");
            } catch (IOException e) {
                // Alerting user when unable to delete file
                displayDialogue("File Deleted!", "file deleted ");
            }
        }
    }

    /**
     * A method to display a message displayDialogue.
     * @param headerMsg a String object indicating the header of the alert message
     * @param contentMsg a String object indicating the body of the alert message
     */
    private void displayDialogue(String headerMsg, String contentMsg) {
        Stage secondaryStage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 300, 300, Color.DARKGRAY);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);
        Optional<ButtonType> result = alert.showAndWait(); // Allowing user to close the alert box by clicking on any button
    }

    /**
     * A method to switch screen to home screen.
     */
    @FXML
    private void switchToSecondary() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            // Establishing a connection with the database
            DbConnection myObj = new DbConnection();

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
            Logger.getLogger(DeleteFileController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * A method to initialize data into the controller.
     * @param userdata a User object indicating the user data
     */
    public void initialise(User userdata) throws SQLException, ClassNotFoundException {
        // Establishing a connection with the database
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        this.user = userdata;
        FileManagement filemanagement = new FileManagement(connection);

        ObservableList<String> files = FXCollections.observableArrayList(filemanagement.getFilesForUser(this.user.getUsername()));

        SpinnerValueFactory<String> fact = new SpinnerValueFactory.ListSpinnerValueFactory<String>(files);

        spinner.setValueFactory(fact);

    }
}
