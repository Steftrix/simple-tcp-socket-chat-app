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

    public ChatClient(String username) {
        this.username = username;
        try{
            ServerConfig config = new ServerConfig();
            String host = config.getHost();
            int port = config.getPort();

            this.socket = new Socket(host, port);
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            logger.info("Connected to chat server on {}:{}", host, port);
        } catch (IOException e){
            logger.error("Connection failed", e);

        }
    }

    public void sendMessages() {
        new Thread(() -> {
            try (Scanner scanner = new Scanner((System.in))) {
                while (socket.isConnected()){
                    String msg = scanner.nextLine();
                    if (msg.equalsIgnoreCase("exit")){
                        closeEverything();
                        break;
                    }
                    out.write(username + ": " + msg);
                    out.newLine();
                    out.flush();
                }
            } catch (IOException e){
                closeEverything();
            }
        }).start();
    }

    public void listenForMessages() {
        new Thread(() -> {
            String msgFromServer;
            try {
                while((msgFromServer = in.readLine()) != null){
                    System.out.println(msgFromServer);
                }
            } catch (IOException e){
                closeEverything();
            }
        }).start();
    }

    public void closeEverything(){
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            logger.info("Disconnected from server");
        } catch (IOException e){
            logger.error("Error closing connection", e);
        }
    }

    public static void main(String[] args){
        System.out.println("Enter your username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();

        ChatClient client = new ChatClient(username);
        client.listenForMessages();
        client.sendMessages();
    }
}
