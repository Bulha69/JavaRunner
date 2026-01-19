package com.example.runner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.geometry.Rectangle2D;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads level data from JSON files
 * Supports both custom JSON format and Tiled Map Editor JSON export
 */
public class LevelLoader {
    private Gson gson;

    public LevelLoader() {
        this.gson = new Gson();
    }

    /**
     * Load a level from a JSON file in resources
     * @param resourcePath Path to JSON file (e.g., "/com/example/runner/levels/level1.json")
     * @return Level object
     */
    public Level loadFromJSON(String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Could not find level file: " + resourcePath);
                return null;
            }

            Reader reader = new InputStreamReader(is);
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            // Check if it's a Tiled map or custom format
            if (jsonObject.has("type") && jsonObject.get("type").getAsString().equals("map")) {
                return loadFromTiledJSON(jsonObject);
            } else {
                return loadFromCustomJSON(jsonObject);
            }

        } catch (Exception e) {
            System.err.println("Error loading level from JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load from custom JSON format
     */
    private Level loadFromCustomJSON(JsonObject json) {
        Level level = new Level();

        // Basic properties
        if (json.has("levelId")) {
            level.setLevelId(json.get("levelId").getAsInt());
        }
        if (json.has("levelName")) {
            level.setLevelName(json.get("levelName").getAsString());
        }
        if (json.has("backgroundPath")) {
            level.setBackgroundPath(json.get("backgroundPath").getAsString());
        }
        if (json.has("levelWidth")) {
            level.setLevelWidth(json.get("levelWidth").getAsDouble());
        }

        // Load platforms
        if (json.has("platforms")) {
            JsonArray platforms = json.getAsJsonArray("platforms");
            for (int i = 0; i < platforms.size(); i++) {
                JsonObject platform = platforms.get(i).getAsJsonObject();
                double x = platform.get("x").getAsDouble();
                double y = platform.get("y").getAsDouble();
                double width = platform.get("width").getAsDouble();
                double height = platform.get("height").getAsDouble();
                level.addPlatform(x, y, width, height);
            }
        }

        // Load obstacles
        if (json.has("obstacles")) {
            JsonArray obstacles = json.getAsJsonArray("obstacles");
            for (int i = 0; i < obstacles.size(); i++) {
                JsonObject obstacle = obstacles.get(i).getAsJsonObject();
                double x = obstacle.get("x").getAsDouble();
                double y = obstacle.get("y").getAsDouble();
                double width = obstacle.get("width").getAsDouble();
                double height = obstacle.get("height").getAsDouble();
                level.addObstacle(x, y, width, height);
            }
        }

        // Load goal
        if (json.has("goal")) {
            JsonObject goal = json.getAsJsonObject("goal");
            double x = goal.get("x").getAsDouble();
            double y = goal.get("y").getAsDouble();
            double width = goal.get("width").getAsDouble();
            double height = goal.get("height").getAsDouble();
            level.setGoal(new Rectangle2D(x, y, width, height));
        }

        // Calculate level width if not specified
        if (level.getLevelWidth() == 0) {
            level.calculateLevelWidth();
        }

        return level;
    }

    /**
     * Load from Tiled Map Editor JSON export
     */
    private Level loadFromTiledJSON(JsonObject json) {
        Level level = new Level();

        // Get map dimensions
        int mapWidth = json.get("width").getAsInt();
        int mapHeight = json.get("height").getAsInt();
        int tileWidth = json.get("tilewidth").getAsInt();
        int tileHeight = json.get("tileheight").getAsInt();

        level.setLevelWidth(mapWidth * tileWidth);

        // Get layers
        JsonArray layers = json.getAsJsonArray("layers");
        for (int i = 0; i < layers.size(); i++) {
            JsonObject layer = layers.get(i).getAsJsonObject();
            String layerName = layer.get("name").getAsString().toLowerCase();

            if (layer.get("type").getAsString().equals("objectgroup")) {
                // Object layer (platforms, obstacles, goal)
                JsonArray objects = layer.getAsJsonArray("objects");

                for (int j = 0; j < objects.size(); j++) {
                    JsonObject obj = objects.get(j).getAsJsonObject();
                    double x = obj.get("x").getAsDouble();
                    double y = obj.get("y").getAsDouble();
                    double width = obj.get("width").getAsDouble();
                    double height = obj.get("height").getAsDouble();

                    String objectType = "";
                    if (obj.has("type")) {
                        objectType = obj.get("type").getAsString().toLowerCase();
                    } else if (obj.has("name")) {
                        objectType = obj.get("name").getAsString().toLowerCase();
                    }

                    // Categorize based on layer name or object type
                    if (layerName.contains("platform") || objectType.contains("platform")) {
                        level.addPlatform(x, y, width, height);
                    } else if (layerName.contains("obstacle") || objectType.contains("obstacle") ||
                            objectType.contains("spike") || objectType.contains("enemy")) {
                        level.addObstacle(x, y, width, height);
                    } else if (layerName.contains("goal") || objectType.contains("goal") ||
                            objectType.contains("flag") || objectType.contains("finish")) {
                        level.setGoal(new Rectangle2D(x, y, width, height));
                    }
                }
            }
        }

        // Get background from custom properties if available
        if (json.has("properties")) {
            JsonArray properties = json.getAsJsonArray("properties");
            for (int i = 0; i < properties.size(); i++) {
                JsonObject prop = properties.get(i).getAsJsonObject();
                if (prop.get("name").getAsString().equals("background")) {
                    level.setBackgroundPath(prop.get("value").getAsString());
                }
            }
        }

        return level;
    }

    /**
     * Save a level to JSON format (for the level editor)
     */
    public String saveToJSON(Level level) {
        JsonObject json = new JsonObject();

        json.addProperty("levelId", level.getLevelId());
        json.addProperty("levelName", level.getLevelName());
        json.addProperty("backgroundPath", level.getBackgroundPath());
        json.addProperty("levelWidth", level.getLevelWidth());

        // Save platforms
        JsonArray platforms = new JsonArray();
        for (Rectangle2D platform : level.getPlatforms()) {
            JsonObject platformObj = new JsonObject();
            platformObj.addProperty("x", platform.getMinX());
            platformObj.addProperty("y", platform.getMinY());
            platformObj.addProperty("width", platform.getWidth());
            platformObj.addProperty("height", platform.getHeight());
            platforms.add(platformObj);
        }
        json.add("platforms", platforms);

        // Save obstacles
        JsonArray obstacles = new JsonArray();
        for (Rectangle2D obstacle : level.getObstacles()) {
            JsonObject obstacleObj = new JsonObject();
            obstacleObj.addProperty("x", obstacle.getMinX());
            obstacleObj.addProperty("y", obstacle.getMinY());
            obstacleObj.addProperty("width", obstacle.getWidth());
            obstacleObj.addProperty("height", obstacle.getHeight());
            obstacles.add(obstacleObj);
        }
        json.add("obstacles", obstacles);

        // Save goal
        if (level.getGoal() != null) {
            JsonObject goal = new JsonObject();
            goal.addProperty("x", level.getGoal().getMinX());
            goal.addProperty("y", level.getGoal().getMinY());
            goal.addProperty("width", level.getGoal().getWidth());
            goal.addProperty("height", level.getGoal().getHeight());
            json.add("goal", goal);
        }

        return gson.toJson(json);
    }
}