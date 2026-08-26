package test;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
public class Test {
    public void test(MatrixStack matrices, Camera camera) {
        matrices.multiply(camera.getRotation());
    }
}
