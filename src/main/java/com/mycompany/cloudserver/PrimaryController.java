package com.mycompany.cloudserver;

import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class PrimaryController {

    @FXML
    private Button buttonRegister;

    @FXML
    private TextField userTextField;

    @FXML
    private PasswordField fieldPassword;

    @FXML
    private void registerBtnHandler(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonRegister.getScene().getWindow();
        DbConnection myObj = new DbConnection();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 1000);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Register a new User");
            secondaryStage.show();
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void displayDialogue(String headerMsg, String contentMsg) {
        Stage secondaryStage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 300, 300, Color.DARKGRAY);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);

        Optional<ButtonType> result = alert.showAndWait();
    }

    @FXML
    private void switchToSecondary() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonRegister.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();
            
            User user = myObj.validateUser(userTextField.getText(), fieldPassword.getText());
  
            if(user!= null && user.getIsLoggedIn() == 0){
                myObj.loginUser(user);
                user.setIsLoggedIn(1);
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("secondary.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 1000, 1000);
                secondaryStage.setScene(scene);
                SecondaryController controller = loader.getController();
                controller.initialise(user);
                secondaryStage.setTitle("Welcome");
                String msg="Data Sent from Primary Controller";
                secondaryStage.setUserData(user);
                secondaryStage.show();
                primaryStage.close();
            }
            else if(user == null){
                displayDialogue("Invalid User Name / Password","Please try again!");
            }else {
                myObj.logoutUser(user);
//                displayDialogue("Login Session Exists","Please try again!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
