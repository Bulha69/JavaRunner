package com.example.runner;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Visual Level Editor for creating game levels

public class LevelEditorController {
    @FXML private Canvas editorCanvas;
    @FXML private ScrollPane canvasScrollPane;
    @FXML private TextField levelIdField;
    @FXML private TextField levelNameField;
    @FXML private ToggleGroup toolGroup;
    @FXML private RadioButton selectTool;
    @FXML private RadioButton platformTool;
    @FXML private RadioButton obstacleTool;
    @FXML private RadioButton goalTool;
    @FXML private Label selectionInfoLabel;
    @FXML private TextField objXField;
    @FXML private TextField objYField;
    @FXML private TextField objWidthField;
    @FXML private TextField objHeightField;
    @FXML private Button applyChangesBtn;
    @FXML private CheckBox showGridCheckbox;
    @FXML private TextField gridSizeField;
    @FXML private TextField backgroundPathField;
    @FXML private Label statusLabel;
    @FXML private Label mousePositionLabel;
    @FXML private Label objectCountLabel;

    private GraphicsContext gc;
    private Level currentLevel;
    private LevelLoader levelLoader;

    // Editor state
    private List<EditorObject> editorObjects;
    private EditorObject selectedObject;
    private EditorObject draggedObject;
    private double dragStartX, dragStartY;
    private double dragOffsetX, dragOffsetY;
    private boolean isDragging = false;
    private boolean isDrawingNew = false;
    private double drawStartX, drawStartY;

    private int gridSize = 10;
    private boolean showGrid = true;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        gc = editorCanvas.getGraphicsContext2D();
        editorObjects = new ArrayList<>();
        levelLoader = new LevelLoader();
        currentLevel = new Level(1, "/com/example/runner/background.png", "New Level");

        // Setup mouse handlers
        editorCanvas.setOnMousePressed(this::handleMousePressed);
        editorCanvas.setOnMouseDragged(this::handleMouseDragged);
        editorCanvas.setOnMouseReleased(this::handleMouseReleased);
        editorCanvas.setOnMouseMoved(this::handleMouseMoved);

        render();
        updateObjectCount();
    }

    private void handleMousePressed(MouseEvent e) {
        double x = snapToGrid(e.getX());
        double y = snapToGrid(e.getY());

        if (selectTool.isSelected()) {
            // Check if clicking on existing object
            selectedObject = getObjectAt(x, y);
            if (selectedObject != null) {
                updateSelectionInfo();
                draggedObject = selectedObject;
                dragStartX = selectedObject.x;
                dragStartY = selectedObject.y;
                dragOffsetX = x - selectedObject.x;
                dragOffsetY = y - selectedObject.y;
            } else {
                clearSelection();
            }
        } else {
            // Start drawing new object
            isDrawingNew = true;
            drawStartX = x;
            drawStartY = y;
        }

        render();
    }

    private void handleMouseDragged(MouseEvent e) {
        double x = snapToGrid(e.getX());
        double y = snapToGrid(e.getY());

        if (selectTool.isSelected() && draggedObject != null) {
            // Move selected object
            isDragging = true;
            draggedObject.x = x - dragOffsetX;
            draggedObject.y = y - dragOffsetY;
            updateSelectionInfo();
        } else if (isDrawingNew) {
            // Preview new object size
        }

        render();
    }

    private void handleMouseReleased(MouseEvent e) {
        double x = snapToGrid(e.getX());
        double y = snapToGrid(e.getY());

        if (isDrawingNew) {
            // Create new object
            double objX = Math.min(drawStartX, x);
            double objY = Math.min(drawStartY, y);
            double objWidth = Math.abs(x - drawStartX);
            double objHeight = Math.abs(y - drawStartY);

            if (objWidth >= 10 && objHeight >= 10) {
                EditorObject newObj = null;

                if (platformTool.isSelected()) {
                    newObj = new EditorObject(objX, objY, objWidth, objHeight, ObjectType.PLATFORM);
                } else if (obstacleTool.isSelected()) {
                    newObj = new EditorObject(objX, objY, objWidth, objHeight, ObjectType.OBSTACLE);
                } else if (goalTool.isSelected()) {
                    // Remove existing goal if any
                    editorObjects.removeIf(obj -> obj.type == ObjectType.GOAL);
                    newObj = new EditorObject(objX, objY, objWidth, objHeight, ObjectType.GOAL);
                }

                if (newObj != null) {
                    editorObjects.add(newObj);
                    statusLabel.setText("Created " + newObj.type);
                    updateObjectCount();
                }
            }
            isDrawingNew = false;
        }

        if (isDragging) {
            isDragging = false;
            statusLabel.setText("Moved object to (" + (int)draggedObject.x + ", " + (int)draggedObject.y + ")");
        }

        draggedObject = null;
        render();
    }

    private void handleMouseMoved(MouseEvent e) {
        mousePositionLabel.setText(String.format("Mouse: (%.0f, %.0f)", e.getX(), e.getY()));
    }

    private void render() {
        // Clear canvas
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, editorCanvas.getWidth(), editorCanvas.getHeight());

        // Draw grid
        if (showGrid) {
            gc.setStroke(Color.rgb(200, 200, 200));
            gc.setLineWidth(1);

            for (double x = 0; x < editorCanvas.getWidth(); x += gridSize) {
                gc.strokeLine(x, 0, x, editorCanvas.getHeight());
            }
            for (double y = 0; y < editorCanvas.getHeight(); y += gridSize) {
                gc.strokeLine(0, y, editorCanvas.getWidth(), y);
            }
        }

        // Draw all objects
        for (EditorObject obj : editorObjects) {
            drawObject(obj, obj == selectedObject);
        }

        // Draw object being created
        if (isDrawingNew && drawStartX > 0) {
            gc.setGlobalAlpha(0.5);
            double x = Math.min(drawStartX, snapToGrid(editorCanvas.getWidth()));
            double y = Math.min(drawStartY, snapToGrid(editorCanvas.getHeight()));
            double width = Math.abs(snapToGrid(editorCanvas.getWidth()) - drawStartX);
            double height = Math.abs(snapToGrid(editorCanvas.getHeight()) - drawStartY);

            if (platformTool.isSelected()) {
                gc.setFill(Color.GREEN);
            } else if (obstacleTool.isSelected()) {
                gc.setFill(Color.RED);
            } else if (goalTool.isSelected()) {
                gc.setFill(Color.GOLD);
            }

            gc.fillRect(x, y, width, height);
            gc.setGlobalAlpha(1.0);
        }
    }

    private void drawObject(EditorObject obj, boolean selected) {
        // Draw fill
        switch (obj.type) {
            case PLATFORM:
                gc.setFill(Color.GREEN);
                break;
            case OBSTACLE:
                gc.setFill(Color.RED);
                break;
            case GOAL:
                gc.setFill(Color.GOLD);
                break;
        }
        gc.fillRect(obj.x, obj.y, obj.width, obj.height);

        // Draw outline (thicker if selected)
        gc.setStroke(selected ? Color.BLUE : Color.BLACK);
        gc.setLineWidth(selected ? 3 : 1);
        gc.strokeRect(obj.x, obj.y, obj.width, obj.height);

        // Draw label
        gc.setFill(Color.BLACK);
        gc.fillText(obj.type.toString(), obj.x + 5, obj.y + 15);
    }

    private EditorObject getObjectAt(double x, double y) {
        // Check in reverse order (top objects first)
        for (int i = editorObjects.size() - 1; i >= 0; i--) {
            EditorObject obj = editorObjects.get(i);
            if (x >= obj.x && x <= obj.x + obj.width &&
                    y >= obj.y && y <= obj.y + obj.height) {
                return obj;
            }
        }
        return null;
    }

    private double snapToGrid(double value) {
        try {
            gridSize = Integer.parseInt(gridSizeField.getText());
        } catch (NumberFormatException e) {
            gridSize = 10;
        }
        return Math.round(value / gridSize) * gridSize;
    }

    private void updateSelectionInfo() {
        if (selectedObject != null) {
            selectionInfoLabel.setText("Selected: " + selectedObject.type);
            objXField.setText(String.valueOf((int)selectedObject.x));
            objYField.setText(String.valueOf((int)selectedObject.y));
            objWidthField.setText(String.valueOf((int)selectedObject.width));
            objHeightField.setText(String.valueOf((int)selectedObject.height));
            objXField.setDisable(false);
            objYField.setDisable(false);
            objWidthField.setDisable(false);
            objHeightField.setDisable(false);
            applyChangesBtn.setDisable(false);
        }
    }

    private void clearSelection() {
        selectedObject = null;
        selectionInfoLabel.setText("No object selected");
        objXField.clear();
        objYField.clear();
        objWidthField.clear();
        objHeightField.clear();
        objXField.setDisable(true);
        objYField.setDisable(true);
        objWidthField.setDisable(true);
        objHeightField.setDisable(true);
        applyChangesBtn.setDisable(true);
    }

    private void updateObjectCount() {
        int platforms = (int) editorObjects.stream().filter(o -> o.type == ObjectType.PLATFORM).count();
        int obstacles = (int) editorObjects.stream().filter(o -> o.type == ObjectType.OBSTACLE).count();
        int goals = (int) editorObjects.stream().filter(o -> o.type == ObjectType.GOAL).count();
        objectCountLabel.setText(String.format("Platforms: %d, Obstacles: %d, Goals: %d",
                platforms, obstacles, goals));
    }

    @FXML
    private void applyObjectChanges() {
        if (selectedObject != null) {
            try {
                selectedObject.x = Double.parseDouble(objXField.getText());
                selectedObject.y = Double.parseDouble(objYField.getText());
                selectedObject.width = Double.parseDouble(objWidthField.getText());
                selectedObject.height = Double.parseDouble(objHeightField.getText());
                render();
                statusLabel.setText("Object updated");
            } catch (NumberFormatException e) {
                statusLabel.setText("Invalid number format");
            }
        }
    }

    @FXML
    private void toggleGrid() {
        showGrid = showGridCheckbox.isSelected();
        render();
    }

    @FXML
    private void newLevel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("New Level");
        alert.setHeaderText("Create new level?");
        alert.setContentText("This will clear the current level. Continue?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            editorObjects.clear();
            clearSelection();
            levelIdField.setText("1");
            levelNameField.setText("New Level");
            render();
            updateObjectCount();
            statusLabel.setText("New level created");
        }
    }

    @FXML
    private void saveLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Level");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        fileChooser.setInitialFileName("level" + levelIdField.getText() + ".json");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            Level level = editorToLevel();
            String json = levelLoader.saveToJSON(level);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
                statusLabel.setText("Level saved to: " + file.getName());
            } catch (Exception e) {
                statusLabel.setText("Error saving: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exportLevel() {
        statusLabel.setText("Copy JSON below and save to src/main/resources/com/example/runner/levels/");

        Level level = editorToLevel();
        String json = levelLoader.saveToJSON(level);

        TextArea textArea = new TextArea(json);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Level");
        alert.setHeaderText("Level JSON");
        alert.getDialogPane().setContent(textArea);
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(600, 400);
        alert.showAndWait();
    }

    @FXML
    private void openLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Level");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                // Load level from file path (not resource path)
                // This is a simplified version - you'd need to adjust LevelLoader
                statusLabel.setText("Level loading from file not fully implemented yet");
            } catch (Exception e) {
                statusLabel.setText("Error loading: " + e.getMessage());
            }
        }
    }

    @FXML
    private void clearAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear All");
        alert.setHeaderText("Clear all objects?");
        alert.setContentText("This cannot be undone!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            editorObjects.clear();
            clearSelection();
            render();
            updateObjectCount();
            statusLabel.setText("All objects cleared");
        }
    }

    @FXML
    private void deleteSelected() {
        if (selectedObject != null) {
            editorObjects.remove(selectedObject);
            clearSelection();
            render();
            updateObjectCount();
            statusLabel.setText("Object deleted");
        }
    }

    @FXML
    private void testLevel() {
        statusLabel.setText("Test level feature - launch game with this level");
        // You could implement launching the game with this level here
    }

    @FXML
    private void exitEditor() {
        stage.close();
    }

    @FXML
    private void loadBackgroundImage() {
        statusLabel.setText("Background image loading not implemented - use path in field");
    }

    private Level editorToLevel() {
        try {
            int levelId = Integer.parseInt(levelIdField.getText());
            String levelName = levelNameField.getText();
            String bgPath = backgroundPathField.getText();
            if (bgPath.isEmpty()) {
                bgPath = "/com/example/runner/background.png";
            }

            Level level = new Level(levelId, bgPath, levelName);

            for (EditorObject obj : editorObjects) {
                switch (obj.type) {
                    case PLATFORM:
                        level.addPlatform(obj.x, obj.y, obj.width, obj.height);
                        break;
                    case OBSTACLE:
                        level.addObstacle(obj.x, obj.y, obj.width, obj.height);
                        break;
                    case GOAL:
                        level.setGoal(new Rectangle2D(obj.x, obj.y, obj.width, obj.height));
                        break;
                }
            }

            level.calculateLevelWidth();
            return level;
        } catch (NumberFormatException e) {
            return new Level(1, "/com/example/runner/background.png", "Error");
        }
    }

    // Helper class for editor objects
    private static class EditorObject {
        double x, y, width, height;
        ObjectType type;

        EditorObject(double x, double y, double width, double height, ObjectType type) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
        }
    }

    private enum ObjectType {
        PLATFORM, OBSTACLE, GOAL
    }
}