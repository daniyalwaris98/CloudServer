module com.mycompany.cloudserver {    
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.base;
    requires java.sql; 

    opens com.mycompany.cloudserver to javafx.fxml;
    exports com.mycompany.cloudserver;
}
