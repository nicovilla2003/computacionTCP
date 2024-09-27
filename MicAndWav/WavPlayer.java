import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class WavPlayer {

    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            // Cargar el archivo de audio
            File file = new File("Still.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);

            // Obtener el formato de audio
            AudioFormat format = audioInputStream.getFormat();

            // Linea de reproduccion en el sistema
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

            // Abrir la linea de reproduccion
            line.open(format);
            line.start();

            System.out.println("Reproduciendo el archivo " + file);
            int bytesRed = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                bytesRed = audioInputStream.read(buffer, 0, buffer.length);
                if (bytesRed == -1) {
                    break;
                }
                line.write(buffer, 0, bytesRed);
            }

            // Cerrar la linea de reproduccion
            line.drain();
            line.close();
            audioInputStream.close();
            
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}