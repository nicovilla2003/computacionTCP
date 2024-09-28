import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static Map<String, Chat> chats = new HashMap<>();
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        ServerSocket audioSocket = new ServerSocket(12346);
        System.out.println("Servidor iniciado en el puerto 12345");

        while (true) {
            Socket socket = serverSocket.accept();
            Socket audioSoc = audioSocket.accept();
            ClientHandler clientHandler = new ClientHandler(socket, audioSoc);
            clients.add(clientHandler);
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

    public static synchronized String getChatNames() {
        return chats.keySet().toString();
    }

    public static synchronized void removeClientFromChat(ClientHandler client, Chat chat) {
        chat.removeClient(client);
    }
}
