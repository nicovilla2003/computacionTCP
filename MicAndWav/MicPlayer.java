import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class MicPlayer {

    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            // Definir el formato de audio de micrófono
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);

            // Linea de captura en el sistema
            DataLine.Info infoMic = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine lineMic = (TargetDataLine) AudioSystem.getLine(infoMic);

            // Linea de reproduccion en el sistema
            DataLine.Info infoSpeaker = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine lineSpeaker = (SourceDataLine) AudioSystem.getLine(infoSpeaker);

            // Abrir la linea de captura
            lineMic.open(format);
            lineMic.start();

            // Abrir la linea de reproduccion
            lineSpeaker.open(format);
            lineSpeaker.start();

            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                int bytesRed = lineMic.read(buffer, 0, buffer.length);

                lineSpeaker.write(buffer, 0, bytesRed);
            }
            
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}