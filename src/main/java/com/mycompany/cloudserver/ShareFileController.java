package com.mycompany.cloudserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Controller class for Sharing file interface.
 */
public class ShareFileController {
    
    private User user;

    @FXML
    private Button buttonSecondary;
    
    @FXML
    private Spinner spinnerUser;
    
    @FXML
    private Spinner spinnerFile;
   
   /**
    * This method is called when the "Share File" button is clicked.
    * It gets the values of the selected User and File, checks for incomplete data
    * and downloads the selected file. If an error occurs, it displays the error message.
    *
    * @param event The ActionEvent object representing the click action.
    * @throws SQLException Thrown when a database access error occurs.
    * @throws IOException Thrown when an IO Exception occurs.
    * @throws ClassNotFoundException Thrown when a class not found exception occurs.
    * @throws InterruptedException Thrown when a thread is interrupted while sleeping.
    */
    @FXML
    private void filShareHandler(ActionEvent event) throws SQLException , IOException, ClassNotFoundException, InterruptedException {
        String name = spinnerUser.getValue().toString();
        String file = spinnerFile.getValue().toString();
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        
        if(name.isEmpty() || file.isEmpty()){
            displayDialogue("Incomplete Data","Both fields are required");
        }else {
            FileManagement filemanagement = new FileManagement(connection);
            try{
                filemanagement.fileDownload(name, this.user.getUsername());
                displayDialogue("Success!","File Downloaded");
            } catch(IOException e) {
                displayDialogue("Internal Server Error!","IO Exception ");
            } catch (SQLException e) {
                displayDialogue("Internal Server Error!","SQL Exception ");
            } catch (IllegalArgumentException e) {
                displayDialogue("Internal Server Error!","File not found ");
            }
        }
    }
    
   /**
    * This method displays the given message in an alert box.
    *
    * @param headerMsg The header message to display.
    * @param contentMsg The content message to display.
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
    * This method changes the scene to the secondary interface when "Switch to Secondary" button is clicked.
    * It also logs out the current user.
    */
    @FXML
    private void switchToSecondary(){
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();
            //myObj.logoutUser(this.user);
        
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
    * This method initializes the ShareFileController with the User data.
    * It displays a list of files available to current user on spinnerFile.
    *
    * @param userdata The current User object.
    * @throws SQLException Thrown when a database access error occurs.
    * @throws ClassNotFoundException Thrown when a class not found exception occurs.
    */
    public void initialise(User userdata) throws SQLException, ClassNotFoundException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");
        this.user = userdata;
        FileManagement filemanagement = new FileManagement(connection);
        
        ObservableList<String> files = FXCollections.observableArrayList(filemanagement.getFilesForUser(this.user.getUsername()));        
        SpinnerValueFactory<String> fact = new SpinnerValueFactory.ListSpinnerValueFactory<String>(files);
    
        spinnerFile.setValueFactory(fact);
        spinnerUser.setValueFactory(fact);
    }
}
