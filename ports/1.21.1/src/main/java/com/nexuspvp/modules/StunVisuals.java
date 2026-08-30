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
import net.minecraft.particle.DustParticleEffect;
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
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 215, 0, 220)));
    private final NumberSetting defaultRadius = addSetting(new NumberSetting("HalfSize", 5.5, 2.0, 15.0, 0.5));
    private final BooleanSetting autoDetect = addSetting(new BooleanSetting("AutoDetect", true));
    private final NumberSetting height = addSetting(new NumberSetting("Height", 4.0, 1.0, 8.0, 0.5));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 3.0, 1.0, 6.0, 0.5));
    private final BooleanSetting pulse = addSetting(new BooleanSetting("Pulsing", true));
    private final BooleanSetting hideParticles = addSetting(new BooleanSetting("HideParticles", true));
    private final BooleanSetting warning = addSetting(new BooleanSetting("Warning", true));

    public static class StunZone {
        public double minX, maxX;
        public double minZ, maxZ;
        public double groundY;
        public double centerX, centerZ;
        public double halfSize;
        public long firstSeen;
        public long lastSeen;
        public int particleCount;

        public StunZone(double x, double y, double z, double defHalf, long now) {
            this.minX = x;
            this.maxX = x;
            this.minZ = z;
            this.maxZ = z;
            this.groundY = y;
            this.centerX = x;
            this.centerZ = z;
            this.halfSize = defHalf;
            this.firstSeen = now;
            this.lastSeen = now;
            this.particleCount = 1;
        }

        public void addParticle(double x, double y, double z, double defHalf, boolean auto, long now) {
            this.lastSeen = now;
            this.particleCount++;
            this.minX = Math.min(minX, x);
            this.maxX = Math.max(maxX, x);
            this.minZ = Math.min(minZ, z);
            this.maxZ = Math.max(maxZ, z);
            this.groundY = Math.min(groundY, y);

            this.centerX = (minX + maxX) / 2.0;
            this.centerZ = (minZ + maxZ) / 2.0;

            if (auto) {
                double spanX = (maxX - minX) / 2.0;
                double spanZ = (maxZ - minZ) / 2.0;
                double detectedHalf = Math.max(spanX, spanZ);
                if (detectedHalf >= 2.0 && detectedHalf <= 12.0) {
                    this.halfSize = Math.max(defHalf, detectedHalf);
                }
            }
        }

        public boolean isInside(Vec3d pos) {
            return Math.abs(pos.x - centerX) <= halfSize && Math.abs(pos.z - centerZ) <= halfSize;
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
        if (!isEnabled() || mc.world == null) return false;
        long now = System.currentTimeMillis();

        boolean isStunCandidate = false;

        if (parameters instanceof DustParticleEffect) {
            isStunCandidate = true;
        } else if (parameters.getType() == ParticleTypes.TOTEM_OF_UNDYING ||
                   parameters.getType() == ParticleTypes.ENCHANT ||
                   parameters.getType() == ParticleTypes.ENCHANTED_HIT ||
                   parameters.getType() == ParticleTypes.CRIT ||
                   parameters.getType() == ParticleTypes.ELECTRIC_SPARK ||
                   parameters.getType() == ParticleTypes.FALLING_DUST ||
                   parameters.getType() == ParticleTypes.FLAME ||
                   parameters.getType() == ParticleTypes.END_ROD) {
            isStunCandidate = true;
        }

        if (!isStunCandidate) return false;

        double defHalf = defaultRadius.getValue();
        boolean auto = autoDetect.isEnabled();

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                double dist = Math.hypot(x - zone.centerX, z - zone.centerZ);
                if (dist <= 14.0) {
                    zone.addParticle(x, y, z, defHalf, auto, now);
                    return true;
                }
            }

            if (activeZones.size() < 3) {
                activeZones.add(new StunZone(x, y, z, defHalf, now));
                return true;
            }
        }
        return false;
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

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                if (zone.particleCount < 2) continue;

                double half = zone.halfSize;
                float x1 = (float) (Math.floor(zone.centerX - half) - camPos.x);
                float x2 = (float) (Math.ceil(zone.centerX + half) - camPos.x);
                float z1 = (float) (Math.floor(zone.centerZ - half) - camPos.z);
                float z2 = (float) (Math.ceil(zone.centerZ + half) - camPos.z);
                float y1 = (float) (Math.floor(zone.groundY) - camPos.y);
                float y2 = y1 + h;

                // 1. Semi-transparent 4 Vertical Square Walls
                if (curStyle.equals("SquareBox") || curStyle.equals("ForcefieldPrism")) {
                    BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    float wallAlpha = a * 0.32f;

                    buffer.vertex(mat, x1, y1, z1).color(r, g, b, wallAlpha);
                    buffer.vertex(mat, x2, y1, z1).color(r, g, b, wallAlpha);
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, 0.0f);
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, 0.0f);

                    buffer.vertex(mat, x2, y1, z1).color(r, g, b, wallAlpha);
                    buffer.vertex(mat, x2, y1, z2).color(r, g, b, wallAlpha);
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, 0.0f);
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, 0.0f);

                    buffer.vertex(mat, x2, y1, z2).color(r, g, b, wallAlpha);
                    buffer.vertex(mat, x1, y1, z2).color(r, g, b, wallAlpha);
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, 0.0f);
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, 0.0f);

                    buffer.vertex(mat, x1, y1, z2).color(r, g, b, wallAlpha);
                    buffer.vertex(mat, x1, y1, z1).color(r, g, b, wallAlpha);
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, 0.0f);
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, 0.0f);

                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                }

                // 2. Ground & Top Lines
                BufferBuilder lineBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                lineBuffer.vertex(mat, x1, y1 + 0.05f, z1).color(r, g, b, a);
                lineBuffer.vertex(mat, x2, y1 + 0.05f, z1).color(r, g, b, a);
                lineBuffer.vertex(mat, x2, y1 + 0.05f, z1).color(r, g, b, a);
                lineBuffer.vertex(mat, x2, y1 + 0.05f, z2).color(r, g, b, a);
                lineBuffer.vertex(mat, x2, y1 + 0.05f, z2).color(r, g, b, a);
                lineBuffer.vertex(mat, x1, y1 + 0.05f, z2).color(r, g, b, a);
                lineBuffer.vertex(mat, x1, y1 + 0.05f, z2).color(r, g, b, a);
                lineBuffer.vertex(mat, x1, y1 + 0.05f, z1).color(r, g, b, a);

                if (!curStyle.equals("SquareOutline")) {
                    lineBuffer.vertex(mat, x1, y2, z1).color(r, g, b, a * 0.6f);
                    lineBuffer.vertex(mat, x2, y2, z1).color(r, g, b, a * 0.6f);
                    lineBuffer.vertex(mat, x2, y2, z1).color(r, g, b, a * 0.6f);
                    lineBuffer.vertex(mat, x2, y2, z2).color(r, g, b, a * 0.6f);
                    lineBuffer.vertex(mat, x2, y2, z2).color(r, g, b, a * 0.6f);
                    lineBuffer.vertex(mat, x1, y2, z2).color(r, g, b, a * 0.6f);
                    lineBuffer.vertex(mat, x1, y2, z2).color(r, g, b, a * 0.6f);
                    lineBuffer.vertex(mat, x1, y2, z1).color(r, g, b, a * 0.6f);

                    lineBuffer.vertex(mat, x1, y1, z1).color(r, g, b, a);
                    lineBuffer.vertex(mat, x1, y2, z1).color(r, g, b, 0.0f);
                    lineBuffer.vertex(mat, x2, y1, z1).color(r, g, b, a);
                    lineBuffer.vertex(mat, x2, y2, z1).color(r, g, b, 0.0f);
                    lineBuffer.vertex(mat, x2, y1, z2).color(r, g, b, a);
                    lineBuffer.vertex(mat, x2, y2, z2).color(r, g, b, 0.0f);
                    lineBuffer.vertex(mat, x1, y1, z2).color(r, g, b, a);
                    lineBuffer.vertex(mat, x1, y2, z2).color(r, g, b, 0.0f);
                }
                BufferRenderer.drawWithGlobalProgram(lineBuffer.end());
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
                if (zone.particleCount >= 2 && zone.isInside(pPos)) {
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
