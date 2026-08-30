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
    private final NumberSetting height = addSetting(new NumberSetting("Height", 18.0, 3.0, 30.0, 1.0));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 3.5, 1.0, 6.0, 0.5));
    private final BooleanSetting pulse = addSetting(new BooleanSetting("Pulsing", true));
    private final BooleanSetting hideParticles = addSetting(new BooleanSetting("HideParticles", true));
    private final BooleanSetting warning = addSetting(new BooleanSetting("Warning", true));
    private final BooleanSetting testMode = addSetting(new BooleanSetting("TestMode", false));

    public static class StunZone {
        public double minX, maxX;
        public double minZ, maxZ;
        public double groundY, maxY;
        public double centerX, centerZ;
        public long firstSeen;
        public long lastSeen;
        public int particleCount;
        public boolean confirmed;

        public StunZone(double x, double y, double z, long now) {
            this.minX = x;
            this.maxX = x;
            this.minZ = z;
            this.maxZ = z;
            this.groundY = y;
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
            this.groundY = Math.min(groundY, y);
            this.maxY = Math.max(maxY, y);
            this.centerX = (minX + maxX) / 2.0;
            this.centerZ = (minZ + maxZ) / 2.0;

            // Confirm when burst of at least 6 particles spans over 3.0 blocks
            if (!confirmed && particleCount >= 6 && ((maxX - minX) >= 3.0 || (maxZ - minZ) >= 3.0 || (maxY - groundY) >= 3.0)) {
                this.confirmed = true;
            }
        }

        public boolean isInside(Vec3d pos) {
            double half = 15.0;
            return Math.abs(pos.x - centerX) <= half && Math.abs(pos.z - centerZ) <= half;
        }
    }

    private final List<StunZone> activeZones = new ArrayList<>();
    private float pulseAnim = 0f;
    private StunZone testZone = null;

    public StunVisuals() {
        super("StunVisuals", "HolyWorld 30x30x30 Square Stun Trap / Anti-Pearl Zone continuous neon 3D barrier", Category.VISUAL);
        setEnabled(true);
    }

    public boolean isHideParticles() {
        return isEnabled() && hideParticles.isEnabled();
    }

    public boolean handleParticle(ParticleEffect parameters, double x, double y, double z) {
        if (!isEnabled() || mc.world == null || mc.player == null) return false;
        long now = System.currentTimeMillis();

        boolean isStunDust = false;

        if (parameters instanceof DustParticleEffect) {
            DustParticleEffect dust = (DustParticleEffect) parameters;
            float r = dust.getRed();
            float g = dust.getGreen();
            float b = dust.getBlue();
            // Yellow, Gold, Amber, Orange Dust
            if (r > 0.40f && g > 0.25f && b < 0.60f) {
                isStunDust = true;
            }
        } else if (parameters.getType() == ParticleTypes.TOTEM_OF_UNDYING ||
                   parameters.getType() == ParticleTypes.ENCHANT ||
                   parameters.getType() == ParticleTypes.FALLING_DUST ||
                   parameters.getType() == ParticleTypes.CRIT ||
                   parameters.getType() == ParticleTypes.FLAME) {
            isStunDust = true;
        }

        if (!isStunDust) return false;

        Vec3d pPos = mc.player.getPos();
        if (Math.hypot(x - pPos.x, z - pPos.z) > 60.0) return false;

        synchronized (activeZones) {
            // Cluster check within 22.0 blocks horizontal & 35.0 blocks vertical (full 30x30x30 cube volume)
            for (StunZone zone : activeZones) {
                double dist = Math.hypot(x - zone.centerX, z - zone.centerZ);
                if (dist <= 22.0 && Math.abs(y - zone.groundY) <= 35.0) {
                    zone.addParticle(x, y, z, now);
                    return true; // 100% Silence ALL server particles (ground runes & sky box)
                }
            }

            if (activeZones.size() < 6) {
                activeZones.add(new StunZone(x, y, z, now));
                return true; // Silence initial particle
            }
        }
        return false;
    }

    @Override
    public void onTick() {
        pulseAnim += 0.04f;
        if (mc.world == null || mc.player == null) return;
        long now = System.currentTimeMillis();

        if (testMode.isEnabled()) {
            if (testZone == null) {
                Vec3d pPos = mc.player.getPos();
                testZone = new StunZone(pPos.x - 15.0, pPos.y, pPos.z - 15.0, now);
                testZone.maxX = pPos.x + 15.0;
                testZone.maxZ = pPos.z + 15.0;
                testZone.maxY = pPos.y + 18.0;
                testZone.confirmed = true;
                testZone.particleCount = 50;
            }
            testZone.lastSeen = now;
        } else {
            testZone = null;
        }

        synchronized (activeZones) {
            Iterator<StunZone> it = activeZones.iterator();
            while (it.hasNext()) {
                StunZone z = it.next();
                // Purge unconfirmed false ambient particles after 800ms
                if (!z.confirmed && (now - z.firstSeen > 800)) {
                    it.remove();
                    continue;
                }
                // Confirmed 30x30 Stun Trap lasts exactly 15 seconds
                if (z.confirmed && (now - z.lastSeen > 15500 || now - z.firstSeen > 16000)) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null) return;
        if (activeZones.isEmpty() && testZone == null) return;

        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        Color baseCol = color.getColor();
        float pulseMul = pulse.isEnabled() ? (0.85f + (float) Math.sin(pulseAnim * 3.5f) * 0.15f) : 1.0f;
        
        int r = (int) (baseCol.getRed() * pulseMul);
        int g = (int) (baseCol.getGreen() * pulseMul);
        int b = (int) (baseCol.getBlue() * pulseMul);
        int a = baseCol.getAlpha();

        String curStyle = style.getValue();
        float userH = height.getFloatValue();
        float lw = lineWidth.getFloatValue();

        RenderSystem.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.disableTexture();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        List<StunZone> toRender = new ArrayList<>();
        if (testZone != null) {
            toRender.add(testZone);
        }
        synchronized (activeZones) {
            for (StunZone z : activeZones) {
                if (z.confirmed) toRender.add(z);
            }
        }

        for (StunZone zone : toRender) {
            double cx = Math.floor(zone.centerX) + 0.5;
            double cz = Math.floor(zone.centerZ) + 0.5;
            double half = 15.0; // Official HolyWorld 30x30 footprint

            double x1 = (cx - half) - camPos.x;
            double x2 = (cx + half) - camPos.x;
            double z1 = (cz - half) - camPos.z;
            double z2 = (cz + half) - camPos.z;
            double y1 = (Math.floor(zone.groundY)) - camPos.y;
            double y2 = y1 + userH;

            // 1. Semi-transparent 4 Vertical Square Walls (30x30 footprint)
            if (curStyle.equals("SquareBox") || curStyle.equals("ForcefieldPrism")) {
                buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
                int wallAlpha = Math.min(255, (int) (a * 0.28f));

                addWallQuad(buffer, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, r, g, b, wallAlpha);
                addWallQuad(buffer, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, r, g, b, wallAlpha);
                addWallQuad(buffer, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, r, g, b, wallAlpha);
                addWallQuad(buffer, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, r, g, b, wallAlpha);

                tessellator.draw();
            }

            // 2. Thick Glowing Ground Neon Square (30x30 footprint)
            GL11.glLineWidth(lw);
            buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
            buffer.vertex(x1, y1 + 0.05, z1).color(r, g, b, a).next();
            buffer.vertex(x2, y1 + 0.05, z1).color(r, g, b, a).next();
            buffer.vertex(x2, y1 + 0.05, z2).color(r, g, b, a).next();
            buffer.vertex(x1, y1 + 0.05, z2).color(r, g, b, a).next();
            tessellator.draw();

            // 3. Top Rim Square & 4 Vertical Corner Neon Pillars
            if (!curStyle.equals("SquareOutline")) {
                buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
                buffer.vertex(x1, y2, z1).color(r, g, b, (int) (a * 0.7f)).next();
                buffer.vertex(x2, y2, z1).color(r, g, b, (int) (a * 0.7f)).next();
                buffer.vertex(x2, y2, z2).color(r, g, b, (int) (a * 0.7f)).next();
                buffer.vertex(x1, y2, z2).color(r, g, b, (int) (a * 0.7f)).next();
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
        if (!warning.isEnabled() || mc.player == null) return;

        Vec3d pPos = mc.player.getPos();
        boolean insideAny = (testZone != null && testZone.isInside(pPos));

        if (!insideAny) {
            synchronized (activeZones) {
                for (StunZone zone : activeZones) {
                    if (zone.confirmed && zone.isInside(pPos)) {
                        insideAny = true;
                        break;
                    }
                }
            }
        }

        if (insideAny) {
            int screenW = mc.getWindow().getScaledWidth();
            int screenH = mc.getWindow().getScaledHeight();
            String text = "§e§l⚠ SQUARE STUN ZONE (30x30) - NO PEARLS! ⚠";
            int textW = mc.textRenderer.getWidth(text);
            int x = screenW / 2 - textW / 2;
            int y = screenH / 2 + 35;

            RenderUtils.drawRoundedRect(matrices, x - 6, y - 3, textW + 12, 16, 4, 0xDD1E1F22);
            RenderUtils.drawRoundedRect(matrices, x - 7, y - 4, textW + 14, 18, 5, 0xEEFFCC00);
            mc.textRenderer.drawWithShadow(matrices, text, x, y + 1, 0xFFFFFFFF);
        }
    }
}
