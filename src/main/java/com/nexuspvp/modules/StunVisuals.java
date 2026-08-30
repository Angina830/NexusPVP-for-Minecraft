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
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;

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
        public double radius; // Half-width of square (e.g. 7.0 blocks)
        public long firstSeen;
        public long lastSeen;
        public int particleCount;
        public boolean confirmed; // Only renders when confirmed by high particle density

        public StunZone(Vec3d center, double radius, long now) {
            this.center = center;
            this.radius = radius;
            this.firstSeen = now;
            this.lastSeen = now;
            this.particleCount = 1;
            this.confirmed = false;
        }

        public boolean isInside(Vec3d pos) {
            double dx = Math.abs(pos.x - center.x);
            double dz = Math.abs(pos.z - center.z);
            return dx <= radius && dz <= radius;
        }
    }

    private final List<StunZone> activeZones = new ArrayList<>();
    private float pulseAnim = 0f;

    // Minimum particles in a cluster required to confirm a real HolyWorld Stun (avoids crits/torches)
    private static final int CONFIRMATION_THRESHOLD = 8;

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

        // 1. Check for Dust particles with yellow/gold/amber or red/orange hue
        if (parameters instanceof DustParticleEffect) {
            DustParticleEffect dust = (DustParticleEffect) parameters;
            Vec3f col = dust.getColor();
            float r = col.getX();
            float g = col.getY();
            float b = col.getZ();
            // Yellow / Gold / Amber / Flame dust check
            if (r > 0.5f && g > 0.4f && b < 0.6f) {
                isStunCandidate = true;
            }
        } else if (parameters.getType() == ParticleTypes.TOTEM_OF_UNDYING ||
                   parameters.getType() == ParticleTypes.ENCHANT ||
                   parameters.getType() == ParticleTypes.ENCHANTED_HIT) {
            isStunCandidate = true;
        }

        // Ignore ordinary water, smoke, heart, portal, or sword crit particles!
        if (!isStunCandidate) return false;

        Vec3d pPos = new Vec3d(x, y, z);
        double rad = defaultRadius.getValue();

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                double dx = Math.abs(pPos.x - zone.center.x);
                double dz = Math.abs(pPos.z - zone.center.z);
                if (dx <= rad + 2.5 && dz <= rad + 2.5) {
                    zone.lastSeen = now;
                    zone.particleCount++;

                    // Once threshold is reached, zone is confirmed as a HolyWorld Stun!
                    if (zone.particleCount >= CONFIRMATION_THRESHOLD) {
                        zone.confirmed = true;
                    }

                    if (autoDetect.isEnabled() && zone.confirmed) {
                        double maxOffset = Math.max(dx, dz);
                        if (maxOffset > 2.0 && maxOffset < 15.0) {
                            zone.radius = zone.radius * 0.96 + maxOffset * 0.04;
                        }
                    }
                    // Only hide particle if zone is confirmed
                    return zone.confirmed;
                }
            }

            // Only track candidate cluster if close to ground (stuns are placed on blocks)
            activeZones.add(new StunZone(pPos, rad, now));
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
                // Unconfirmed clusters die after 1.5 seconds; confirmed stuns die after 6.5s without particles
                long timeout = z.confirmed ? 6500 : 1500;
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
                // ONLY RENDER CONFIRMED STUN ZONES!
                if (!zone.confirmed) continue;

                double cx = zone.center.x - camPos.x;
                double cy = zone.center.y - camPos.y;
                double cz = zone.center.z - camPos.z;
                double rad = zone.radius;

                double x1 = cx - rad;
                double x2 = cx + rad;
                double z1 = cz - rad;
                double z2 = cz + rad;
                double y1 = cy;
                double y2 = cy + h;

                // 1. Semi-transparent 4 Vertical Square Walls
                if (curStyle.equals("SquareBox") || curStyle.equals("ForcefieldPrism")) {
                    buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
                    int wallAlpha = Math.min(255, (int) (a * 0.35f));

                    addWallQuad(buffer, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, r, g, b, wallAlpha);
                    addWallQuad(buffer, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, r, g, b, wallAlpha);
                    addWallQuad(buffer, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, r, g, b, wallAlpha);
                    addWallQuad(buffer, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, r, g, b, wallAlpha);

                    tessellator.draw();
                }

                // 2. Ground Neon Square
                GL11.glLineWidth(lw);
                buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
                buffer.vertex(x1, y1 + 0.05, z1).color(r, g, b, a).next();
                buffer.vertex(x2, y1 + 0.05, z1).color(r, g, b, a).next();
                buffer.vertex(x2, y1 + 0.05, z2).color(r, g, b, a).next();
                buffer.vertex(x1, y1 + 0.05, z2).color(r, g, b, a).next();
                tessellator.draw();

                // 3. Top Rim Square & Corner Pillars
                if (!curStyle.equals("SquareOutline")) {
                    buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
                    buffer.vertex(x1, y2, z1).color(r, g, b, (int) (a * 0.5f)).next();
                    buffer.vertex(x2, y2, z1).color(r, g, b, (int) (a * 0.5f)).next();
                    buffer.vertex(x2, y2, z2).color(r, g, b, (int) (a * 0.5f)).next();
                    buffer.vertex(x1, y2, z2).color(r, g, b, (int) (a * 0.5f)).next();
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
                if (zone.confirmed && zone.isInside(pPos)) {
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
