package stefan.app.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ChatApplication.class);
    public static final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(ChatApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load(), 400, 500);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.setOnCloseRequest(event -> {
            logger.info("Application closing via window close button");
        });

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        logger.info("JavaFX Application stop() called");
        if (!shuttingDown.compareAndSet(false, true)) {
            return;
        }
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("JVM shutdown hook triggered");
            shuttingDown.compareAndSet(false, true);
        }, "Shutdown-Hook"));

        launch(args);
    }
}
