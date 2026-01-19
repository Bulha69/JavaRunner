package com.example.runner;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class GameOverController {
    @FXML private Label scoreLabel;

    private Stage stage;
    private int finalScore;
    private GameController gameController; // Reference to game controller for restarting

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setFinalScore(int score) {
        this.finalScore = score;
        scoreLabel.setText("Score: " + score);
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    @FXML
    private void initialize() {
        System.out.println("GameOverController initialized");
    }

    @FXML
    private void handleRestart() throws Exception {
        System.out.println("Restart button clicked!");

        if (gameController != null) {
            // Restart the game
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/Game.fxml"));
            Scene gameScene = new Scene(loader.load(), 800, 600);
            stage.setScene(gameScene);

            GameController controller = loader.getController();
            controller.setStage(stage);
            controller.startGame();
        } else {
            // Fallback: just start a new game
            handleMenu();
        }
    }

    @FXML
    private void handleMenu() throws Exception {
        System.out.println("Menu button clicked!");
        // Return to main menu
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/MainMenu.fxml"));
        Scene menuScene = new Scene(loader.load(), 800, 600);
        stage.setScene(menuScene);

        MainMenuController controller = loader.getController();
        controller.setStage(stage);
    }

    @FXML
    private void handleQuit() throws Exception {
        System.out.println("Quit button clicked!");

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quit Game");
        alert.setHeaderText("Are you sure you want to quit?");
        alert.setContentText("Your score: " + finalScore);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }
}