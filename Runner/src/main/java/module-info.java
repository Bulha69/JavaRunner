module com.example.runner {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;

    // GSON for JSON parsing
    requires com.google.gson;

    // Open package to JavaFX for FXML loading
    opens com.example.runner to javafx.fxml;

    // Export package
    exports com.example.runner;
}
