module stefan.app.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;

    opens stefan.app.chatapp to javafx.fxml;
    opens stefan.app.chatapp.controllers to javafx.fxml;
    exports stefan.app.chatapp;
    exports stefan.app.chatapp.controllers;
    exports stefan.app.chatapp.Chat_Client;
    exports stefan.app.chatapp.Chat_Server;
}