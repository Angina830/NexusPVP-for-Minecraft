package com.nexuspvp.modules;

import com.nexuspvp.module.Module;
import com.nexuspvp.module.Category;
import net.minecraft.client.util.math.MatrixStack;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class DebugLogger extends Module {
    public DebugLogger() {
        super("DebugLogger", "Logs debug info", Category.VISUAL);
        this.toggle(); // Enable by default
    }
    
    private int tickCount = 0;

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.gameRenderer.getCamera() == null) return;
        if (tickCount++ % 60 != 0) return; // log once a second
        
        try (PrintWriter out = new PrintWriter(new FileWriter("C:\\UPROJ\\32131\\debug.log", true))) {
            out.println("--- RENDER DEBUG ---");
            out.println("Player pos: " + mc.player.getPos());
            out.println("Camera pos: " + mc.gameRenderer.getCamera().getPos());
            out.println("Camera pitch: " + mc.gameRenderer.getCamera().getPitch());
            out.println("Camera yaw: " + mc.gameRenderer.getCamera().getYaw());
            out.println("MatrixStack peek:");
            out.println(matrices.peek().getModel().toString());
        } catch (IOException e) {}
    }
}
