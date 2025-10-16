module stefan.app.chatapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.slf4j;

    opens stefan.app.chatapp to javafx.fxml;
    exports stefan.app.chatapp;
    exports tests;
    opens tests to javafx.fxml;
}