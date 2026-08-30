package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.gui.ThemeManager;
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

        // Check if particle matches HolyWorld Stun particles (Dust / Crit / Flame / Totem)
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

            // Create new stun zone if far from existing
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
                // Stun zone expires after 7 seconds without new particles
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
                double cx = zone.center.x - camPos.x;
                double cy = zone.center.y - camPos.y;
                double cz = zone.center.z - camPos.z;
                double rad = zone.radius;

                int segments = 64;

                // 1. Semi-transparent forcefield cylinder wall
                if (curStyle.equals("Cylinder") || curStyle.equals("Forcefield")) {
                    buffer.begin(GL11.GL_QUAD_STRIP, VertexFormats.POSITION_COLOR);
                    int wallAlpha = Math.min(255, (int) (a * 0.35f));
                    for (int i = 0; i <= segments; i++) {
                        double angle = 2 * Math.PI * i / segments;
                        double px = cx + Math.cos(angle) * rad;
                        double pz = cz + Math.sin(angle) * rad;

                        // Bottom vertex (full opacity)
                        buffer.vertex(px, cy, pz).color(r, g, b, wallAlpha).next();
                        // Top vertex (faded opacity)
                        buffer.vertex(px, cy + h, pz).color(r, g, b, 0).next();
                    }
                    tessellator.draw();
                }

                // 2. Ground Neon Ring
                GL11.glLineWidth(lw);
                buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
                for (int i = 0; i < segments; i++) {
                    double angle = 2 * Math.PI * i / segments;
                    double px = cx + Math.cos(angle) * rad;
                    double pz = cz + Math.sin(angle) * rad;
                    buffer.vertex(px, cy + 0.05, pz).color(r, g, b, a).next();
                }
                tessellator.draw();

                // 3. Floating Waist / Chest Ring
                if (!curStyle.equals("Ring")) {
                    buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < segments; i++) {
                        double angle = 2 * Math.PI * i / segments;
                        double px = cx + Math.cos(angle) * rad;
                        double pz = cz + Math.sin(angle) * rad;
                        buffer.vertex(px, cy + h * 0.5, pz).color(r, g, b, (int) (a * 0.6f)).next();
                    }
                    tessellator.draw();
                }

                // 4. Vertical Grid Pillars (for Wireframe / Forcefield)
                if (curStyle.equals("Wireframe") || curStyle.equals("Forcefield")) {
                    buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < 16; i++) {
                        double angle = 2 * Math.PI * i / 16;
                        double px = cx + Math.cos(angle) * rad;
                        double pz = cz + Math.sin(angle) * rad;
                        buffer.vertex(px, cy, pz).color(r, g, b, (int) (a * 0.7f)).next();
                        buffer.vertex(px, cy + h, pz).color(r, g, b, 0).next();
                    }
                    tessellator.draw();
                }
            }
        }

        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (!warning.isEnabled() || mc.player == null || activeZones.isEmpty()) return;

        Vec3d pPos = mc.player.getPos();
        boolean insideAny = false;
        double minDistance = Double.MAX_VALUE;

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                if (zone.isInside(pPos)) {
                    insideAny = true;
                    break;
                }
                double dist = Math.hypot(pPos.x - zone.center.x, pPos.z - zone.center.z) - zone.radius;
                if (dist < minDistance) minDistance = dist;
            }
        }

        if (insideAny) {
            int screenW = mc.getWindow().getScaledWidth();
            int screenH = mc.getWindow().getScaledHeight();
            String text = "§e§l⚠ STUN ZONE - NO PEARLS! ⚠";
            int textW = mc.textRenderer.getWidth(text);
            int x = screenW / 2 - textW / 2;
            int y = screenH / 2 + 35;

            RenderUtils.drawRoundedRect(matrices, x - 6, y - 3, textW + 12, 16, 4, 0xDD1E1F22);
            RenderUtils.drawRoundedRect(matrices, x - 7, y - 4, textW + 14, 18, 5, 0xEEFFCC00);
            mc.textRenderer.drawWithShadow(matrices, text, x, y + 1, 0xFFFFFFFF);
        }
    }
}
