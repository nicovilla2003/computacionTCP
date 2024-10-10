import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/*
 * Clase ClientHandler:
 * La clase ClientHandler es la encargada de manejar la comunicación entre el servidor y un cliente.
 * A penas se corre la clase main del Cliente en Cliente.java, se crea un nuevo hilo de ClientHandler para manejar la comunicación con el cliente.
 * Digamos que el ClientHandler es el intermediario entre el servidor y el cliente, ya que es el encargado de manejar los mensajes y audios que se envían y reciben.
 * El ClientHandler también mantiene una referencia al chat actual del cliente, para poder enviar mensajes y audios a los demás clientes en el chat.
 * La clase ClientHandler implementa la interfaz Runnable, lo que significa que se puede correr en un hilo separado.
 * 
 * El método run() es el que se encarga de manejar la comunicación con el cliente.
 * La parte que puede llegar a ser la más confusa del metodo run() es el switch case que se encarga de manejar las opciones que el cliente envía al servidor.
 * Este switch es compartido por decirlo asi entre el Client Handler y el Cliente, ya que el cliente puede enviar comandos especiales al Client Handler.
 * Y el Client Handler tiene que reaccionar a estos comandos y ejecutar los metodos correspondientes en el servidor.
 * Luego el servidor se encarga de ejecutarlos en el chat correspondiente.
 * Cuando un Cliente escribe /exit durante la sesión de un chat por ejemplo, el Client Handler tiene que remover al cliente del chat y del servidor 
 * y colocarlo en el menu principal. 
 * 
 * El método sendMessage() es el encargado de enviar mensajes al cliente.
 * El método sendAudio() es el encargado de enviar audios al cliente.
 * El método getAudioSocket() es el encargado de retornar el socket de audio del chat.
 * El método getNickname() es el encargado de retornar el nickname del chat.
 * El método manejarAudio() es el encargado de manejar la recepción de audios.
 * Para manejar la recepción de mensajes escritos entre el cliente y el CLient Handler se utiliza un BufferedReader y un PrintWriter.
 * Estos estan especificados con los nombres in y out.
 * Para manejar la recepción de audios entre el cliente y el Client Handler se utiliza un DataInputStream y un DataOutputStream.
 * Estos estan especificados con los nombres dis y dos.
 * Podran notar que debido a que la comunicación de mensajes y audios es diferente, se utilizan diferentes Streams para manejar la comunicación.
 * Por eso tienen que haber dos puertos diferentes, uno para mensajes y otro para audios.
 */

public class ClientHandler implements Runnable {
    private Socket socket;
    private Socket audioSocket;
    private BufferedReader in;
    private PrintWriter out;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String nickname;
    private Chat chat; // Mantiene una referencia al chat actual del cliente

    /*
     * El formato de audio debe ser estandarisado para que todos los clientes puedan enviar y recibir audios.
     * Por motivos de tiempo y simplicidad, se ha decidido utilizar un formato de audio que el profe nos dio.
     * Por eso todos los audios duran 5 segundos.
     */

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
                //Se inicializan los streams de entrada y salida




            // Leer el nickname del cliente
            nickname = in.readLine();
            out.println("Bienvenido " + nickname + "!");

            /*
             * Para manejar la recepción de audios, se crea un nuevo hilo que se encargará de manejar los audios.
             * Esto sucede porque mientras que yo estoy enviando mensajes debo poder a la vez estar recibiendo audios.
             * Entonces es necesario que el cliente pueda enviar y recibir mensajes y audios al mismo tiempo.
             */
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
