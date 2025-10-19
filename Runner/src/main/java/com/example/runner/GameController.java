package com.example.runner;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class GameController {
    @FXML private Canvas gameCanvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    // Game variables
    private double playerY = 300;
    private double playerX = 100;
    private double velocityY = 0;
    private boolean isJumping = false;
    private double groundY = 350;
    private double obstacleX = 800;
    private double obstacleWidth = 50, obstacleHeight = 50;
    private int score = 0;
    private boolean gameRunning = false;

    @FXML
    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPress);
    }

    public void startGame() {
        gameRunning = true;
        score = 0;
        playerY = groundY - 50;
        obstacleX = 800;
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        gameLoop.start();
        gameCanvas.requestFocus();
        System.out.println("Game started!");
    }

    private void update() {
        if (!gameRunning) return;
        if (isJumping) {
            velocityY += 1;
            playerY += velocityY;
            if (playerY >= groundY - 50) {
                playerY = groundY - 50;
                isJumping = false;
                velocityY = 0;
            }
        }
        obstacleX -= 5;
        if (obstacleX < -obstacleWidth) {
            obstacleX = 800;
            score += 10;
        }
        if (playerX + 50 > obstacleX && playerX < obstacleX + obstacleWidth &&
                playerY + 50 > groundY - obstacleHeight) {
            gameOver();
        }
    }

    private void render() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setFill(Color.CYAN);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setFill(Color.BLUE);
        gc.fillRect(playerX, playerY, 50, 50);
        gc.setFill(Color.RED);
        gc.fillRect(obstacleX, groundY - obstacleHeight, obstacleWidth, obstacleHeight);
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", 20));
        gc.fillText("Score: " + score, 10, 30);
    }

    private void gameOver() {
        gameRunning = false;
        gameLoop.stop();
        System.out.println("Game Over! Score: " + score);
    }

    private void handleKeyPress(KeyEvent e) {
        if (e.getCode() == KeyCode.SPACE && !isJumping) {
            isJumping = true;
            velocityY = -15;
        } else if (e.getCode() == KeyCode.ESCAPE) {
            gameOver();
        }
    }
}