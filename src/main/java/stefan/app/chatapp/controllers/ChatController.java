package stefan.app.chatapp.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import stefan.app.chatapp.Chat_Client.ChatClient;
import stefan.app.chatapp.ChatApplication;

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
    private Thread listenerThread;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        messageContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0);
        });

        sendButton.setDisable(true);
        messageField.textProperty().addListener((obs, oldVal, newVal) -> {
            sendButton.setDisable(newVal.trim().isEmpty());
        });

        messageField.setOnAction(event -> handleSend());
    }

    public void setClient(ChatClient client) {
        this.client = client;
        emailLabel.setText(client.getUsername());
        statusLabel.setText("Connected");

        listenerThread = new Thread(() -> {
            try {
                String message;
                while (client.isConnected()) {
                    try {
                        message = client.receiveMessage();
                    } catch (Exception e) {
                        message = null;
                        if (client.isConnected()) {
                            final String err = e.getMessage() == null ? "Connection error" : e.getMessage();
                            Platform.runLater(() -> statusLabel.setText("Disconnected"));
                        }
                        break;
                    }
                    if (message == null) {
                        break;
                    }

                    final String msg = message;
                    Platform.runLater(() -> addIncomingMessage(msg));
                }
            } finally {
                Platform.runLater(() -> statusLabel.setText("Disconnected"));
            }
        }, "MessageListener-" + client.getUsername());

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

        client.sendMessage(message);
        addOutgoingMessage(message);
        messageField.clear();
    }

    private void addOutgoingMessage(String message) {
        HBox messageBox = createMessageBox(message, true, client.getUsername());
        messageContainer.getChildren().add(messageBox);
    }

    private void addIncomingMessage(String fullMessage) {
        String displayMessage = fullMessage;
        String sender = "";

        if (fullMessage.startsWith("[")) {
            int endBracket = fullMessage.indexOf("]");
            if (endBracket > 0) {
                displayMessage = fullMessage.substring(endBracket + 1);
            }
        }

        if (displayMessage.contains(":")) {
            int colonIndex = displayMessage.indexOf(":");
            sender = displayMessage.substring(0, colonIndex).trim();
        }

        HBox messageBox = createMessageBox(displayMessage, false, sender);
        messageContainer.getChildren().add(messageBox);
    }

    private HBox createMessageBox(String message, boolean isOutgoing, String sender) {
        VBox bubble = new VBox(4);
        bubble.setMaxWidth(450);

        String content = message;
        if (message.contains(":")) {
            int colonIndex = message.indexOf(":");
            content = message.substring(colonIndex + 1).trim();
        }

        if (!isOutgoing && !sender.isEmpty()) {
            Label senderLabel = new Label(sender);
            senderLabel.setStyle(
                    "-fx-font-size: 11px; " +
                            "-fx-text-fill: #666666; " +
                            "-fx-font-weight: 500;"
            );
            bubble.getChildren().add(senderLabel);
        }

        Label messageLabel = new Label(content);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(430);
        messageLabel.setStyle("-fx-font-size: 13px;");

        Label timeLabel = new Label(LocalTime.now().format(TIME_FORMATTER));
        timeLabel.setStyle(
                "-fx-font-size: 10px; " +
                        "-fx-text-fill: #999999;"
        );

        bubble.getChildren().addAll(messageLabel, timeLabel);

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

        HBox container = new HBox(bubble);
        container.setPadding(new Insets(4, 0, 4, 0));
        container.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        return container;
    }

    @FXML
    private void handleDisconnect() {
        if (!ChatApplication.shuttingDown.compareAndSet(false, true)) {
            return;
        }

        statusLabel.setText("Disconnecting...");
        sendButton.setDisable(true);
        messageField.setDisable(true);

        new Thread(() -> {
            try {
                if (client != null) {
                    client.closeEverything();
                }
                if (listenerThread != null) {
                    listenerThread.interrupt();
                }

            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            } finally {
                Platform.runLater(() -> {
                    Stage stage = (Stage) sendButton.getScene().getWindow();
                    stage.close();
                });
            }
        }, "Client-Cleanup").start();
    }

    public void onWindowClose() {
        handleDisconnect();
    }
}
