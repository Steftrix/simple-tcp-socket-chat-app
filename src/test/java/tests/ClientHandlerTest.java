package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import stefan.app.chatapp.Chat_Server.ClientHandler;
import stefan.app.chatapp.Chat_Server.ServerConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientHandlerTest {
    private ServerSocket serverSocket;
    private ExecutorService executor;

    @BeforeEach
    public void setup() throws IOException{
        ServerConfig serverConfig = new ServerConfig();
        int port = serverConfig.getPort();
        int maxClients = serverConfig.maxClients();

        serverSocket = new ServerSocket(port);
        executor = Executors.newFixedThreadPool(maxClients);
    }
    @AfterEach
    public void cleanup() throws IOException{
        serverSocket.close();
        executor.shutdown();
        ClientHandler.clients.clear();
    }
    @Test
    public void testBroadcast() throws IOException, InterruptedException {
        //first client connection
        Socket client1 = new Socket("localhost", serverSocket.getLocalPort());
        Socket serverSideClient1 = serverSocket.accept();
        ClientHandler handler1 = new ClientHandler(serverSideClient1);
        executor.execute(handler1);

        BufferedReader reader1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
        reader1.readLine();

        //connect second client
        Socket client2 = new Socket("localhost", serverSocket.getLocalPort());
        Socket serverSideClient2 = serverSocket.accept();
        ClientHandler handler2 = new ClientHandler(serverSideClient2);
        executor.execute(handler2);

        BufferedReader reader2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));
        reader2.readLine();

        //send message from first client
        PrintWriter writer1 = new PrintWriter(client1.getOutputStream(), true);
        String testMessage = "Hello from client1";
        writer1.println(testMessage);

        //set timer for broadcast
        Thread.sleep(200);

        //check if the second client got the message
        String recievedMessage = reader2.readLine();
        assertEquals("["+ serverSideClient1.getPort()+"]" + testMessage, recievedMessage);

        //clean-up used sockets afterwards
        client1.close();
        client2.close();
    }
}
