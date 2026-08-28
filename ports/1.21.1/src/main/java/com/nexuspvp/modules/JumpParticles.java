package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JumpParticles extends Module {
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Burst", "Burst", "RingBurst"));
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

        for (int i = 0; i < n; i++) {
            Color c = rainbow.isEnabled() ? ColorUtils.rainbow(i * 100) : color.getColor();
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.random() * Math.PI * 0.5;
            Vec3d vel = new Vec3d(
                    Math.cos(theta) * Math.sin(phi) * spd,
                    Math.cos(phi) * spd * 0.9 + 0.04,
                    Math.sin(theta) * Math.sin(phi) * spd
            );
            particles.add(new PhysicsParticle(origin.add(0, 0.05, 0), vel, c, 25 + (int) (Math.random() * 15)));
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null || particles.isEmpty()) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        for (PhysicsParticle p : particles) {
            if (p.alpha <= 0.01f) continue;

            matrices.push();
            matrices.translate(p.pos.x - cam.x, p.pos.y - cam.y, p.pos.z - cam.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            int alpha = (int) (p.color.getAlpha() * p.alpha);
            float sz = 0.06f * p.alpha;
            int r = p.color.getRed();
            int g = p.color.getGreen();
            int b = p.color.getBlue();

            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, 0f, 0f, 0f).color(r, g, b, alpha);
            buffer.vertex(matrix, 0f, sz, 0f).color(r, g, b, 0);
            buffer.vertex(matrix, sz * 0.7f, 0f, 0f).color(r, g, b, 0);
            buffer.vertex(matrix, 0f, -sz, 0f).color(r, g, b, 0);
            buffer.vertex(matrix, -sz * 0.7f, 0f, 0f).color(r, g, b, 0);
            buffer.vertex(matrix, 0f, sz, 0f).color(r, g, b, 0);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            matrices.pop();
        }

        RenderSystem.disableBlend();
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
