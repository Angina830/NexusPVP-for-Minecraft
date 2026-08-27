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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Particles extends Module {
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Sparks", "Sparks", "AnimeSlash", "Stars", "Hearts", "Blood", "CritBurst"));
    private final NumberSetting count = addSetting(new NumberSetting("Count", 12, 4, 32, 1));
    private final NumberSetting size = addSetting(new NumberSetting("Size", 1.0, 0.4, 2.5, 0.1));
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 1.0, 0.3, 2.5, 0.1));
    private final BooleanSetting gravity = addSetting(new BooleanSetting("Gravity", true));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 0, 100, 240)));

    private final List<VFXParticle> particles = new ArrayList<>();
    private Entity lastTarget = null;
    private float lastHealth = -1;

    public Particles() {
        super("Particles", "Physical visual combat particles & spark bursts on hit", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        Entity target = null;
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            target = ((EntityHitResult) mc.crosshairTarget).getEntity();
        }

        if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) target;
            float totalHealth = living.getHealth() + living.getAbsorptionAmount();
            if (lastTarget == living) {
                if (totalHealth < lastHealth) {
                    spawnHitParticles(living);
                }
            }
            lastHealth = totalHealth;
            lastTarget = living;
        } else {
            lastTarget = null;
            lastHealth = -1;
        }

        boolean hasGrav = gravity.isEnabled();
        Iterator<VFXParticle> it = particles.iterator();
        while (it.hasNext()) {
            VFXParticle p = it.next();
            p.pos = p.pos.add(p.vel);
            if (hasGrav) {
                p.vel = new Vec3d(p.vel.x * 0.95, p.vel.y - 0.012, p.vel.z * 0.95);
            } else {
                p.vel = p.vel.multiply(0.94);
            }
            p.rotation += p.rotSpeed;
            p.age++;
            p.alpha = Math.max(0.0f, 1.0f - ((float) p.age / (float) p.maxAge));
            if (p.age >= p.maxAge) {
                it.remove();
            }
        }
    }

    private void spawnHitParticles(LivingEntity living) {
        Vec3d center = living.getPos().add(0, living.getHeight() * 0.6, 0);
        int n = count.getIntValue();
        float spd = speed.getFloatValue() * 0.15f;

        for (int i = 0; i < n; i++) {
            Color c = rainbow.isEnabled() ? ColorUtils.rainbow(i * 100) : color.getColor();
            if (style.getValue().equals("Blood")) {
                c = new Color(200 + (int)(Math.random() * 55), 10, 20, 240);
            }

            double theta = Math.random() * 2 * Math.PI;
            double phi = (Math.random() - 0.5) * Math.PI;
            Vec3d vel = new Vec3d(
                    Math.cos(theta) * Math.cos(phi) * spd,
                    Math.sin(phi) * spd + 0.05,
                    Math.sin(theta) * Math.cos(phi) * spd
            );

            particles.add(new VFXParticle(
                    center.add((Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.3),
                    vel,
                    c,
                    style.getValue(),
                    size.getFloatValue() * (0.8f + (float) Math.random() * 0.4f),
                    20 + (int) (Math.random() * 15)
            ));
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null || particles.isEmpty()) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (VFXParticle p : particles) {
            if (p.alpha <= 0.01f) continue;

            matrices.push();
            matrices.translate(p.pos.x - cam.x, p.pos.y - cam.y, p.pos.z - cam.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(p.rotation));
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            int alpha = (int) (p.color.getAlpha() * p.alpha);
            int r = p.color.getRed();
            int g = p.color.getGreen();
            int b = p.color.getBlue();
            float sz = 0.08f * p.size * p.alpha;

            buffer.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, 0f, 0f, 0f).color(r, g, b, alpha).next();
            for (int s = 0; s <= 8; s++) {
                double a = 2 * Math.PI * s / 8;
                buffer.vertex(matrix, (float) (sz * Math.cos(a)), (float) (sz * Math.sin(a)), 0f).color(r, g, b, 0).next();
            }
            tessellator.draw();

            matrices.pop();
        }

        RenderSystem.disableBlend();
    }

    private static class VFXParticle {
        Vec3d pos;
        Vec3d vel;
        final Color color;
        final String style;
        final float size;
        final int maxAge;
        int age = 0;
        float alpha = 1.0f;
        float rotation = (float) (Math.random() * 360);
        float rotSpeed = (float) ((Math.random() - 0.5) * 12);

        VFXParticle(Vec3d pos, Vec3d vel, Color color, String style, float size, int maxAge) {
            this.pos = pos;
            this.vel = vel;
            this.color = color;
            this.style = style;
            this.size = size;
            this.maxAge = maxAge;
        }
    }
}
