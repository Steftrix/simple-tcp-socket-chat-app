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
            socket = new Socket(); //care e dif dintre implement asta si cea veche ?
            socket.connect(new InetSocketAddress(host, port),3000);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running.set(true);
            logger.info("Connected to chat server on {}:{}", host, port);

            startListening();
        } catch (IOException e){
            logger.error("Connection failed", e);

        }
    }
    private void startListening(){
        listenThread = new Thread(() -> {
            try{
                String msg;
                while (running.get() && (msg = in.readLine()) != null){
                    System.out.println(msg);
                }
            } catch( IOException e){
                if(running.get()) logger.warn("Connection lost: {}",e.getMessage());
            } finally {
                running.set(false);
            }
        }, "ListenerThread-" + username);
        listenThread.start();
    }
    public void sendMessages() {
        sendThread = new Thread(() -> {
            try (Scanner scanner = new Scanner((System.in))) {
                while (running.get() && socket.isConnected()){
                    String msg = scanner.nextLine();
                    if (msg.equalsIgnoreCase("exit")){
                        closeEverything();
                        break;
                    }
                    sendMessage(msg);
                }
            } catch (Exception e){
                logger.error("Error sending message", e);
                closeEverything();
            }
        }, "SenderThread-" + username);
        sendThread.start();
    }
    public void sendMessage(String message){
        try{
            if (socket != null && socket.isConnected()){
                out.write(username + ": " + message);
                out.newLine();
                out.flush();
            } else {
                logger.warn("Cannot send message to chat server. (socket is not connected)");
            }
        } catch (IOException e){
            logger.error("Error sending message", e);
        }
    }
    public void closeEverything(){
        try {
            if (sendThread != null && sendThread.isAlive()) sendThread.interrupt();
            if (listenThread != null && listenThread.isAlive()) listenThread.interrupt();
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            logger.info("Disconnected from server");
        } catch (IOException e){
            logger.error("Error closing connection", e);
        }
    }
    public void reconnect(){
        logger.info("{} attempting to reconnect...", username);
        closeEverything();
        connect();
    }
    public boolean isConnected(){
        return socket != null && socket.isConnected() && !socket.isClosed();
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
