package org.mp.frontend26;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MpGuiController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
