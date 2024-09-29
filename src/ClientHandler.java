import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Socket audioSocket;
    private BufferedReader in;
    private PrintWriter out;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String nickname;
    private Chat chat; // Mantiene una referencia al chat actual del cliente

    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = true;
    private static final AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);

    public ClientHandler(Socket socket, Socket audioSocket) {
        this.socket = socket;
        this.audioSocket = audioSocket;
    }

    public String getNickname() {
        return nickname;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void sendAudio(byte[] audioData) {
        try {
            dos.writeInt(audioData.length);
            dos.flush();
            dos.write(audioData);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getAudioSocket() {
        return this.audioSocket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            dis = new DataInputStream(audioSocket.getInputStream());
            dos = new DataOutputStream(audioSocket.getOutputStream());





            // Leer el nickname del cliente
            nickname = in.readLine();
            out.println("Bienvenido " + nickname + "!");

            Thread audioThread = new Thread(new Runnable() {
                public void run() {
                    manejarAudio();
                }
            });
            audioThread.start();

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

                                // }else if(message.equals("/audio")){
                                //     byte[] audioData = new byte[4096];
                                //     int bytesRead;
                                //     while ((bytesRead = dis.read(audioData)) != -1) {
                                //         chat.broadcastAudio(audioData, this);
                                //     }
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

private void manejarAudio() {
    try {
        while (true) {
            int audioLength = dis.readInt();  // Leer la longitud del audio
            byte[] audioData = new byte[audioLength];
            dis.readFully(audioData);  // Leer el audio completo
            chat.broadcastAudio(audioData, this);  // Enviar audio a otros clientes
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
           
}
