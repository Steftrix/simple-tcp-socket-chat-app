package stefan.app.chatapp.Chat_Client;

import org.slf4j.LoggerFactory;
import stefan.app.chatapp.Chat_Server.ServerConfig;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import org.slf4j.Logger;

public class ChatClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String username;
    private volatile boolean running = true;

    public ChatClient(String username) {
        this.username = username;
        try {
            ServerConfig config = new ServerConfig();
            String host = config.getHost();
            int port = config.getPort();

            this.socket = new Socket(host, port);
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            logger.info("Connected to chat server on {}:{}", host, port);
        } catch (IOException e) {
            logger.error("Connection failed", e);
        }
    }

    public void startChat() {
        try {
            Scanner scanner = new Scanner(System.in);

            while (running && socket.isConnected()) {
                if (in.ready()) {
                    String msgFromServer = in.readLine();
                    if (msgFromServer != null) {
                        System.out.println(msgFromServer);
                    } else {
                        break;
                    }
                }

                if (System.in.available() > 0) {
                    String userInput = scanner.nextLine();
                    if (userInput.equalsIgnoreCase("exit")) {
                        running = false;
                        break;
                    }
                    out.write(username + ": " + userInput);
                    out.newLine();
                    out.flush();
                }
            }
        } catch (IOException e) {
            logger.error("Error during chat", e);
        } finally {
            closeEverything();
        }
    }

    public void closeEverything() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            logger.info("Disconnected from server");
        } catch (IOException e) {
            logger.error("Error closing connection", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("Enter your username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();

        ChatClient client = new ChatClient(username);
        client.startChat();
    }
}