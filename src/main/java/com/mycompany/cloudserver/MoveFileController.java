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
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * This is the MoveFileController class that handles user events on the Move File screen.
 */
public class MoveFileController {

    private User user;

    @FXML
    private TextField oldfilePathTextField;

    @FXML
    private TextField textFieldPath;

    @FXML
    private Button buttonSecondary;

    @FXML
    private Spinner spinner;

    /**
     * This method handles the move file button event.
     * It gets the name and new path of the file and calls the required methods to move the file from current location to new location.
     * It displays displayDialogue box for success or error messages.
     *
     * @param event The event object representing a click on the "Move File" button.
     * @throws SQLException if a database access error occurs.
     * @throws IOException if there is an error reading from the input stream.
     * @throws ClassNotFoundException if the class cannot be located.
     * @throws InterruptedException if a thread is interrupted during work.
     */
    @FXML
    private void moveFileBtnHandler(ActionEvent event) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db")) {
            String name = spinner.getValue().toString();
            String newPath = textFieldPath.getText();

                FileManagement filemanagement = new FileManagement(connection);
                try {
                    // Calls the fileMove method of FileManagement class to move the file
                    filemanagement.fileMove(name, newPath, this.user.getUsername());
                    displayDialogue("Success!", "File Moved");
                } catch (IOException e) {
                    displayDialogue("Internal Server Error!", "IO Exception ");
                } catch (SQLException e) {
                    displayDialogue("Internal Server Error!", "SQL Exception ");
                } catch (IllegalArgumentException e) {
                    displayDialogue("Internal Server Error!", "File not found ");
                }
        } catch (SQLException e) {
            e.printStackTrace();
            displayDialogue("Internal Server Error!", "Connection Failed");
        }
    }

    /**
     * This method displays a confirmation displayDialogue to the user with a specified header and content message.
     *
     * @param headerMsg The message to be displayed in the header.
     * @param contentMsg The message to be displayed in the content area.
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
     * This method switches from the current stage to the secondary stage on button click.
     */
    @FXML
    private void switchToSecondary() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
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
            e.printStackTrace();
        }
    }

    /**
     * This method initializes the spinner with files for the user.
     * It sets the value factory and catches exceptions if any.
     *
     * @param userdata The User object containing information about the current user.
     * @throws SQLException if a database access error occurs.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public void initialise(User userdata) throws SQLException, ClassNotFoundException {
        this.user = userdata;
        this.textFieldPath = new TextField();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db")) {
            FileManagement filemanagement = new FileManagement(connection);

            // Gets the list of files for the user and adds them to the spinner's value factory
            ObservableList<String> files = FXCollections.observableArrayList(filemanagement.getFilesForUser(this.user.getUsername()));

            SpinnerValueFactory<String> fact = new SpinnerValueFactory.ListSpinnerValueFactory<String>(files);

            spinner.setValueFactory(fact);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            displayDialogue("Internal Server Error!", "Connection Failed");
        }

    }
}
