package stefan.app.chatapp.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import stefan.app.chatapp.Chat_Client.ChatClient;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatController {
    @FXML
    private VBox messageContainer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label emailLabel;

    private ChatClient client;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        // Auto-scroll to bottom when new messages arrive
        messageContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0);
        });

        // Enable send button only when text is entered
        sendButton.setDisable(true);
        messageField.textProperty().addListener((obs, oldVal, newVal) -> {
            sendButton.setDisable(newVal.trim().isEmpty());
        });

        // Allow Enter to send message
        messageField.setOnAction(event -> handleSend());
    }

    public void setClient(ChatClient client) {
        this.client = client;
        emailLabel.setText(client.getUsername());
        statusLabel.setText("Connected");

        // Start listening for messages in a separate thread
        Thread listenerThread = new Thread(() -> {
            try {
                String message;
                while (client.isConnected() && (message = client.receiveMessage()) != null) {
                    final String msg = message;
                    Platform.runLater(() -> addIncomingMessage(msg));
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Disconnected");
                });
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    @FXML
    private void handleSend() {
        String message = messageField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        if (message.equalsIgnoreCase("exit")) {
            handleDisconnect();
            return;
        }

        // Send message to server
        client.sendMessage(message);

        // Display message in UI
        addOutgoingMessage(message);

        // Clear input field
        messageField.clear();
    }

    private void addOutgoingMessage(String message) {
        HBox messageBox = createMessageBox(message, true, client.getUsername());
        messageContainer.getChildren().add(messageBox);
    }

    private void addIncomingMessage(String fullMessage) {
        // Parse message format: "username: message" or "[port]username: message"
        String displayMessage = fullMessage;
        String sender = "";

        // Remove port prefix if present
        if (fullMessage.startsWith("[")) {
            int endBracket = fullMessage.indexOf("]");
            if (endBracket > 0) {
                displayMessage = fullMessage.substring(endBracket + 1);
            }
        }

        // Extract sender
        if (displayMessage.contains(":")) {
            int colonIndex = displayMessage.indexOf(":");
            sender = displayMessage.substring(0, colonIndex).trim();
        }

        HBox messageBox = createMessageBox(displayMessage, false, sender);
        messageContainer.getChildren().add(messageBox);
    }

    private HBox createMessageBox(String message, boolean isOutgoing, String sender) {
        // Create message container
        VBox bubble = new VBox(4);
        bubble.setMaxWidth(450);

        // Parse message content
        String content = message;
        if (message.contains(":")) {
            int colonIndex = message.indexOf(":");
            content = message.substring(colonIndex + 1).trim();
        }

        // Sender label (for incoming messages)
        if (!isOutgoing && !sender.isEmpty()) {
            Label senderLabel = new Label(sender);
            senderLabel.setStyle(
                    "-fx-font-size: 11px; " +
                            "-fx-text-fill: #666666; " +
                            "-fx-font-weight: 500;"
            );
            bubble.getChildren().add(senderLabel);
        }

        // Message content
        Label messageLabel = new Label(content);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(430);
        messageLabel.setStyle("-fx-font-size: 13px;");

        // Time label
        Label timeLabel = new Label(LocalTime.now().format(TIME_FORMATTER));
        timeLabel.setStyle(
                "-fx-font-size: 10px; " +
                        "-fx-text-fill: #999999;"
        );

        bubble.getChildren().addAll(messageLabel, timeLabel);

        // Style bubble based on sender
        if (isOutgoing) {
            bubble.setStyle(
                    "-fx-background-color: #e3f2fd; " +
                            "-fx-padding: 10 14; " +
                            "-fx-background-radius: 4;"
            );
            messageLabel.setStyle(messageLabel.getStyle() + "-fx-text-fill: #1a1a1a;");
        } else {
            bubble.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-padding: 10 14; " +
                            "-fx-background-radius: 4; " +
                            "-fx-border-color: #e0e0e0; " +
                            "-fx-border-width: 1;"
            );
            messageLabel.setStyle(messageLabel.getStyle() + "-fx-text-fill: #1a1a1a;");
        }

        // Container for alignment
        HBox container = new HBox(bubble);
        container.setPadding(new Insets(4, 0, 4, 0));
        container.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        return container;
    }

    @FXML
    private void handleDisconnect() {
        client.closeEverything();
        Platform.exit();
    }

    @FXML
    private void handleReconnect() {
        statusLabel.setText("Reconnecting...");

        new Thread(() -> {
            client.reconnect();
            Platform.runLater(() -> {
                if (client.isConnected()) {
                    statusLabel.setText("Connected");
                } else {
                    statusLabel.setText("Disconnected");
                }
            });
        }).start();
    }
}