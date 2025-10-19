package com.example.runner;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class OptionsController {
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleOptionA() {
        System.out.println("Option A selected!");  // Debug print (you can add real functionality here, e.g., change settings)
    }

    @FXML
    private void handleOptionB() {
        System.out.println("Option B selected!");  // Debug print (add functionality later)
    }

    @FXML
    private void handleBack() throws Exception {
        System.out.println("Returning to Main Menu!");  // Debug print
        // Switch back to main menu scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/MainMenu.fxml"));
        Scene mainMenuScene = new Scene(loader.load(), 800, 600);
        stage.setScene(mainMenuScene);

        // Pass the stage back to the main menu controller
        MainMenuController controller = loader.getController();
        controller.setStage(stage);
    }
}