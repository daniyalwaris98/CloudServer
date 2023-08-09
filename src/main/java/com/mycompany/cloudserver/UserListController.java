package com.mycompany.cloudserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


// <TableView fx:id="dataTableView" prefHeight="200.0" prefWidth="200.0" />


public class UserListController {
    
    private User user;
    
    @FXML
    private TableView dataTableView;

    @FXML
    private Button buttonSecondary;
    
    
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
            Scene scene = new Scene(root, 1000,1000);
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

    public void initialise(User userdata) {
        this.user = userdata;
        DbConnection myObj = new DbConnection();
        ObservableList<User> data;
        try {
            data = myObj.getDataFromTable();
            TableColumn user = new TableColumn("Username");
        user.setCellValueFactory(
        new PropertyValueFactory<>("username"));

        TableColumn pass = new TableColumn("Password");
        pass.setCellValueFactory(
            new PropertyValueFactory<>("password"));
        
        TableColumn email = new TableColumn("Email");
        email.setCellValueFactory(
            new PropertyValueFactory<>("email"));
        
        
        TableColumn firstname = new TableColumn("First Name");
        firstname.setCellValueFactory(
            new PropertyValueFactory<>("firstname"));
        
        TableColumn lastname = new TableColumn("Last Name");
        lastname.setCellValueFactory(
            new PropertyValueFactory<>("lastname"));
        
        TableColumn isAdmin = new TableColumn("Admin");
        isAdmin.setCellValueFactory(
            new PropertyValueFactory<>("isAdmin"));
        
        TableColumn isLoggedIn = new TableColumn("Logged In");
        isLoggedIn.setCellValueFactory(
            new PropertyValueFactory<>("isLoggedIn"));
        
        
        dataTableView.setItems(data);
        dataTableView.getColumns().addAll(user, email, firstname, lastname, isAdmin, isLoggedIn, pass);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SecondaryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
