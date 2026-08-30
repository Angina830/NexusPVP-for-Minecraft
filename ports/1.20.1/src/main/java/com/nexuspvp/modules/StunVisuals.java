package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StunVisuals extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "Cylinder", "Cylinder", "Ring", "Forcefield", "Wireframe"));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 215, 0, 190)));
    private final NumberSetting defaultRadius = addSetting(new NumberSetting("Radius", 7.0, 3.0, 15.0, 0.5));
    private final BooleanSetting autoDetect = addSetting(new BooleanSetting("AutoDetect", true));
    private final NumberSetting height = addSetting(new NumberSetting("Height", 3.5, 1.0, 8.0, 0.5));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 5.0, 0.5));
    private final BooleanSetting pulse = addSetting(new BooleanSetting("Pulsing", true));
    private final BooleanSetting hideParticles = addSetting(new BooleanSetting("HideParticles", true));
    private final BooleanSetting warning = addSetting(new BooleanSetting("Warning", true));

    public static class StunZone {
        public Vec3d center;
        public double radius;
        public long firstSeen;
        public long lastSeen;
        public int particleCount;

        public StunZone(Vec3d center, double radius, long now) {
            this.center = center;
            this.radius = radius;
            this.firstSeen = now;
            this.lastSeen = now;
            this.particleCount = 1;
        }

        public boolean isInside(Vec3d pos) {
            double dx = pos.x - center.x;
            double dz = pos.z - center.z;
            return Math.sqrt(dx * dx + dz * dz) <= radius;
        }
    }

    private final List<StunZone> activeZones = new ArrayList<>();
    private float pulseAnim = 0f;

    public StunVisuals() {
        super("StunVisuals", "HolyWorld Stun Trap / Anti-Pearl Zone continuous neon 3D barrier", Category.VISUAL);
        setEnabled(true);
    }

    public boolean isHideParticles() {
        return isEnabled() && hideParticles.isEnabled();
    }

    public boolean handleParticle(ParticleEffect parameters, double x, double y, double z) {
        if (!isEnabled()) return false;
        long now = System.currentTimeMillis();

        boolean isStunParticle = (parameters.getType() == ParticleTypes.DUST ||
                                  parameters.getType() == ParticleTypes.CRIT ||
                                  parameters.getType() == ParticleTypes.TOTEM_OF_UNDYING ||
                                  parameters.getType() == ParticleTypes.FLAME ||
                                  parameters.getType() == ParticleTypes.ENCHANTED_HIT);

        if (!isStunParticle) return false;

        Vec3d pPos = new Vec3d(x, y, z);
        double rad = defaultRadius.getValue();

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                double dist = Math.hypot(pPos.x - zone.center.x, pPos.z - zone.center.z);
                if (dist <= rad + 2.5) {
                    zone.lastSeen = now;
                    zone.particleCount++;
                    if (autoDetect.isEnabled() && dist > 2.0 && dist < 12.0) {
                        zone.radius = zone.radius * 0.95 + dist * 0.05;
                    }
                    return true;
                }
            }

            activeZones.add(new StunZone(pPos, rad, now));
        }
        return true;
    }

    @Override
    public void onTick() {
        pulseAnim += 0.04f;
        long now = System.currentTimeMillis();
        synchronized (activeZones) {
            Iterator<StunZone> it = activeZones.iterator();
            while (it.hasNext()) {
                StunZone z = it.next();
                if (now - z.lastSeen > 7000) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || activeZones.isEmpty()) return;

        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        Color baseCol = color.getColor();
        float pulseMul = pulse.isEnabled() ? (0.85f + (float) Math.sin(pulseAnim * 3.5f) * 0.15f) : 1.0f;
        
        float r = (baseCol.getRed() / 255.0f) * pulseMul;
        float g = (baseCol.getGreen() / 255.0f) * pulseMul;
        float b = (baseCol.getBlue() / 255.0f) * pulseMul;
        float a = baseCol.getAlpha() / 255.0f;

        String curStyle = style.getValue();
        float h = height.getFloatValue();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Matrix4f mat = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                float cx = (float) (zone.center.x - camPos.x);
                float cy = (float) (zone.center.y - camPos.y);
                float cz = (float) (zone.center.z - camPos.z);
                float rad = (float) zone.radius;

                int segments = 64;

                // 1. Semi-transparent forcefield cylinder wall (Quads)
                if (curStyle.equals("Cylinder") || curStyle.equals("Forcefield")) {
                    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    float wallAlpha = a * 0.35f;

                    for (int i = 0; i < segments; i++) {
                        double a1 = 2 * Math.PI * i / segments;
                        double a2 = 2 * Math.PI * (i + 1) / segments;

                        float x1 = cx + (float) (Math.cos(a1) * rad);
                        float z1 = cz + (float) (Math.sin(a1) * rad);
                        float x2 = cx + (float) (Math.cos(a2) * rad);
                        float z2 = cz + (float) (Math.sin(a2) * rad);

                        buffer.vertex(mat, x1, cy, z1).color(r, g, b, wallAlpha).next();
                        buffer.vertex(mat, x2, cy, z2).color(r, g, b, wallAlpha).next();
                        buffer.vertex(mat, x2, cy + h, z2).color(r, g, b, 0.0f).next();
                        buffer.vertex(mat, x1, cy + h, z1).color(r, g, b, 0.0f).next();
                    }
                    tessellator.draw();
                }

                // 2. Ground and Top Lines
                buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                for (int i = 0; i < segments; i++) {
                    double a1 = 2 * Math.PI * i / segments;
                    double a2 = 2 * Math.PI * (i + 1) / segments;

                    float x1 = cx + (float) (Math.cos(a1) * rad);
                    float z1 = cz + (float) (Math.sin(a1) * rad);
                    float x2 = cx + (float) (Math.cos(a2) * rad);
                    float z2 = cz + (float) (Math.sin(a2) * rad);

                    // Ground ring
                    buffer.vertex(mat, x1, cy + 0.05f, z1).color(r, g, b, a).next();
                    buffer.vertex(mat, x2, cy + 0.05f, z2).color(r, g, b, a).next();

                    // Waist ring
                    if (!curStyle.equals("Ring")) {
                        buffer.vertex(mat, x1, cy + h * 0.5f, z1).color(r, g, b, a * 0.6f).next();
                        buffer.vertex(mat, x2, cy + h * 0.5f, z2).color(r, g, b, a * 0.6f).next();
                    }
                }

                // Wireframe vertical pillars
                if (curStyle.equals("Wireframe") || curStyle.equals("Forcefield")) {
                    for (int i = 0; i < 16; i++) {
                        double ang = 2 * Math.PI * i / 16;
                        float px = cx + (float) (Math.cos(ang) * rad);
                        float pz = cz + (float) (Math.sin(ang) * rad);
                        buffer.vertex(mat, px, cy, pz).color(r, g, b, a * 0.7f).next();
                        buffer.vertex(mat, px, cy + h, pz).color(r, g, b, 0.0f).next();
                    }
                }
                tessellator.draw();
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (!warning.isEnabled() || mc.player == null || activeZones.isEmpty()) return;

        Vec3d pPos = mc.player.getPos();
        boolean insideAny = false;

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                if (zone.isInside(pPos)) {
                    insideAny = true;
                    break;
                }
            }
        }

        if (insideAny) {
            int screenW = Compat.getScaledWidth();
            int screenH = Compat.getScaledHeight();
            String text = "§e§l⚠ STUN ZONE - NO PEARLS! ⚠";
            int textW = mc.textRenderer.getWidth(text);
            int x = screenW / 2 - textW / 2;
            int y = screenH / 2 + 35;

            RenderUtils.drawRoundedRect(matrices, x - 6, y - 3, textW + 12, 16, 4, 0xDD1E1F22);
            RenderUtils.drawRoundedRect(matrices, x - 7, y - 4, textW + 14, 18, 5, 0xEEFFCC00);
            Compat.drawWithShadow(mc.textRenderer, matrices, text, x, y + 1, 0xFFFFFFFF);
        }
    }
}
