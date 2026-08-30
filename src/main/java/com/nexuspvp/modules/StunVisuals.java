package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

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

        // Broad particle type matching for all HolyWorld / Saturn / FunTime stun plugins
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
                // If particle is within 14 blocks of the stun zone center, it belongs to this trap
                if (dist <= 14.0) {
                    zone.addParticle(x, y, z, defHalf, auto, now);
                    return true;
                }
            }

            // Create new stun zone (maximum 3 active traps)
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
                // Stun zone stays active for 7 seconds after last particle
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
        
        int r = (int) (baseCol.getRed() * pulseMul);
        int g = (int) (baseCol.getGreen() * pulseMul);
        int b = (int) (baseCol.getBlue() * pulseMul);
        int a = baseCol.getAlpha();

        String curStyle = style.getValue();
        float h = height.getFloatValue();
        float lw = lineWidth.getFloatValue();

        RenderSystem.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.disableTexture();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                // Minimum 2 particles to start rendering the stun trap
                if (zone.particleCount < 2) continue;

                double half = zone.halfSize;
                double x1 = (Math.floor(zone.centerX - half)) - camPos.x;
                double x2 = (Math.ceil(zone.centerX + half)) - camPos.x;
                double z1 = (Math.floor(zone.centerZ - half)) - camPos.z;
                double z2 = (Math.ceil(zone.centerZ + half)) - camPos.z;
                double y1 = (Math.floor(zone.groundY)) - camPos.y;
                double y2 = y1 + h;

                // 1. Semi-transparent 4 Vertical Square Walls
                if (curStyle.equals("SquareBox") || curStyle.equals("ForcefieldPrism")) {
                    buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
                    int wallAlpha = Math.min(255, (int) (a * 0.32f));

                    addWallQuad(buffer, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, r, g, b, wallAlpha);
                    addWallQuad(buffer, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, r, g, b, wallAlpha);
                    addWallQuad(buffer, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, r, g, b, wallAlpha);
                    addWallQuad(buffer, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, r, g, b, wallAlpha);

                    tessellator.draw();
                }

                // 2. Thick Glowing Ground Neon Square
                GL11.glLineWidth(lw);
                buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
                buffer.vertex(x1, y1 + 0.05, z1).color(r, g, b, a).next();
                buffer.vertex(x2, y1 + 0.05, z1).color(r, g, b, a).next();
                buffer.vertex(x2, y1 + 0.05, z2).color(r, g, b, a).next();
                buffer.vertex(x1, y1 + 0.05, z2).color(r, g, b, a).next();
                tessellator.draw();

                // 3. Top Rim Square & 4 Vertical Corner Pillars
                if (!curStyle.equals("SquareOutline")) {
                    buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
                    buffer.vertex(x1, y2, z1).color(r, g, b, (int) (a * 0.6f)).next();
                    buffer.vertex(x2, y2, z1).color(r, g, b, (int) (a * 0.6f)).next();
                    buffer.vertex(x2, y2, z2).color(r, g, b, (int) (a * 0.6f)).next();
                    buffer.vertex(x1, y2, z2).color(r, g, b, (int) (a * 0.6f)).next();
                    tessellator.draw();

                    buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
                    buffer.vertex(x1, y1, z1).color(r, g, b, a).next();
                    buffer.vertex(x1, y2, z1).color(r, g, b, 0).next();
                    buffer.vertex(x2, y1, z1).color(r, g, b, a).next();
                    buffer.vertex(x2, y2, z1).color(r, g, b, 0).next();
                    buffer.vertex(x2, y1, z2).color(r, g, b, a).next();
                    buffer.vertex(x2, y2, z2).color(r, g, b, 0).next();
                    buffer.vertex(x1, y1, z2).color(r, g, b, a).next();
                    buffer.vertex(x1, y2, z2).color(r, g, b, 0).next();
                    tessellator.draw();
                }
            }
        }

        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private void addWallQuad(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2,
                             double x3, double y3, double z3, double x4, double y4, double z4,
                             int r, int g, int b, int a) {
        buffer.vertex(x1, y1, z1).color(r, g, b, a).next();
        buffer.vertex(x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(x3, y3, z3).color(r, g, b, 0).next();
        buffer.vertex(x4, y4, z4).color(r, g, b, 0).next();
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
            int screenW = mc.getWindow().getScaledWidth();
            int screenH = mc.getWindow().getScaledHeight();
            String text = "§e§l⚠ SQUARE STUN ZONE - NO PEARLS! ⚠";
            int textW = mc.textRenderer.getWidth(text);
            int x = screenW / 2 - textW / 2;
            int y = screenH / 2 + 35;

            RenderUtils.drawRoundedRect(matrices, x - 6, y - 3, textW + 12, 16, 4, 0xDD1E1F22);
            RenderUtils.drawRoundedRect(matrices, x - 7, y - 4, textW + 14, 18, 5, 0xEEFFCC00);
            mc.textRenderer.drawWithShadow(matrices, text, x, y + 1, 0xFFFFFFFF);
        }
    }
}
