import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;
    private Chat chat; // Mantiene una referencia al chat actual del cliente

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getNickname() {
        return nickname;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Leer el nickname del cliente
            nickname = in.readLine();
            out.println("Bienvenido " + nickname + "!");

            String opcion;
            while ((opcion = (String) in.readLine()) != null) {
                    switch (opcion) {
                        case "chat":
                        String chatCommand = in.readLine();
                        if (chatCommand.startsWith("/chat ")) {
                            String chatName = chatCommand.split(" ", 2)[1];
                            chat = Servidor.getChat(chatName);
                            // Si el chat no existe, se crea uno nuevo
                            if (chat == null) {
                                out.println("Chat no encontrado, creando uno nuevo...");
                                chat = new Chat(chatName);
                                Servidor.addChat(chatName, chat);  // Cambiamos aquí para agregar el chat completo
                            }
                            chat.addClient(this); // Agregar al cliente al chat
                            out.println("Te has unido al chat: " + chatName);
                        }

                        // Leer y manejar los mensajes del cliente
                        boolean exit = false;
                        String message;
                        while ((message = in.readLine()) != null && !exit) {
                            if (!message.isEmpty()) {
                                if(message.equals("/exit")){
                                    chat.removeClient(this);
                                    Servidor.removeClientFromChat(this, chat);
                                    chat=null;
                                    // Servidor.removeClient(this);
                                    exit = true;
                                    break;
                                }else{
                                System.out.println("Mensaje recibido de " + nickname + ": " + message);
                                chat.broadcast(nickname + ": " + message, this); // Difundir mensaje a todos en el chat
                                }
                            }
                        }
                            break;
                        case "ver_chats":
                            out.println("Chats disponibles: " + Servidor.getChatNames());
                            break;    
                        case "/exit":
                            out.println("Saliendo...");
                            Servidor.removeClient(this);
                            chat.removeClient(this);
                            return;    
                        default:
                            out.println("Opción no válida");
                            break;
                    }
                }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Servidor.removeClient(this);
            if (chat != null) {
                chat.removeClient(this); // Eliminar al cliente del chat al desconectarse
            }
        }
    }
}
