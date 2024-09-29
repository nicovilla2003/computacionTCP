import java.io.*;
import java.net.*;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;

public class Cliente {
    private Socket socket;
    private Socket audioSocket;
    private BufferedReader in;
    private PrintWriter out;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String nickname;
    private Thread listenerThread;
    private Thread audioListenerThread;

    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = true;
    private static final AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);

    public Cliente(String address, int port, int audioPort, String nickname) {
        try {
            this.nickname = nickname;
            this.socket = new Socket(address, port);
            this.audioSocket = new Socket(address, audioPort);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.dis = new DataInputStream(audioSocket.getInputStream());
            this.dos = new DataOutputStream(audioSocket.getOutputStream());

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
                iniciarAudioListener();
                // Permitir que el cliente envíe mensajes
                enviarMensajes();

            }else if(opcion.equals("2")){
                out.println("ver_chats");
            }else if (opcion.equals("3")){
                out.println("/exit");
                detenerListener();
                detenerAudioListener();
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
                }else if(message.equals("/audio")){
                    enviarAudio();  
                }
            }
        }
    }

    private void enviarAudio() {
        int duration = 5; // seconds
        ByteArrayOutputStream outAudio = new ByteArrayOutputStream();
        AudioRecorder recorder = new AudioRecorder(format, duration, outAudio);
        Thread t = new Thread(recorder);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte[] audio = outAudio.toByteArray();
        try {
            dos.writeInt(audio.length);
            dos.flush();
            dos.write(audio);
            dos.flush();
            System.out.println("Enviando audio...");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void iniciarAudioListener() {
        detenerAudioListener();
        audioListenerThread = new Thread(new audioListener());
        audioListenerThread.start();
    }

    // Método para detener el audio listener
    private void detenerAudioListener() {
        if (audioListenerThread != null && audioListenerThread.isAlive()) {
            audioListenerThread.interrupt(); // Interrumpir el hilo de manera controlada
            audioListenerThread = null;
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
            byte[] audioData;
            String serverMessage;
            try {
                while (!Thread.currentThread().isInterrupted() && (serverMessage = in.readLine()) != null ) {
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

    private class audioListener implements Runnable {
        public void run() {
            byte[] audioData;
            try {
                DataInputStream dataIn = new DataInputStream(audioSocket.getInputStream());
                while (!Thread.currentThread().isInterrupted()) {
                    int audioLength = dataIn.readInt();
                
                // Crear un buffer para el audio recibido
                audioData = new byte[audioLength];
                
                // Leer el audio en el buffer
                dataIn.readFully(audioData);  // Lee exactamente 'audioLength' bytes
                
                // Reproducir el audio recibido
                AudioPlayer player = new AudioPlayer(format);
                player.initAudio(audioData);  // Pasar los datos de audio al reproductor
            }} catch (IOException e) {
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
        new Cliente("localhost", 12345, 12346, nickname);
    }
}
