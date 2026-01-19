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
    private Stage stage;

    // Game variables
    private double playerY = 300;
    private double playerX = 100;
    private double velocityY = 0;
    private double velocityX = 0;
    private boolean isJumping = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private int score = 0;
    private boolean gameRunning = false;
    private boolean gameOverState = false;
    private final double PLAYER_SPEED = 5;
    private final double PLAYER_SIZE = 50;
    private final double GRAVITY = 1;

    // Camera variables (NEW!)
    private double cameraX = 0;
    private final double CAMERA_OFFSET = 300; // Keep player this far from left edge
    private final double CAMERA_SMOOTHNESS = 0.1; // Camera follow smoothness (0-1)
    private double targetCameraX = 0;

    // Level variables
    private Level currentLevelData;
    private int currentLevelNumber = 1;
    private final int MAX_LEVELS = 3;
    private LevelLoader levelLoader;

    // Image variables
    private Image[] avatarImages = new Image[3];
    private Image backgroundImage;
    private Image flagImage;
    private int avatarFrame = 0;
    private int avatarFrameCounter = 0;
    private final int AVATAR_SWAP_DELAY = 10;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPress);
        gameCanvas.setOnKeyReleased(this::handleKeyRelease);

        // Initialize level loader
        levelLoader = new LevelLoader();

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
    }

    private void loadFlagImage() {
        try {
            flagImage = new Image(getClass().getResourceAsStream("/com/example/runner/flag.png"));
            System.out.println("Flag image loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading flag image: " + e.getMessage());
            flagImage = null;
        }
    }

    private void loadBackground(String path) {
        try {
            backgroundImage = new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Error loading background: " + e.getMessage());
            backgroundImage = null;
        }
    }

    /**
     * Load a level from JSON file
     */
    private void loadLevel(int levelNumber) {
        // Try to load from JSON first
        String jsonPath = "/com/example/runner/levels/level" + levelNumber + ".json";
        Level level = levelLoader.loadFromJSON(jsonPath);

        if (level == null) {
            // Fallback to hardcoded level if JSON not found
            System.out.println("JSON not found, using hardcoded level " + levelNumber);
            level = createHardcodedLevel(levelNumber);
        } else {
            System.out.println("Loaded level " + levelNumber + " from JSON");
        }

        currentLevelData = level;

        // Load background
        if (level.getBackgroundPath() != null) {
            loadBackground(level.getBackgroundPath());
        }

        // Reset camera
        cameraX = 0;
        targetCameraX = 0;
    }

    /**
     * Fallback hardcoded levels (same as before)
     */
    private Level createHardcodedLevel(int levelNumber) {
        Level level = new Level(levelNumber, "/com/example/runner/background.png", "Level " + levelNumber);

        if (levelNumber == 1) {
            level.setBackgroundPath("/com/example/runner/leveltest.png");
            level.addPlatform(0, 350, 400, 50);
            level.addPlatform(500, 350, 400, 50);
            level.addPlatform(200, 250, 200, 20);
            level.addObstacle(600, 300, 50, 50);
            level.setGoal(new Rectangle2D(850, 300, 50, 50));
        } else if (levelNumber == 2) {
            level.addPlatform(0, 350, 300, 50);
            level.addPlatform(400, 350, 300, 50);
            level.addPlatform(150, 200, 150, 20);
            level.addPlatform(500, 150, 150, 20);
            level.addObstacle(250, 300, 50, 50);
            level.addObstacle(550, 100, 50, 50);
            level.setGoal(new Rectangle2D(750, 100, 50, 50));
        } else if (levelNumber == 3) {
            level.addPlatform(0, 350, 200, 50);
            level.addPlatform(300, 350, 200, 50);
            level.addPlatform(600, 350, 200, 50);
            level.addPlatform(100, 250, 100, 20);
            level.addPlatform(400, 200, 100, 20);
            level.addPlatform(700, 150, 100, 20);
            level.addObstacle(150, 300, 50, 50);
            level.addObstacle(450, 150, 50, 50);
            level.setGoal(new Rectangle2D(850, 100, 50, 50));
        }

        level.calculateLevelWidth();
        return level;
    }

    public void startGame() {
        gameRunning = true;
        gameOverState = false;
        score = 0;
        playerY = 300;
        playerX = 100;
        currentLevelNumber = 1;
        cameraX = 0;
        targetCameraX = 0;

        loadLevel(currentLevelNumber);

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
        System.out.println("Game started! Level: " + currentLevelNumber);
    }

    private void update() {
        if (!gameRunning || gameOverState || currentLevelData == null) return;

        // Horizontal movement
        velocityX = 0;
        if (isMovingLeft) velocityX = -PLAYER_SPEED;
        if (isMovingRight) velocityX = PLAYER_SPEED;

        double newPlayerX = playerX + velocityX;
        Rectangle2D playerRect = new Rectangle2D(newPlayerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
        boolean canMoveX = true;

        for (Rectangle2D platform : currentLevelData.getPlatforms()) {
            if (playerRect.intersects(platform)) {
                canMoveX = false;
                break;
            }
        }

        if (canMoveX) {
            playerX = newPlayerX;
        }

        // Keep player within level bounds
        if (playerX < 0) playerX = 0;
        if (playerX > currentLevelData.getLevelWidth() - PLAYER_SIZE) {
            playerX = currentLevelData.getLevelWidth() - PLAYER_SIZE;
        }

        // Update camera to follow player (smooth scrolling)
        targetCameraX = playerX - CAMERA_OFFSET;
        targetCameraX = Math.max(0, targetCameraX); // Don't go below 0

        // Don't scroll past the end of the level
        double maxCameraX = currentLevelData.getLevelWidth() - gameCanvas.getWidth();
        if (maxCameraX < 0) maxCameraX = 0;
        targetCameraX = Math.min(targetCameraX, maxCameraX);

        // Smooth camera follow
        cameraX += (targetCameraX - cameraX) * CAMERA_SMOOTHNESS;

        // Gravity and vertical movement
        velocityY += GRAVITY;
        double newPlayerY = playerY + velocityY;
        playerRect = new Rectangle2D(playerX, newPlayerY, PLAYER_SIZE, PLAYER_SIZE);
        boolean onPlatform = false;

        for (Rectangle2D platform : currentLevelData.getPlatforms()) {
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

        // Fall off the screen
        if (playerY > gameCanvas.getHeight()) {
            gameOver();
            return;
        }

        // Check collision with obstacles
        for (Rectangle2D obstacle : currentLevelData.getObstacles()) {
            if (playerRect.intersects(obstacle)) {
                gameOver();
                return;
            }
        }

        // Check if reached goal
        if (currentLevelData.getGoal() != null && playerRect.intersects(currentLevelData.getGoal())) {
            levelComplete();
        }
    }

    private void render() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        if (currentLevelData == null) return;

        // Draw background with parallax effect (scrolls slower than foreground)
        if (backgroundImage != null) {
            double bgScrollFactor = 0.5; // Background scrolls at half speed
            double bgX = -cameraX * bgScrollFactor;

            // Tile the background if needed
            int numTiles = (int) Math.ceil(gameCanvas.getWidth() / backgroundImage.getWidth()) + 2;
            for (int i = 0; i < numTiles; i++) {
                double tileX = (bgX % backgroundImage.getWidth()) + (i * backgroundImage.getWidth())
                        - backgroundImage.getWidth();
                gc.drawImage(backgroundImage, tileX, 0, backgroundImage.getWidth(), gameCanvas.getHeight());
            }
        } else {
            gc.setFill(Color.CYAN);
            gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        }

        // Draw platforms (with camera offset)
        gc.setFill(Color.GREEN);
        for (Rectangle2D platform : currentLevelData.getPlatforms()) {
            gc.fillRect(platform.getMinX() - cameraX, platform.getMinY(),
                    platform.getWidth(), platform.getHeight());
        }

        // Draw obstacles (with camera offset)
        gc.setFill(Color.RED);
        for (Rectangle2D obstacle : currentLevelData.getObstacles()) {
            gc.fillRect(obstacle.getMinX() - cameraX, obstacle.getMinY(),
                    obstacle.getWidth(), obstacle.getHeight());
        }

        // Draw goal (with camera offset)
        if (currentLevelData.getGoal() != null) {
            Rectangle2D goal = currentLevelData.getGoal();
            if (flagImage != null) {
                gc.drawImage(flagImage, goal.getMinX() - cameraX, goal.getMinY(),
                        goal.getWidth(), goal.getHeight());
            } else {
                gc.setFill(Color.YELLOW);
                gc.fillRect(goal.getMinX() - cameraX, goal.getMinY(),
                        goal.getWidth(), goal.getHeight());
            }
        }

        // Draw player (relative to camera)
        if (avatarImages[avatarFrame] != null) {
            gc.drawImage(avatarImages[avatarFrame], playerX - cameraX, playerY,
                    PLAYER_SIZE, PLAYER_SIZE);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillRect(playerX - cameraX, playerY, PLAYER_SIZE, PLAYER_SIZE);
        }

        // Animate avatar
        avatarFrameCounter++;
        if (avatarFrameCounter >= AVATAR_SWAP_DELAY) {
            avatarFrameCounter = 0;
            avatarFrame = (avatarFrame + 1) % 3;
        }

        // Draw HUD (fixed position, not affected by camera)
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Times New Roman", 20));
        gc.fillText("Score: " + score + " | Level: " + currentLevelNumber, 10, 30);

        // Optional: Draw player X position for debugging
        // gc.fillText("X: " + (int)playerX + " Camera: " + (int)cameraX, 10, 50);
    }

    private void gameOver() {
        gameRunning = false;
        gameOverState = true;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        System.out.println("Game Over! Score: " + score);
        showGameOverScreen();
    }

    private void levelComplete() {
        gameRunning = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        score += 100;

        if (currentLevelNumber < MAX_LEVELS) {
            currentLevelNumber++;
            loadLevel(currentLevelNumber);
            // Continue to next level
            gameRunning = true;
            playerY = 300;
            playerX = 100;
            cameraX = 0;
            targetCameraX = 0;
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
            controller.setGameController(this);
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