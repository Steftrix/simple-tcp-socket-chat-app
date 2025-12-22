package stefan.app.chatapp.Chat_Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stefan.app.chatapp.Chat_Server.ServerConfig;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private final String username;
    private String host;
    private int port;

    private Thread listenThread;
    private Thread sendThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ChatClient(String username) {
        this.username = username;
        loadConfig();
        connect();
    }

    private void loadConfig(){
        ServerConfig config = new ServerConfig();
        this.host = config.getHost();
        this.port = config.getPort();
    }

    private void connect(){
        try{
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 3000);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running.set(true);
            logger.info("Connected to chat server on {}:{}", host, port);
        } catch (IOException e){
            logger.error("Connection failed", e);
        }
    }
    private void startListening(){
        listenThread = new Thread(() -> {
            try{
                String msg;
                while (running.get()) {
                    try {
                        msg = in.readLine();
                    } catch (IOException e) {
                        if (running.get()) {
                            logger.warn("Connection lost: {}", e.getMessage());
                        }
                        break;
                    }
                    if (msg == null) break;
                    System.out.println(msg);
                }
            } finally {
                running.set(false);
            }
        }, "ListenerThread-" + username);
        listenThread.setDaemon(true);
        listenThread.start();
    }
    public String receiveMessage() throws IOException {
        if (!running.get() || in == null) {
            return null;
        }
        try {
            return in.readLine();
        } catch (IOException e) {
            if (running.get()) {
                throw e;
            } else {
                return null;
            }
        }
    }
    public void sendMessages() {
        sendThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                startListening();

                while (running.get() && socket != null && socket.isConnected()){
                    String msg = scanner.nextLine();
                    if (msg.equalsIgnoreCase("exit")){
                        closeEverything();
                        break;
                    }
                    sendMessage(msg);
                }
            } catch (Exception e){
                if (running.get()) {
                    logger.error("Error sending message", e);
                    closeEverything();
                }
            }
        }, "SenderThread-" + username);
        sendThread.setDaemon(true);
        sendThread.start();
    }

    public void sendMessage(String message){
        try{
            if (socket != null && socket.isConnected() && running.get()){
                out.write(username + ": " + message);
                out.newLine();
                out.flush();
            } else {
                logger.warn("Cannot send message to chat server. (socket is not connected)");
            }
        } catch (IOException e){
            if (running.get()) {
                logger.error("Error sending message", e);
            }
        }
    }
    public void closeEverything(){
        if (!running.compareAndSet(true, false)) {
            return;
        }
        logger.info("Closing connection for user: {}", username);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.warn("Error closing socket: {}", e.getMessage());
        }
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            logger.warn("Error closing input stream: {}", e.getMessage());
        }
        try {
            if (out != null) out.close();
        } catch (IOException e) {
            logger.warn("Error closing output stream: {}", e.getMessage());
        }
        try {
            if (listenThread != null && listenThread.isAlive()) listenThread.interrupt();
        } catch (Exception ignored){}
        try {
            if (sendThread != null && sendThread.isAlive()) sendThread.interrupt();
        } catch (Exception ignored){}

        logger.info("Connection closed for user: {}", username);
    }

    public boolean isConnected(){
        return socket != null && socket.isConnected() && !socket.isClosed() && running.get();
    }

    public String getUsername(){
        return username;
    }

    public static void main(String[] args){
        System.out.println("Enter your username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();

        ChatClient client = new ChatClient(username);
        client.sendMessages();
    }
}
