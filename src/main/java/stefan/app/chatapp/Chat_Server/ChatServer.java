package stefan.app.chatapp.Chat_Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);

    public static void main(String[] args) {
        ServerConfig config = new ServerConfig();
        int port = config.getPort();
        int maxClients = config.maxClients();

        ExecutorService threadPool = Executors.newFixedThreadPool(maxClients);

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Chat server started on port " + port);

            while(true) {
                Socket clientSocket = serverSocket.accept();
                log.info("New connection from {}", clientSocket.getRemoteSocketAddress());
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e){
            log.error("Server error",e);
        }
    }
}
