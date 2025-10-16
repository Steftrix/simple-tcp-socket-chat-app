package stefan.app.chatapp.Chat_Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    private static final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            clients.add(this);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            log.info("Client connected: {}", socket.getRemoteSocketAddress());
            out.println("Welcome to ChatApp");

            String message;

            while((message = in.readLine()) != null){
                log.info("Recieved {} from {}", message, socket.getRemoteSocketAddress());
                broadcast("["+ socket.getPort() + "]"+message);
            }
        }catch(Exception e){
            log.warn("Connection error: {}", e.getMessage());
        }
        finally{
            cleanup();
        }
    }
    public void broadcast(String message){
        for(ClientHandler client : clients){
            if(client != this){
                client.out.println(message);
            }
        }
    }
    public void cleanup(){
        clients.remove(this);
        try{
            socket.close();
        }catch(IOException e){
            log.error("Error getting socket", e);
        }
        log.info("Client disconnected: {}", socket.getRemoteSocketAddress());
    }
}
