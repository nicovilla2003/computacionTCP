import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String name;
    private List<ClientHandler> clients;

    public Chat(String name) {
        this.name = name;
        this.clients = new ArrayList<>();
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
        broadcast(client.getNickname() + " se ha unido al chat", null);
    }
    
    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        broadcast(client.getNickname() + " ha salido del chat", null);
    }
    
    public synchronized void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {  // No enviamos el mensaje al remitente
                client.sendMessage(message);
            }
        }
    }
}
