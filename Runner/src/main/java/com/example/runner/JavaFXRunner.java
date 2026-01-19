package com.example.runner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXRunner extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/MainMenu.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        primaryStage.setTitle("Runner Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Pass stage to controller
        MainMenuController controller = loader.getController();
        controller.setStage(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}