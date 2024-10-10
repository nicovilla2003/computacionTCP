import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/* 
 * Clase Chat:
 * La clase Chat es la encargada de manejar la comunicación entre los clientes en un chat.
 * La clase Chat mantiene una lista de ClientHandlers que están conectados al chat.
 * Para que los mensajes de un chat sean enviados solo a los demás clientes de su chat, se requiere esta clase.
 * La clase Chat tiene un método addClient() que se encarga de agregar un cliente al chat.
 * La clase Chat tiene un método removeClient() que se encarga de remover un cliente del chat.
 * La clase Chat tiene un método broadcast() que se encarga de enviar un mensaje a todos los clientes del chat.
 * La clase Chat tiene un método broadcastAudio() que se encarga de enviar un audio a todos los clientes del chat.
 * Para que los métodos sean sincronizados y evitar problemas de concurrencia entre los hilos, se utiliza la palabra clave synchronized.
 * Esta clase también se encarga de evitar que los mensajes y audios que se envian no sean recibidos por el remitente (el que lo envia).
 * De lo contrario, el remitente recibiría su propio mensaje o audio.
 */

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

    public synchronized void broadcastAudio(byte[] audioData, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {  // No enviamos el audio al remitente
                    client.sendAudio(audioData);
            }
        }
    }
}
