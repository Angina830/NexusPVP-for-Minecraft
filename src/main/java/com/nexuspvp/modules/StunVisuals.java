package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StunVisuals extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "SquareBox", "SquareBox", "SquareOutline", "ForcefieldPrism", "WireframeCube"));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 215, 0, 230)));
    private final NumberSetting height = addSetting(new NumberSetting("Height", 25.0, 5.0, 35.0, 1.0));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 3.5, 1.0, 6.0, 0.5));
    private final BooleanSetting pulse = addSetting(new BooleanSetting("Pulsing", true));
    private final BooleanSetting hideParticles = addSetting(new BooleanSetting("HideParticles", true));
    private final BooleanSetting warning = addSetting(new BooleanSetting("Warning", true));
    private final BooleanSetting testMode = addSetting(new BooleanSetting("TestMode", false));

    public static class StunZone {
        public double minX, maxX;
        public double minZ, maxZ;
        public double groundY;
        public double centerX, centerZ;
        public long firstSeen;
        public long lastSeen;
        public int particleCount;

        public StunZone(double x, double y, double z, long now) {
            this.minX = x;
            this.maxX = x;
            this.minZ = z;
            this.maxZ = z;
            this.groundY = y;
            this.centerX = x;
            this.centerZ = z;
            this.firstSeen = now;
            this.lastSeen = now;
            this.particleCount = 1;
        }

        public void addParticle(double x, double y, double z, long now) {
            this.lastSeen = now;
            this.particleCount++;
            this.minX = Math.min(minX, x);
            this.maxX = Math.max(maxX, x);
            this.minZ = Math.min(minZ, z);
            this.maxZ = Math.max(maxZ, z);
            this.groundY = Math.min(groundY, y);
            this.centerX = (minX + maxX) / 2.0;
            this.centerZ = (minZ + maxZ) / 2.0;
        }

        public boolean isConfirmed() {
            return particleCount >= 8 && ((maxX - minX) >= 4.0 || (maxZ - minZ) >= 4.0);
        }

        public boolean isInside(Vec3d pos) {
            return pos.x >= (minX - 0.5) && pos.x <= (maxX + 0.5) &&
                   pos.z >= (minZ - 0.5) && pos.z <= (maxZ + 0.5);
        }
    }

    private final List<StunZone> activeZones = new ArrayList<>();
    private float pulseAnim = 0f;
    private StunZone testZone = null;

    public StunVisuals() {
        super("StunVisuals", "HolyWorld 30x30 Square Stun Trap / Anti-Pearl Zone continuous neon 3D barrier", Category.VISUAL);
        setEnabled(true);
    }

    public boolean isHideParticles() {
        return isEnabled() && hideParticles.isEnabled();
    }

    public boolean handleParticle(ParticleEffect parameters, double x, double y, double z) {
        if (!isEnabled() || mc.world == null || mc.player == null) return false;
        long now = System.currentTimeMillis();

        // Strict HolyWorld Stun Particle Signature: "minecraft:dust 1.00 1.00 0.00 1.00"
        boolean isExactYellowStunDust = false;
        if (parameters instanceof DustParticleEffect) {
            DustParticleEffect dust = (DustParticleEffect) parameters;
            float r = dust.getRed();
            float g = dust.getGreen();
            float b = dust.getBlue();
            if (r >= 0.80f && g >= 0.80f && b <= 0.25f) {
                isExactYellowStunDust = true;
            }
        } else if (parameters.asString().contains("dust 1.00 1.00 0.00") || parameters.asString().contains("dust 1.0 1.0 0.0")) {
            isExactYellowStunDust = true;
        }

        if (!isExactYellowStunDust) return false;

        Vec3d pPos = mc.player.getPos();
        if (Math.hypot(x - pPos.x, z - pPos.z) > 60.0) return false;

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                double dist = Math.hypot(x - zone.centerX, z - zone.centerZ);
                if (dist <= 25.0) {
                    zone.addParticle(x, y, z, now);
                    return true; // 100% Silence ALL server yellow stun particles
                }
            }

            if (activeZones.size() < 4) {
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
                testZone.groundY = pPos.y;
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
                if (!z.isConfirmed() && (now - z.firstSeen > 1200)) {
                    it.remove();
                    continue;
                }
                if (z.isConfirmed() && (now - z.lastSeen > 15500 || now - z.firstSeen > 16000)) {
                    it.remove();
                }
            }
        }
    }

    private double getSurfaceFloorY(double cx, double cz, double fallbackY) {
        if (mc.world == null) return fallbackY;
        int bx = (int) Math.floor(cx);
        int bz = (int) Math.floor(cz);
        int startY = (int) Math.floor(mc.player != null ? mc.player.getY() + 3.0 : fallbackY + 3.0);
        
        BlockPos.Mutable mPos = new BlockPos.Mutable(bx, startY, bz);
        while (mPos.getY() > 1) {
            BlockState state = mc.world.getBlockState(mPos);
            if (!state.isAir() && !state.getMaterial().isLiquid()) {
                return mPos.getY() + 1.0;
            }
            mPos.setY(mPos.getY() - 1);
        }
        return fallbackY;
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null) return;
        if (activeZones.isEmpty() && testZone == null) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Color baseCol = color.getColor();
        float pulseMul = pulse.isEnabled() ? (0.85f + (float) Math.sin(pulseAnim * 3.5f) * 0.15f) : 1.0f;
        
        int r = (int) (baseCol.getRed() * pulseMul);
        int g = (int) (baseCol.getGreen() * pulseMul);
        int b = (int) (baseCol.getBlue() * pulseMul);
        int a = baseCol.getAlpha();

        String curStyle = style.getValue();
        float userH = Math.max(22.0f, height.getFloatValue());
        float lw = lineWidth.getFloatValue();

        List<StunZone> toRender = new ArrayList<>();
        if (testZone != null) {
            toRender.add(testZone);
        }
        synchronized (activeZones) {
            for (StunZone z : activeZones) {
                if (z.isConfirmed()) toRender.add(z);
            }
        }

        // Lock rendering firmly to World Coordinates
        RenderUtils.setupBloom3D();
        RenderSystem.pushMatrix();
        RenderSystem.translated(-cam.x, -cam.y, -cam.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (StunZone zone : toRender) {
            double pad = 0.2;
            float minX = (float) (zone.minX - pad);
            float maxX = (float) (zone.maxX + pad);
            float minZ = (float) (zone.minZ - pad);
            float maxZ = (float) (zone.maxZ + pad);

            double surfaceFloor = getSurfaceFloorY(zone.centerX, zone.centerZ, zone.groundY);
            float minY = (float) surfaceFloor;
            float maxY = minY + userH;

            // 1. Semi-transparent 4 Vertical Square Walls
            if (curStyle.equals("SquareBox") || curStyle.equals("ForcefieldPrism")) {
                int wallAlphaBottom = Math.min(255, (int) (a * 0.35f));
                int wallAlphaTop = Math.min(255, (int) (a * 0.12f));

                buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);

                // North Wall (Z = minZ)
                buffer.vertex(minX, minY, minZ).color(r, g, b, wallAlphaBottom).next();
                buffer.vertex(maxX, minY, minZ).color(r, g, b, wallAlphaBottom).next();
                buffer.vertex(maxX, maxY, minZ).color(r, g, b, wallAlphaTop).next();
                buffer.vertex(minX, maxY, minZ).color(r, g, b, wallAlphaTop).next();

                // East Wall (X = maxX)
                buffer.vertex(maxX, minY, minZ).color(r, g, b, wallAlphaBottom).next();
                buffer.vertex(maxX, minY, maxZ).color(r, g, b, wallAlphaBottom).next();
                buffer.vertex(maxX, maxY, maxZ).color(r, g, b, wallAlphaTop).next();
                buffer.vertex(maxX, maxY, minZ).color(r, g, b, wallAlphaTop).next();

                // South Wall (Z = maxZ)
                buffer.vertex(maxX, minY, maxZ).color(r, g, b, wallAlphaBottom).next();
                buffer.vertex(minX, minY, maxZ).color(r, g, b, wallAlphaBottom).next();
                buffer.vertex(minX, maxY, maxZ).color(r, g, b, wallAlphaTop).next();
                buffer.vertex(maxX, maxY, maxZ).color(r, g, b, wallAlphaTop).next();

                // West Wall (X = minX)
                buffer.vertex(minX, minY, maxZ).color(r, g, b, wallAlphaBottom).next();
                buffer.vertex(minX, minY, minZ).color(r, g, b, wallAlphaBottom).next();
                buffer.vertex(minX, maxY, minZ).color(r, g, b, wallAlphaTop).next();
                buffer.vertex(minX, maxY, maxZ).color(r, g, b, wallAlphaTop).next();

                tessellator.draw();
            }

            // 2. Exact Outline Box
            GL11.glLineWidth(lw);
            buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);

            // Ground Floor Rim
            buffer.vertex(minX, minY + 0.03f, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY + 0.03f, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY + 0.03f, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY + 0.03f, maxZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY + 0.03f, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, minY + 0.03f, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, minY + 0.03f, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, minY + 0.03f, minZ).color(r, g, b, a).next();

            // Sky Rim
            buffer.vertex(minX, maxY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, maxY, minZ).color(r, g, b, a).next();

            // 4 Vertical Corner Pillars
            buffer.vertex(minX, minY, minZ).color(r, g, b, a).next();
            buffer.vertex(minX, maxY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, minY, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).next();

            tessellator.draw();
        }

        RenderSystem.popMatrix();
        RenderUtils.cleanupBloom3D();
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (!warning.isEnabled() || mc.player == null) return;

        Vec3d pPos = mc.player.getPos();
        boolean insideAny = (testZone != null && testZone.isInside(pPos));

        if (!insideAny) {
            synchronized (activeZones) {
                for (StunZone zone : activeZones) {
                    if (zone.isConfirmed() && zone.isInside(pPos)) {
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
