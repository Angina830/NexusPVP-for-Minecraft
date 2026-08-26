import javazoom.jl.player.Player;
import java.lang.reflect.Field;
public class TestJLayer {
    public static void main(String[] args) {
        System.out.println("Fields in Player:");
        for (Field f : Player.class.getDeclaredFields()) {
            System.out.println(f.getName() + " - " + f.getType().getName());
        }
    }
}