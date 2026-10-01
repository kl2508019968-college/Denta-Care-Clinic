module com.mycompany.dentacareclinic {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;

    opens com.mycompany.dentacareclinic to javafx.fxml;
    exports com.mycompany.dentacareclinic;
}
