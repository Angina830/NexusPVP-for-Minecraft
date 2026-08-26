import javazoom.jl.player.JavaSoundAudioDevice;
import java.lang.reflect.Field;
public class TestJLayer2 {
    public static void main(String[] args) {
        System.out.println("Fields in JavaSoundAudioDevice:");
        for (Field f : JavaSoundAudioDevice.class.getDeclaredFields()) {
            System.out.println(f.getName() + " - " + f.getType().getName());
        }
        System.out.println("Fields in Superclass (AudioDeviceBase):");
        for (Field f : JavaSoundAudioDevice.class.getSuperclass().getDeclaredFields()) {
            System.out.println(f.getName() + " - " + f.getType().getName());
        }
    }
}