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
import org.joml.Vector3f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StunVisuals extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "SquareBox", "SquareBox", "SquareOutline", "ForcefieldPrism", "WireframeCube"));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 215, 0, 190)));
    private final NumberSetting defaultRadius = addSetting(new NumberSetting("HalfSize", 7.0, 2.0, 15.0, 0.5));
    private final BooleanSetting autoDetect = addSetting(new BooleanSetting("AutoDetect", true));
    private final NumberSetting height = addSetting(new NumberSetting("Height", 4.0, 1.0, 8.0, 0.5));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 5.0, 0.5));
    private final BooleanSetting pulse = addSetting(new BooleanSetting("Pulsing", true));
    private final BooleanSetting hideParticles = addSetting(new BooleanSetting("HideParticles", true));
    private final BooleanSetting warning = addSetting(new BooleanSetting("Warning", true));

    public static class StunZone {
        public double minX, maxX;
        public double minZ, maxZ;
        public double minY, maxY;
        public double centerX, centerZ;
        public long firstSeen;
        public long lastSeen;
        public int particleCount;
        public boolean confirmed;

        public StunZone(double x, double y, double z, double defaultHalf, long now) {
            this.minX = x;
            this.maxX = x;
            this.minZ = z;
            this.maxZ = z;
            this.minY = y;
            this.maxY = y;
            this.centerX = x;
            this.centerZ = z;
            this.firstSeen = now;
            this.lastSeen = now;
            this.particleCount = 1;
            this.confirmed = false;
        }

        public void addParticle(double x, double y, double z, long now) {
            this.lastSeen = now;
            this.particleCount++;
            this.minX = Math.min(minX, x);
            this.maxX = Math.max(maxX, x);
            this.minZ = Math.min(minZ, z);
            this.maxZ = Math.max(maxZ, z);
            this.minY = Math.min(minY, y);
            this.maxY = Math.max(maxY, y);

            this.centerX = (minX + maxX) / 2.0;
            this.centerZ = (minZ + maxZ) / 2.0;

            if (particleCount >= 15 && (maxX - minX > 3.0 || maxZ - minZ > 3.0)) {
                this.confirmed = true;
            }
        }

        public boolean isInside(Vec3d pos, double fallbackHalf) {
            double hx = confirmed ? Math.max((maxX - minX) / 2.0, fallbackHalf) : fallbackHalf;
            double hz = confirmed ? Math.max((maxZ - minZ) / 2.0, fallbackHalf) : fallbackHalf;
            return Math.abs(pos.x - centerX) <= hx && Math.abs(pos.z - centerZ) <= hz;
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
            DustParticleEffect dust = (DustParticleEffect) parameters;
            Vector3f col = dust.getColor();
            float r = col.x();
            float g = col.y();
            float b = col.z();
            if (r > 0.5f && g > 0.4f && b < 0.6f) {
                isStunCandidate = true;
            }
        } else if (parameters.getType() == ParticleTypes.TOTEM_OF_UNDYING ||
                   parameters.getType() == ParticleTypes.ENCHANT ||
                   parameters.getType() == ParticleTypes.ENCHANTED_HIT) {
            isStunCandidate = true;
        }

        if (!isStunCandidate) return false;

        double defHalf = defaultRadius.getValue();

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                double dist = Math.hypot(x - zone.centerX, z - zone.centerZ);
                if (dist <= 14.0) {
                    zone.addParticle(x, y, z, now);
                    return zone.confirmed;
                }
            }

            if (activeZones.size() < 4) {
                activeZones.add(new StunZone(x, y, z, defHalf, now));
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
                long timeout = z.confirmed ? 6000 : 1200;
                if (now - z.lastSeen > timeout) {
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
        double defHalf = defaultRadius.getValue();

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
                if (!zone.confirmed) continue;

                double halfX = autoDetect.isEnabled() ? Math.max((zone.maxX - zone.minX) / 2.0, defHalf) : defHalf;
                double halfZ = autoDetect.isEnabled() ? Math.max((zone.maxZ - zone.minZ) / 2.0, defHalf) : defHalf;

                float x1 = (float) (Math.floor(zone.centerX - halfX) - camPos.x);
                float x2 = (float) (Math.ceil(zone.centerX + halfX) - camPos.x);
                float z1 = (float) (Math.floor(zone.centerZ - halfZ) - camPos.z);
                float z2 = (float) (Math.ceil(zone.centerZ + halfZ) - camPos.z);
                float y1 = (float) (Math.floor(zone.minY) - camPos.y);
                float y2 = y1 + h;

                // 1. Semi-transparent 4 Vertical Square Walls
                if (curStyle.equals("SquareBox") || curStyle.equals("ForcefieldPrism")) {
                    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    float wallAlpha = a * 0.35f;

                    buffer.vertex(mat, x1, y1, z1).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x2, y1, z1).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, 0.0f).next();

                    buffer.vertex(mat, x2, y1, z1).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x2, y1, z2).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, 0.0f).next();

                    buffer.vertex(mat, x2, y1, z2).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x1, y1, z2).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, 0.0f).next();

                    buffer.vertex(mat, x1, y1, z2).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x1, y1, z1).color(r, g, b, wallAlpha).next();
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, 0.0f).next();
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, 0.0f).next();

                    tessellator.draw();
                }

                // 2. Ground & Top Lines
                buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                buffer.vertex(mat, x1, y1 + 0.05f, z1).color(r, g, b, a).next();
                buffer.vertex(mat, x2, y1 + 0.05f, z1).color(r, g, b, a).next();
                buffer.vertex(mat, x2, y1 + 0.05f, z1).color(r, g, b, a).next();
                buffer.vertex(mat, x2, y1 + 0.05f, z2).color(r, g, b, a).next();
                buffer.vertex(mat, x2, y1 + 0.05f, z2).color(r, g, b, a).next();
                buffer.vertex(mat, x1, y1 + 0.05f, z2).color(r, g, b, a).next();
                buffer.vertex(mat, x1, y1 + 0.05f, z2).color(r, g, b, a).next();
                buffer.vertex(mat, x1, y1 + 0.05f, z1).color(r, g, b, a).next();

                if (!curStyle.equals("SquareOutline")) {
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x2, y2, z1).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x2, y2, z2).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x1, y2, z2).color(r, g, b, a * 0.5f).next();
                    buffer.vertex(mat, x1, y2, z1).color(r, g, b, a * 0.5f).next();

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
        double defHalf = defaultRadius.getValue();

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                if (zone.confirmed && zone.isInside(pPos, defHalf)) {
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
