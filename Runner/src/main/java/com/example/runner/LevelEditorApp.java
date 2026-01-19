package com.example.runner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LevelEditorApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/LevelEditor.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 700);

        LevelEditorController controller = loader.getController();
        controller.setStage(primaryStage);

        primaryStage.setTitle("Runner Game - Level Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}