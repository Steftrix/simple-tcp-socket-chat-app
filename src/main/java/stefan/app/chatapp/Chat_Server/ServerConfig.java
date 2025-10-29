package stefan.app.chatapp.Chat_Server;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// This class takes input from our config.properties file and throws exception if anything goes wrong
public class ServerConfig {
    private final Properties properties = new Properties();

    public ServerConfig(){
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")){
            if (input == null) {
                throw new IOException("No config file found");
            }
            properties.load(input);
        } catch (IOException e){
            throw new RuntimeException("Failed to load config file", e);
        }
    }

    public int getPort(){
        return Integer.parseInt(properties.getProperty("server.port", "5050"));
    }

    public int maxClients(){
        return Integer.parseInt(properties.getProperty("max.clients", "20"));
    }

    public String getHost(){
        return properties.getProperty("server.host", "localhost");
    }
}
