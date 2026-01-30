package org.mp.frontend26;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mp.frontend26.dto.AWSModule;

import java.sql.SQLException;
import java.util.List;

public class MpGuiController {

    // ---------- DB ----------
    private MpDatabase db;
    private List<AWSModule> modules;
    private int index = 0;
    private boolean createMode = false;

    // ---------- TOP ----------
    @FXML private ToggleButton connectToggle;
    @FXML private Label statusLabel;

    // ---------- FIELDS ----------
    @FXML private TextField moduleIdField;
    @FXML private TextField moduleTitleField;
    @FXML private TextArea moduleSummaryArea;
    @FXML private TextArea learningOutcomesArea;
    @FXML private TextArea salesPitchArea;
    @FXML private TextField imageUriField;
    @FXML private TextField timeRequirementField;
    @FXML private TextField difficultyLevelField;

    // ---------- NAV BUTTONS ----------
    @FXML private Button firstButton, backThreeButton, backOneButton, forwardOneButton, forwardThreeButton, lastButton;
    @FXML private Button createButton, updateButton, deleteButton, reloadButton;

    @FXML
    private void onMenuNew() {
        System.out.println("Menu: New");
        // Call onCreate or add new logic
    }

    @FXML
    private void onMenuOpen() {
        System.out.println("Menu: Open");
    }

    @FXML
    private void onMenuSave() {
        System.out.println("Menu: Save");
        // Call onUpdate
    }

    @FXML
    private void onMenuExit() {
        Platform.exit();
    }

    @FXML
    private void onMenuDocs() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Documentation");
        alert.setHeaderText("AWS Module Manager");
        alert.setContentText("Visit AWS documentation for more information.");
        alert.showAndWait();
    }

    @FXML
    private void onMenuAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("AWS Learning Module Manager v1.0");
        alert.setContentText("Manage and configure AWS learning modules.");
        alert.showAndWait();
    }

    // ---------- INIT ----------
    @FXML
    private void initialize() {
        db = new MpDatabase();
        setDisconnectedState();
    }

    // ---------- CONNECT ----------
    @FXML
    private void onConnectToggle() {
        try {
            if (connectToggle.isSelected()) {
                db.connect();
                statusLabel.setText("Connected");
                connectToggle.setText("Disconnect");
                load();
            } else {
                db.close();
                setDisconnectedState();
            }
        } catch (Exception e) {
            statusLabel.setText("DB Error: " + e.getMessage());
            connectToggle.setSelected(false);
        }
    }

    private void setDisconnectedState() {
        statusLabel.setText("Disconnected");
        connectToggle.setText("Connect");
        clearFields();
        modules = null;
        index = 0;
        updateNavButtons();
    }

    // ---------- LOAD ----------
    private void load() {
        try {
            modules = db.getAllModules();
            index = 0;
            createMode = false;

            if (modules.isEmpty()) {
                clearFields();
                statusLabel.setText("No modules found");
            } else {
                showCurrent();
                statusLabel.setText("Modules loaded: " + modules.size());
            }
            updateNavButtons();
        } catch (SQLException e) {
            modules = null;
            clearFields();
            statusLabel.setText("Failed to load modules");
        }
    }

    // ---------- DISPLAY ----------
    private void showCurrent() {
        if (modules == null || modules.isEmpty()) {
            clearFields();
            updateNavButtons();
            return;
        }

        AWSModule m = modules.get(index);

        moduleIdField.setText(String.valueOf(m.getModuleId()));
        moduleTitleField.setText(m.getModuleTitle());
        moduleSummaryArea.setText(m.getModuleSummary());
        learningOutcomesArea.setText(m.getLearningOutcomes());
        salesPitchArea.setText(m.getSalesPitch());
        imageUriField.setText(m.getImageUri());
        timeRequirementField.setText(String.valueOf(m.getTimeRequirement()));
        difficultyLevelField.setText(String.valueOf(m.getDifficultyLevel()));

        updateNavButtons();
    }

    private void clearFields() {
        moduleIdField.clear();
        moduleTitleField.clear();
        moduleSummaryArea.clear();
        learningOutcomesArea.clear();
        salesPitchArea.clear();
        imageUriField.clear();
        timeRequirementField.clear();
        difficultyLevelField.clear();
    }

    // ---------- NAVIGATION ----------
    @FXML private void onFirst() { ifValidIndex(0); }
    @FXML private void onLast() { ifValidIndex(modules.size() - 1); }
    @FXML private void onBackOne() { ifValidIndex(index - 1); }
    @FXML private void onForwardOne() { ifValidIndex(index + 1); }
    @FXML private void onBackThree() { ifValidIndex(index - 3); }
    @FXML private void onForwardThree() { ifValidIndex(index + 3); }

    private void ifValidIndex(int newIndex) {
        if (modules == null || modules.isEmpty()) return;
        index = Math.max(0, Math.min(modules.size() - 1, newIndex));
        showCurrent();
    }

    private void updateNavButtons() {
        boolean hasModules = modules != null && !modules.isEmpty();
        firstButton.setDisable(!hasModules || index == 0);
        backThreeButton.setDisable(!hasModules || index == 0);
        backOneButton.setDisable(!hasModules || index == 0);
        forwardOneButton.setDisable(!hasModules || index == modules.size() - 1);
        forwardThreeButton.setDisable(!hasModules || index == modules.size() - 1);
        lastButton.setDisable(!hasModules || index == modules.size() - 1);

        createButton.setDisable(!connectToggle.isSelected());
        updateButton.setDisable(!hasModules);
        deleteButton.setDisable(!hasModules);
        reloadButton.setDisable(!connectToggle.isSelected());
    }

    // ---------- CREATE ----------
    @FXML
    private void onCreate() {
        if (!createMode) {
            clearFields();
            createMode = true;
            statusLabel.setText("Create mode");
            return;
        }

        try {
            AWSModule m = readFromFields();
            db.createModule(m);
            load();
            statusLabel.setText("Module created successfully");
            createMode = false;
        } catch (Exception e) {
            statusLabel.setText("Failed to create: " + e.getMessage());
        }
    }

    // ---------- UPDATE ----------
    @FXML
    private void onUpdate() {
        if (modules == null || modules.isEmpty()) return;

        try {
            AWSModule m = readFromFields();
            m.setModuleId(Integer.parseInt(moduleIdField.getText()));
            db.updateModule(m);
            load();
            statusLabel.setText("Module updated");
        } catch (Exception e) {
            statusLabel.setText("Failed to update: " + e.getMessage());
        }
    }

    // ---------- DELETE ----------
    @FXML
    private void onDelete() {
        if (modules == null || modules.isEmpty()) return;

        try {
            int id = Integer.parseInt(moduleIdField.getText());
            db.deleteModule(id);
            load();
            statusLabel.setText("Module deleted");
        } catch (Exception e) {
            statusLabel.setText("Failed to delete: " + e.getMessage());
        }
    }

    // ---------- RELOAD ----------
    @FXML
    private void onReload() { load(); }

    // ---------- HELPERS ----------
    private AWSModule readFromFields() throws IllegalArgumentException {
        AWSModule m = new AWSModule();

        m.setModuleTitle(moduleTitleField.getText());
        m.setModuleSummary(moduleSummaryArea.getText());
        m.setLearningOutcomes(learningOutcomesArea.getText());
        m.setSalesPitch(salesPitchArea.getText());
        m.setImageUri(imageUriField.getText());

        // --- number validation ---
        try {
            m.setTimeRequirement(Float.parseFloat(timeRequirementField.getText()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Time Requirement must be a number");
        }

        try {
            m.setDifficultyLevel(Double.parseDouble(difficultyLevelField.getText()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Difficulty Level must be a number");
        }

        return m;
    }
}
