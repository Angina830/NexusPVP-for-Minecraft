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

    private final ModeSetting style = addSetting(new ModeSetting("Style", "SquareBox", "SquareBox", "SquareOutline", "ForcefieldPrism", "WireframeCube"));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 215, 0, 190)));
    private final NumberSetting defaultRadius = addSetting(new NumberSetting("HalfSize", 7.0, 2.0, 15.0, 0.5));
    private final BooleanSetting autoDetect = addSetting(new BooleanSetting("AutoDetect", true));
    private final NumberSetting height = addSetting(new NumberSetting("Height", 3.5, 1.0, 8.0, 0.5));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 5.0, 0.5));
    private final BooleanSetting pulse = addSetting(new BooleanSetting("Pulsing", true));
    private final BooleanSetting hideParticles = addSetting(new BooleanSetting("HideParticles", true));
    private final BooleanSetting warning = addSetting(new BooleanSetting("Warning", true));

    public static class StunZone {
        public Vec3d center;
        public double radius; // Half-width of square
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

        // Exact square/AABB box collision
        public boolean isInside(Vec3d pos) {
            double dx = Math.abs(pos.x - center.x);
            double dz = Math.abs(pos.z - center.z);
            return dx <= radius && dz <= radius;
        }
    }

    private final List<StunZone> activeZones = new ArrayList<>();
    private float pulseAnim = 0f;

    public StunVisuals() {
        super("StunVisuals", "HolyWorld Square Stun Trap / Anti-Pearl Zone continuous neon 3D barrier", Category.VISUAL);
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
                double dx = Math.abs(pPos.x - zone.center.x);
                double dz = Math.abs(pPos.z - zone.center.z);
                if (dx <= rad + 2.5 && dz <= rad + 2.5) {
                    zone.lastSeen = now;
                    zone.particleCount++;
                    if (autoDetect.isEnabled()) {
                        double maxOffset = Math.max(dx, dz);
                        if (maxOffset > 2.0 && maxOffset < 15.0) {
                            zone.radius = zone.radius * 0.96 + maxOffset * 0.04;
                        }
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

                float x1 = cx - rad;
                float x2 = cx + rad;
                float z1 = cz - rad;
                float z2 = cz + rad;
                float y1 = cy;
                float y2 = cy + h;

                // 1. Semi-transparent 4 Vertical Square Walls
                if (curStyle.equals("SquareBox") || curStyle.equals("ForcefieldPrism")) {
                    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    float wallAlpha = a * 0.35f;

                    // North wall
                    buffer.vertex(mat, x1, y1, z1).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x2, y1, z1).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, 0.0f).next();

                    // East wall
                    buffer.vertex(mat, x2, y1, z1).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x2, y1, z2).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, 0.0f).next();

                    // South wall
                    buffer.vertex(mat, x2, y1, z2).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x1, y1, z2).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, 0.0f).next();

                    // West wall
                    buffer.vertex(mat, x1, y1, z2).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x1, y1, z1).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, 0.0f).next();

                    tessellator.draw();
                }

                // 2. Ground & Top Lines
                buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                // Ground square
                buffer.vertex(mat, x1, y1 + 0.05f, z1).color(r, g, b, a).next();
                buffer.vertex(mat, x2, y1 + 0.05f, z1).color(r, g, b, a).next();
                buffer.vertex(mat, x2, y1 + 0.05f, z1).color(r, g, b, a).next();
                buffer.vertex(mat, x2, y1 + 0.05f, z2).color(r, g, b, a).next();
                buffer.vertex(mat, x2, y1 + 0.05f, z2).color(r, g, b, a).next();
                buffer.vertex(mat, x1, y1 + 0.05f, z2).color(r, g, b, a).next();
                buffer.vertex(mat, x1, y1 + 0.05f, z2).color(r, g, b, a).next();
                buffer.vertex(mat, x1, y1 + 0.05f, z1).color(r, g, b, a).next();

                // Top square & Vertical Corner Pillars
                if (!curStyle.equals("SquareOutline")) {
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, a * 0.5f).next();

                    // Corner pillars
                    buffer.vertex(mat, x1, y1, z1).color(r, g, b, a).next();
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x2, y1, z1).color(r, g, b, a).next();
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x2, y1, z2).color(r, g, b, a).next();
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x1, y1, z2).color(r, g, b, a).next();
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, 0.0f).next();
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
            String text = "§e§l⚠ SQUARE STUN ZONE - NO PEARLS! ⚠";
            int textW = mc.textRenderer.getWidth(text);
            int x = screenW / 2 - textW / 2;
            int y = screenH / 2 + 35;

            RenderUtils.drawRoundedRect(matrices, x - 6, y - 3, textW + 12, 16, 4, 0xDD1E1F22);
            RenderUtils.drawRoundedRect(matrices, x - 7, y - 4, textW + 14, 18, 5, 0xEEFFCC00);
            Compat.drawWithShadow(mc.textRenderer, matrices, text, x, y + 1, 0xFFFFFFFF);
        }
    }
}
