module stefan.app.chatapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens stefan.app.chatapp to javafx.fxml;
    exports stefan.app.chatapp;
}