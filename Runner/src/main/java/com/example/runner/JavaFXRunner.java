package com.example.runner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXRunner extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Load MainMenu.fxml from resources
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/runner/MainMenu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        // Get the controller and pass the stage
        MainMenuController controller = fxmlLoader.getController();
        controller.setStage(stage);

        stage.setTitle("Runner Game");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}