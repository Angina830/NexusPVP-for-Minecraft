package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.nexuspvp.util.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JumpParticles extends Module {
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Burst", "Burst", "SpiralVortex", "RingBurst"));
    private final NumberSetting count = addSetting(new NumberSetting("Count", 16, 6, 36, 1));
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 1.2, 0.3, 3.0, 0.1));
    private final BooleanSetting gravity = addSetting(new BooleanSetting("Gravity", true));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 255, 200, 240)));

    private final List<PhysicsParticle> particles = new ArrayList<>();
    private boolean wasOnGround = true;

    public JumpParticles() {
        super("JumpParticles", "Outward bursting 3D particles on player jump & impact", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        boolean onGround = mc.player.isOnGround();
        if (wasOnGround && !onGround && mc.player.getVelocity().y > 0.05) {
            spawnParticles(mc.player.getPos());
        }
        wasOnGround = onGround;

        boolean hasGrav = gravity.isEnabled();
        Iterator<PhysicsParticle> it = particles.iterator();
        while (it.hasNext()) {
            PhysicsParticle p = it.next();
            p.pos = p.pos.add(p.vel);
            if (hasGrav) {
                p.vel = new Vec3d(p.vel.x * 0.96, p.vel.y - 0.012, p.vel.z * 0.96);
            } else {
                p.vel = p.vel.multiply(0.95);
            }
            p.age++;
            p.alpha = Math.max(0.0f, 1.0f - ((float) p.age / (float) p.maxAge));
            if (p.age >= p.maxAge) {
                it.remove();
            }
        }
    }

    private void spawnParticles(Vec3d origin) {
        int n = count.getIntValue();
        float spd = speed.getFloatValue() * 0.12f;
        String curStyle = style.getValue();

        for (int i = 0; i < n; i++) {
            Color c = rainbow.isEnabled() ? ColorUtils.rainbow(i * 100) : color.getColor();
            Vec3d vel;

            if (curStyle.equals("SpiralVortex")) {
                double angle = (2 * Math.PI * i / n) + (Math.random() * 0.2);
                vel = new Vec3d(Math.cos(angle) * spd, 0.06 + Math.random() * 0.08, Math.sin(angle) * spd);
            } else if (curStyle.equals("RingBurst")) {
                double angle = 2 * Math.PI * i / n;
                vel = new Vec3d(Math.cos(angle) * spd * 1.5, 0.02, Math.sin(angle) * spd * 1.5);
            } else {
                double theta = Math.random() * 2 * Math.PI;
                double phi = Math.random() * Math.PI * 0.5;
                vel = new Vec3d(
                        Math.cos(theta) * Math.sin(phi) * spd,
                        Math.cos(phi) * spd * 0.9 + 0.04,
                        Math.sin(theta) * Math.sin(phi) * spd
                );
            }

            particles.add(new PhysicsParticle(origin.add(0, 0.05, 0), vel, c, 25 + (int) (Math.random() * 15)));
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null || particles.isEmpty()) return;

        RenderUtils.setupBloom3D();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (PhysicsParticle p : particles) {
            if (p.alpha <= 0.01f) continue;

            RenderSystem.pushMatrix();
            RenderSystem.translated(p.pos.x - cam.x, p.pos.y - cam.y, p.pos.z - cam.z);

            RenderSystem.rotatef(-mc.gameRenderer.getCamera().getYaw(), 0f, 1f, 0f);
            RenderSystem.rotatef(mc.gameRenderer.getCamera().getPitch(), 1f, 0f, 0f);

            int alpha = (int) (p.color.getAlpha() * p.alpha);
            float sz = 0.06f * p.alpha;

            int r = p.color.getRed();
            int g = p.color.getGreen();
            int b = p.color.getBlue();

            buffer.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            buffer.vertex(0f, 0f, 0f).color(r, g, b, alpha).next();
            buffer.vertex(0f, sz, 0f).color(r, g, b, 0).next();
            buffer.vertex(sz * 0.7f, 0f, 0f).color(r, g, b, 0).next();
            buffer.vertex(0f, -sz, 0f).color(r, g, b, 0).next();
            buffer.vertex(-sz * 0.7f, 0f, 0f).color(r, g, b, 0).next();
            buffer.vertex(0f, sz, 0f).color(r, g, b, 0).next();
            tessellator.draw();

            RenderSystem.popMatrix();
        }

        RenderUtils.cleanupBloom3D();
    }

    private static class PhysicsParticle {
        Vec3d pos;
        Vec3d vel;
        final Color color;
        final int maxAge;
        int age = 0;
        float alpha = 1.0f;

        PhysicsParticle(Vec3d pos, Vec3d vel, Color color, int maxAge) {
            this.pos = pos;
            this.vel = vel;
            this.color = color;
            this.maxAge = maxAge;
        }
    }
}
