import net.minecraft.client.font.TextRenderer;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        for (Method m : TextRenderer.class.getDeclaredMethods()) {
            if (m.getName().equals("draw")) {
                System.out.println(Arrays.toString(m.getParameterTypes()));
            }
        }
    }
}
