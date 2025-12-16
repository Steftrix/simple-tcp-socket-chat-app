package stefan.app.chatapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import stefan.app.chatapp.Chat_Client.ChatClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private Button loginButton;

    @FXML
    private Label emailErrorLabel;

    // Allowed email providers
    private static final List<String> ALLOWED_PROVIDERS = Arrays.asList(
            "gmail.com",
            "outlook.com",
            "yahoo.com",
            "hotmail.com",
            "live.com",
            "icloud.com",
            "protonmail.com",
            "aol.com"
    );

    // Email validation regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @FXML
    public void initialize() {
        // Enable login button only when email is entered
        loginButton.setDisable(true);
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            loginButton.setDisable(newVal.trim().isEmpty());
            // Clear error when user types
            if (emailErrorLabel.isVisible()) {
                hideError();
            }
        });

        // Allow Enter key to login
        emailField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim().toLowerCase();

        if (email.isEmpty()) {
            showError("Email cannot be empty");
            return;
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return;
        }

        // Extract domain from email
        String domain = email.substring(email.indexOf("@") + 1);

        // Check if domain is in allowed providers list
        if (!ALLOWED_PROVIDERS.contains(domain)) {
            showError("Email provider not supported. Please use Gmail, Outlook, Yahoo, Hotmail, iCloud, ProtonMail, or AOL");
            return;
        }

        try {
            // Create chat client with email as identifier
            ChatClient client = new ChatClient(email);

            if (!client.isConnected()) {
                showError("Failed to connect to server. Please make sure the server is running.");
                return;
            }

            // Load chat window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/stefan/app/chatapp/chat-view.fxml"));
            Parent root = loader.load();

            // Pass client to chat controller
            ChatController controller = loader.getController();
            controller.setClient(client);

            // Show chat window
            Stage stage = new Stage();
            stage.setTitle("Chat");
            stage.setScene(new Scene(root, 900, 650));
            stage.setMinWidth(700);
            stage.setMinHeight(500);
            stage.setOnCloseRequest(event -> {
                client.closeEverything();
            });
            stage.show();

            // Close login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            showError("Failed to open chat window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        emailErrorLabel.setText(message);
        emailErrorLabel.setManaged(true);
        emailErrorLabel.setVisible(true);
        emailField.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-padding: 10 12; " +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #d32f2f; " +
                        "-fx-border-width: 1;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-insets: 0;"
        );
    }

    private void hideError() {
        emailErrorLabel.setManaged(false);
        emailErrorLabel.setVisible(false);
        emailField.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-padding: 10 12; " +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #d0d0d0; " +
                        "-fx-border-width: 1;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-insets: 0;"
        );
    }
}