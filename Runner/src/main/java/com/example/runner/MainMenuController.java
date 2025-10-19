package com.example.runner;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.Optional;

public class MainMenuController {
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleStart() throws Exception {
        System.out.println("Start button clicked!");
        // Load Game.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/Game.fxml"));
        Scene gameScene = new Scene(loader.load(), 800, 600);
        stage.setScene(gameScene);

        // Start the game
        GameController controller = loader.getController();
        controller.startGame();
    }

    @FXML
    private void handleOptions() throws Exception {
        System.out.println("Options button clicked!");
        // Switch to options scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/Options.fxml"));
        Scene optionsScene = new Scene(loader.load(), 800, 600);
        stage.setScene(optionsScene);

        // Pass the stage to the options controller
        OptionsController controller = loader.getController();
        controller.setStage(stage);
    }

    @FXML
    private void handleQuit() throws Exception {
        System.out.println("Quit button clicked!");
        // Switch to quit confirmation scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/QuitConfirm.fxml"));
        Scene quitConfirmScene = new Scene(loader.load(), 800, 600);
        stage.setScene(quitConfirmScene);

        // Pass the stage to the quit confirm controller
        QuitConfirmController controller = loader.getController();
        controller.setStage(stage);
    }

}