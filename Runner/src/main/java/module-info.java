module com.example.runner {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.runner to javafx.fxml;
    exports com.example.runner;
}