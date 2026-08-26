package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class JumpParticles extends Module {

    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 255, 255)));
    private final NumberSetting count = addSetting(new NumberSetting("Count", 16, 4, 36, 2));
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 1.0, 0.2, 3.0, 0.1));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));

    private final List<JumpParticle> particles = new ArrayList<>();
    private boolean wasOnGround = true;

    public JumpParticles() {
        super("JumpParticles", "Bright particles burst outward on jump", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        if (wasOnGround && !onGround && mc.player.getVelocity().y > 0.1) {
            int num = count.getIntValue();
            float spd = speed.getFloatValue() * 0.12f;
            Vec3d basePos = mc.player.getPos().add(0, 0.1, 0);

            for (int i = 0; i < num; i++) {
                double angle = (i * 2.0 * Math.PI) / num;
                double vx = Math.cos(angle) * spd + (Math.random() - 0.5) * 0.04;
                double vy = Math.random() * 0.08 + 0.02;
                double vz = Math.sin(angle) * spd + (Math.random() - 0.5) * 0.04;

                Color c = rainbow.isEnabled() ? ColorUtils.rainbow(System.currentTimeMillis() + i * 80L) : color.getColor();
                particles.add(new JumpParticle(basePos, new Vec3d(vx, vy, vz), c));
            }
        }
        wasOnGround = onGround;

        long now = System.currentTimeMillis();
        particles.removeIf(p -> now - p.spawnTime > 700);
        for (JumpParticle p : particles) {
            p.pos = p.pos.add(p.velocity);
            p.velocity = p.velocity.multiply(0.92).subtract(0, 0.003, 0);
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (particles.isEmpty() || mc.player == null || mc.gameRenderer == null) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        long now = System.currentTimeMillis();

        for (JumpParticle p : particles) {
            float progress = (now - p.spawnTime) / 700.0f;
            float alpha = 1.0f - progress;
            int a = (int) (alpha * 255);

            Color c = new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), Math.max(0, Math.min(255, a)));
            Vec3d rPos = p.pos.subtract(cam);

            RenderUtils.drawCircle3D(matrices, rPos.x, rPos.y, rPos.z, 0.04f, c, 2.0f);
        }
    }

    private static class JumpParticle {
        Vec3d pos;
        Vec3d velocity;
        Color color;
        long spawnTime;

        JumpParticle(Vec3d pos, Vec3d velocity, Color color) {
            this.pos = pos;
            this.velocity = velocity;
            this.color = color;
            this.spawnTime = System.currentTimeMillis();
        }
    }
}
