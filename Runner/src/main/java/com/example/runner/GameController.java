package com.example.runner;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    @FXML private Canvas gameCanvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    private Stage stage; // Add stage reference

    // Game variables
    private double playerY = 300;
    private double playerX = 100;
    private double velocityY = 0;
    private double velocityX = 0;
    private boolean isJumping = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private double groundY = 350;
    private int score = 0;
    private boolean gameRunning = false;
    private boolean gameOverState = false; // Track if game is over
    private final double PLAYER_SPEED = 5;
    private final double PLAYER_SIZE = 50;
    private final double GRAVITY = 1;

    // Level variables
    private List<Rectangle2D> platforms = new ArrayList<>();
    private List<Rectangle2D> obstacles = new ArrayList<>();
    private Rectangle2D goal;
    private int currentLevel = 1;
    private final int MAX_LEVELS = 3;

    // Image variables
    private Image[] avatarImages = new Image[3];
    private Image backgroundImage;
    private Image flagImage;
    private String backgroundPath = "/com/example/runner/background.png";
    private int avatarFrame = 0;
    private int avatarFrameCounter = 0;
    private final int AVATAR_SWAP_DELAY = 10;

    // Add stage setter
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPress);
        gameCanvas.setOnKeyReleased(this::handleKeyRelease);

        // Load avatar images
        try {
            avatarImages[0] = new Image(getClass().getResourceAsStream("/com/example/runner/avatar1.png"));
            avatarImages[1] = new Image(getClass().getResourceAsStream("/com/example/runner/avatar2.png"));
            avatarImages[2] = new Image(getClass().getResourceAsStream("/com/example/runner/avatar3.png"));
        } catch (Exception e) {
            System.err.println("Error loading avatar images: " + e.getMessage());
        }

        // Load flag image
        loadFlagImage();

        // Load initial background
        loadBackground();

        // Load initial level
        loadLevel(currentLevel);
    }

    private void loadFlagImage() {
        try {
            flagImage = new Image(getClass().getResourceAsStream("/com/example/runner/flag.png"));
            System.out.println("Flag image loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading flag image: " + e.getMessage());
            System.err.println("Using yellow square as fallback");
            flagImage = null;
        }
    }

    private void loadBackground() {
        try {
            backgroundImage = new Image(getClass().getResourceAsStream(backgroundPath));
        } catch (Exception e) {
            System.err.println("Error loading background: " + e.getMessage());
        }
    }

    private void loadLevel(int level) {
        platforms.clear();
        obstacles.clear();
        goal = null;

        if (level == 1) {
            backgroundPath = "/com/example/runner/leveltest.png";
            platforms.add(new Rectangle2D(0, 350, 400, 50));
            platforms.add(new Rectangle2D(500, 350, 400, 50));
            platforms.add(new Rectangle2D(200, 250, 200, 20));
            obstacles.add(new Rectangle2D(600, 300, 50, 50));
            goal = new Rectangle2D(850, 300, 50, 50);
        } else if (level == 2) {
            backgroundPath = "/com/example/runner/background.png";
            platforms.add(new Rectangle2D(0, 350, 300, 50));
            platforms.add(new Rectangle2D(400, 350, 300, 50));
            platforms.add(new Rectangle2D(150, 200, 150, 20));
            platforms.add(new Rectangle2D(500, 150, 150, 20));
            obstacles.add(new Rectangle2D(250, 300, 50, 50));
            obstacles.add(new Rectangle2D(550, 100, 50, 50));
            goal = new Rectangle2D(750, 100, 50, 50);
        } else if (level == 3) {
            backgroundPath = "/com/example/runner/background.png";
            platforms.add(new Rectangle2D(0, 350, 200, 50));
            platforms.add(new Rectangle2D(300, 350, 200, 50));
            platforms.add(new Rectangle2D(600, 350, 200, 50));
            platforms.add(new Rectangle2D(100, 250, 100, 20));
            platforms.add(new Rectangle2D(400, 200, 100, 20));
            platforms.add(new Rectangle2D(700, 150, 100, 20));
            obstacles.add(new Rectangle2D(150, 300, 50, 50));
            obstacles.add(new Rectangle2D(450, 150, 50, 50));
            goal = new Rectangle2D(850, 100, 50, 50);
        }
        loadBackground();
    }

    public void startGame() {
        gameRunning = true;
        gameOverState = false;
        score = 0;
        playerY = 300;
        playerX = 100;
        currentLevel = 1;
        loadLevel(currentLevel);

        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        gameLoop.start();
        gameCanvas.requestFocus();
        System.out.println("Game started! Level: " + currentLevel);
    }

    private void update() {
        if (!gameRunning || gameOverState) return;

        // Horizontal movement
        velocityX = 0;
        if (isMovingLeft) velocityX = -PLAYER_SPEED;
        if (isMovingRight) velocityX = PLAYER_SPEED;

        double newPlayerX = playerX + velocityX;
        Rectangle2D playerRect = new Rectangle2D(newPlayerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
        boolean canMoveX = true;
        for (Rectangle2D platform : platforms) {
            if (playerRect.intersects(platform)) {
                canMoveX = false;
                break;
            }
        }
        if (canMoveX) {
            playerX = newPlayerX;
        }

        if (playerX < 0) playerX = 0;

        velocityY += GRAVITY;
        double newPlayerY = playerY + velocityY;
        playerRect = new Rectangle2D(playerX, newPlayerY, PLAYER_SIZE, PLAYER_SIZE);
        boolean onPlatform = false;

        for (Rectangle2D platform : platforms) {
            if (playerRect.intersects(platform)) {
                if (velocityY > 0 && playerY + PLAYER_SIZE <= platform.getMinY()) {
                    playerY = platform.getMinY() - PLAYER_SIZE;
                    velocityY = 0;
                    isJumping = false;
                    onPlatform = true;
                } else if (velocityY < 0 && playerY >= platform.getMaxY()) {
                    velocityY = 0;
                    playerY = platform.getMaxY();
                }
                break;
            }
        }

        if (!onPlatform) {
            playerY = newPlayerY;
        }

        if (playerY > gameCanvas.getHeight()) {
            gameOver();
            return;
        }

        for (Rectangle2D obstacle : obstacles) {
            if (playerRect.intersects(obstacle)) {
                gameOver();
                return;
            }
        }

        if (goal != null && playerRect.intersects(goal)) {
            levelComplete();
        }
    }

    private void render() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        } else {
            gc.setFill(Color.CYAN);
            gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        }

        gc.setFill(Color.GREEN);
        for (Rectangle2D platform : platforms) {
            gc.fillRect(platform.getMinX(), platform.getMinY(), platform.getWidth(), platform.getHeight());
        }

        gc.setFill(Color.RED);
        for (Rectangle2D obstacle : obstacles) {
            gc.fillRect(obstacle.getMinX(), obstacle.getMinY(), obstacle.getWidth(), obstacle.getHeight());
        }

        if (goal != null) {
            if (flagImage != null) {
                gc.drawImage(flagImage, goal.getMinX(), goal.getMinY(), goal.getWidth(), goal.getHeight());
            } else {
                gc.setFill(Color.YELLOW);
                gc.fillRect(goal.getMinX(), goal.getMinY(), goal.getWidth(), goal.getHeight());
            }
        }

        if (avatarImages[avatarFrame] != null) {
            gc.drawImage(avatarImages[avatarFrame], playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
        }

        avatarFrameCounter++;
        if (avatarFrameCounter >= AVATAR_SWAP_DELAY) {
            avatarFrameCounter = 0;
            avatarFrame = (avatarFrame + 1) % 3;
        }

        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Times New Roman", 20));
        gc.fillText("Score: " + score + " | Level: " + currentLevel, 10, 30);
    }

    private void gameOver() {
        gameRunning = false;
        gameOverState = true;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        System.out.println("Game Over! Score: " + score);

        // Switch to Game Over screen
        showGameOverScreen();
    }

    private void levelComplete() {
        gameRunning = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        score += 100;
        if (currentLevel < MAX_LEVELS) {
            currentLevel++;
            loadLevel(currentLevel);
            // Continue to next level
            gameRunning = true;
            playerY = 300;
            playerX = 100;
            gameLoop.start();
        } else {
            // Game completed
            System.out.println("All Levels Complete! Final Score: " + score);
            showGameOverScreen();
        }
    }

    private void showGameOverScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/GameOver.fxml"));
            Scene gameOverScene = new Scene(loader.load(), 800, 600);
            stage.setScene(gameOverScene);

            GameOverController controller = loader.getController();
            controller.setStage(stage);
            controller.setFinalScore(score);
            controller.setGameController(this); // Pass reference for restarting
        } catch (Exception e) {
            System.err.println("Error loading Game Over screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleKeyPress(KeyEvent e) {
        if (!gameRunning) return;

        if (e.getCode() == KeyCode.W && !isJumping) {
            isJumping = true;
            velocityY = -20;
        } else if (e.getCode() == KeyCode.A) {
            isMovingLeft = true;
        } else if (e.getCode() == KeyCode.D) {
            isMovingRight = true;
        } else if (e.getCode() == KeyCode.ESCAPE) {
            // Return to main menu
            returnToMainMenu();
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        if (!gameRunning) return;

        if (e.getCode() == KeyCode.A) {
            isMovingLeft = false;
        } else if (e.getCode() == KeyCode.D) {
            isMovingRight = false;
        }
    }

    private void returnToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/runner/MainMenu.fxml"));
            Scene menuScene = new Scene(loader.load(), 800, 600);
            stage.setScene(menuScene);

            MainMenuController controller = loader.getController();
            controller.setStage(stage);
        } catch (Exception e) {
            System.err.println("Error returning to main menu: " + e.getMessage());
        }
    }
}
