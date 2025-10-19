package com.example.runner;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class QuitConfirmController {
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleYes() {
        System.out.println("Quit confirmed");

        // Freeze-вам за да излезна
        PauseTransition pause = new PauseTransition(Duration.seconds(0.6));
        pause.setOnFinished(event -> {
            System.out.println("Closing app...");
            stage.close();  // Exit от играта
        });
        pause.play();
    }

    @FXML
    private void handleNo() throws Exception {
        System.out.println("No clicked. Returning to Main Menu.");
        // Switch към main menu
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/MainMenu.fxml"));
        Scene mainMenuScene = new Scene(loader.load(), 800, 600);
        stage.setScene(mainMenuScene);

        // Пак към MenuController
        MainMenuController controller = loader.getController();
        controller.setStage(stage);
    }
}