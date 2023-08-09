package com.mycompany.cloudserver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SecondaryController {

    private User user;

    @FXML
    private Label userTextField;


    @FXML
    private Button buttonSecondary;

    @FXML
    private void RefreshBtnHandler(ActionEvent event) {
        //Stage primaryStage = (Stage) textFieldNew.getScene().getWindow();
        // textFieldNew.setText((String)primaryStage.getUserData());
    }

    @FXML
    private void switchToPrimary() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();
            myObj.logoutUser(this.user);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("primary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 1000);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Login");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToViewLogs() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("viewlogs.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            ViewLogsController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("View Files");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToUploadFile() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();
//            myObj.logoutUser(this.user);
            
            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("uploadfile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            UploadFilesController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Upload File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToShareFile() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("sharefile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            ShareFileController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Share File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void switchToMoveFile() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("movefile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            MoveFileController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Move File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void switchToDownloadFile() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("downloadfile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            DownloadFileController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Download File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void switchToRestoreFile() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("restorefile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            RestoreFileController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Restore File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void switchToCopyFile() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("copyfile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            CopyFileController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Copy File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void switchToErrors() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("errors.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            ShareFileController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Errors");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToUpdateUser() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("updateuser.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            UpdateUserController controller = loader.getController();
            controller.initialize(user);
            secondaryStage.setTitle("Rename File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void switchToRenameFiles() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("renamefile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 1000);
            secondaryStage.setScene(scene);
            RenameFileController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Rename File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @FXML
    private void switchToDeleteFiles() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("deletefile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            DeleteFileController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Delete File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void switchToUserList() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("userlist.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1920, 1080);
            secondaryStage.setScene(scene);
            UserListController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("View Users");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void switchToCreateFile() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) buttonSecondary.getScene().getWindow();
        try {
            DbConnection myObj = new DbConnection();
            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("createfile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            CreateFileController controller = loader.getController();
            controller.initialise(user);
            secondaryStage.setTitle("Create File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        } 
        
    }

    public void initialise(User userdata) {
        this.user = userdata;
        userTextField.setText("Welcome "+userdata.getUsername());
        // this.user=
        DbConnection myObj = new DbConnection();
//         UserManagement myObj = new UserManagement();
    }
}
