import javax.sound.sampled.*;
public class SoundTest {
    public static void main(String[] args) throws Exception {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not supported");
            return;
        }
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        System.out.println("Line opened. Controls supported:");
        for (Control c : line.getControls()) {
            System.out.println(" - " + c.getType().toString());
        }
        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            System.out.println("MASTER_GAIN: min=" + gain.getMinimum() + ", max=" + gain.getMaximum());
        } else {
            System.out.println("MASTER_GAIN is NOT supported!");
        }
    }
}