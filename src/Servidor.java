import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Clase Servidor:
 * La clase Servidor es la que se encarga de iniciar los Sockets con sus puertos correspondientes para la comunicación.
 * Se encarga además de mantener una escucha constante de los clientes que se conectan al servidor.
 * A penas un cliente se conecta, se crea un nuevo hilo para manejar al cliente y se agrega a la lista de clientes conectados
 * Se podría decir que el While True es lo que conocemos como un Thread Pool, ya que se encarga de crear y inicializar los hilos de los Client Handlers
 * y de mantener la escucha constante de nuevos clientes.
 * El servidor también es intermediario entre los Client Handlers y los Chats, ya que se encarga de agregar y eliminar clientes de los chats.
 * La razón por la que el servidor es el intermediario es porque el servidor es el que tiene la lista de clientes conectados y los chats.
 * Además debe ser el encargado de los metodos sean sincronizados para evitar problemas de concurrencia entre los hilos. 
 * Esta clase
 */

public class Servidor {
    //Hashmap para identificar los chats por nombre
    private static Map<String, Chat> chats = new HashMap<>();
    //Lista de clientes conectados
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        //Crear un socket para los mensajes del servidor
        ServerSocket serverSocket = new ServerSocket(12345);
        //Crear un socket para el audio del servidor
        ServerSocket audioSocket = new ServerSocket(12346);
        //Avisar que el servidor se ha iniciado
        System.out.println("Servidor iniciado en el puerto 12345");

        while (true) {
            //Aceptar conexiones de clientes
            Socket socket = serverSocket.accept();
            //Aceptar conexiones de audio
            Socket audioSoc = audioSocket.accept();
            //Crear un nuevo hilo para manejar al cliente
            ClientHandler clientHandler = new ClientHandler(socket, audioSoc);
            //Agregar el cliente a la lista de clientes
            clients.add(clientHandler);
            //Iniciar el hilo del cliente
            new Thread(clientHandler).start();
        }
    }

    // Agregar un chat nuevo o añadir clientes a uno existente
    public static synchronized void addChat(String chatName, Chat chat) {
        chats.put(chatName, chat);  // Añadimos el chat completo, no solo un cliente
    }

    // Obtener el chat por nombre
    public static synchronized Chat getChat(String chatName) {
        return chats.get(chatName);
    }

    // Difundir mensajes a todos los clientes en el chat
    public static synchronized void broadcast(String message, Chat chat, ClientHandler sender) {
        if (chat != null) {
            chat.broadcast(message, sender);
        }
    }

    // Difundir audio a todos los clientes en el chat
    public static synchronized void broadcastAudio(byte[] audioData, Chat chat, ClientHandler sender) {
        if (chat != null) {
            chat.broadcastAudio(audioData, sender);
        }
    }

    // Eliminar al cliente de la lista de clientes activos
    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    // Obtener la lista de nombres de los chats
    public static synchronized String getChatNames() {
        return chats.keySet().toString();
    }

        // Eliminar al cliente del chat
    public static synchronized void removeClientFromChat(ClientHandler client, Chat chat) {
        chat.removeClient(client);
    }
}
