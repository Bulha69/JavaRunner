package com.example.runner;

import javafx.geometry.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game level with platforms, obstacles, and goals
 */
public class Level {
    private int levelId;
    private String backgroundPath;
    private List<Rectangle2D> platforms;
    private List<Rectangle2D> obstacles;
    private Rectangle2D goal;
    private double levelWidth; // Total width of the level for camera bounds
    private String levelName;

    public Level() {
        this.platforms = new ArrayList<>();
        this.obstacles = new ArrayList<>();
    }

    public Level(int levelId, String backgroundPath, String levelName) {
        this.levelId = levelId;
        this.backgroundPath = backgroundPath;
        this.levelName = levelName;
        this.platforms = new ArrayList<>();
        this.obstacles = new ArrayList<>();
    }

    // Getters and setters
    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public List<Rectangle2D> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<Rectangle2D> platforms) {
        this.platforms = platforms;
    }

    public List<Rectangle2D> getObstacles() {
        return obstacles;
    }

    public void setObstacles(List<Rectangle2D> obstacles) {
        this.obstacles = obstacles;
    }

    public Rectangle2D getGoal() {
        return goal;
    }

    public void setGoal(Rectangle2D goal) {
        this.goal = goal;
    }

    public double getLevelWidth() {
        return levelWidth;
    }

    public void setLevelWidth(double levelWidth) {
        this.levelWidth = levelWidth;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    // Helper methods
    public void addPlatform(double x, double y, double width, double height) {
        platforms.add(new Rectangle2D(x, y, width, height));
    }

    public void addObstacle(double x, double y, double width, double height) {
        obstacles.add(new Rectangle2D(x, y, width, height));
    }

    public void calculateLevelWidth() {
        double maxX = 0;
        
        for (Rectangle2D platform : platforms) {
            maxX = Math.max(maxX, platform.getMaxX());
        }
        
        for (Rectangle2D obstacle : obstacles) {
            maxX = Math.max(maxX, obstacle.getMaxX());
        }
        
        if (goal != null) {
            maxX = Math.max(maxX, goal.getMaxX());
        }
        
        // Add some padding
        this.levelWidth = maxX + 200;
    }
}
