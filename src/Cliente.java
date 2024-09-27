import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;
    private Thread listenerThread;

    public Cliente(String address, int port, String nickname) {
        try {
            this.nickname = nickname;
            this.socket = new Socket(address, port);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);

            // Enviar el nickname al servidor
            out.println(nickname);
            
            Scanner scanner = new Scanner(System.in);
            String opcion;
            // Solicitar el nombre del chat
            do{
                System.out.println("Menu:");
                System.out.println("1. Crear chat o unirse a chat");
                System.out.println("2. Ver chats disponibles");
                System.out.println("3. Salir");
                System.out.print("Elige una opción: ");
                opcion = scanner.nextLine();

            if(opcion.equals("1")){
                out.println("chat");
                ingresarChat();
                
                // Iniciar hilo para escuchar mensajes
                iniciarListener();
                // Permitir que el cliente envíe mensajes
                enviarMensajes();

            }else if(opcion.equals("2")){
                out.println("ver_chats");
            }else if (opcion.equals("3")){
                out.println("/exit");
                detenerListener();
                break;
            }
            else{
                System.out.println("Opción no válida");


            }} while (!opcion.equals("3"));
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ingresarChat() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese el nombre del chat para crear o unirse:");
        String chatName = scanner.nextLine();
        out.println("/chat " + chatName);  // Enviar el nombre del chat al servidor
    }

    private void enviarMensajes() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String message = scanner.nextLine();
            if (!message.isEmpty()) {
                out.println(message);
                if(message.equals("/exit")){
                    break;
                }
            }
        }
    }

    // Método para iniciar el listener
    private void iniciarListener() {
        detenerListener();
        listenerThread = new Thread(new Listener());
        listenerThread.start();
    }

    // Método para detener el listener
    private void detenerListener() {
        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt(); // Interrumpir el hilo de manera controlada
            listenerThread = null;
        }
    }

    private class Listener implements Runnable {
        public void run() {
            String serverMessage;
            try {
                while (!Thread.currentThread().isInterrupted() && (serverMessage = in.readLine()) != null) {
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Listener detenido.");
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingresa tu nickname: ");
        String nickname = scanner.nextLine();
        new Cliente("localhost", 12345, nickname);
    }
}
