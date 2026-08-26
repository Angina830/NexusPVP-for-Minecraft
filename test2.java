package test;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
public class Test {
    public void test() {
        WorldRenderEvents.LAST.register(context -> {
            
        });
    }
}
