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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StunVisuals extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "SquareBox", "SquareBox", "SquareOutline", "ForcefieldPrism", "WireframeCube"));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 215, 0, 220)));
    private final NumberSetting height = addSetting(new NumberSetting("Height", 4.0, 1.0, 15.0, 0.5));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 3.5, 1.0, 6.0, 0.5));
    private final BooleanSetting pulse = addSetting(new BooleanSetting("Pulsing", true));
    private final BooleanSetting hideParticles = addSetting(new BooleanSetting("HideParticles", true));
    private final BooleanSetting warning = addSetting(new BooleanSetting("Warning", true));
    private final BooleanSetting testMode = addSetting(new BooleanSetting("TestMode", false));
    private final BooleanSetting debugLog = addSetting(new BooleanSetting("DebugLog", true));

    public static class StunZone {
        public double minX, maxX;
        public double minZ, maxZ;
        public double groundY;
        public double centerX, centerZ;
        public long firstSeen;
        public long lastSeen;
        public int particleCount;
        public boolean confirmed;
        public List<Vec3d> particlePoints = new ArrayList<>();

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
            this.confirmed = false;
            this.particlePoints.add(new Vec3d(x, y, z));
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
            if (particlePoints.size() < 200) {
                particlePoints.add(new Vec3d(x, y, z));
            }

            if (!confirmed && particleCount >= 4 && ((maxX - minX) >= 1.5 || (maxZ - minZ) >= 1.5)) {
                this.confirmed = true;
            }
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
        super("StunVisuals", "HolyWorld Square Stun Trap / Anti-Pearl Zone continuous neon 3D barrier", Category.VISUAL);
        setEnabled(true);
    }

    public boolean isHideParticles() {
        return isEnabled() && hideParticles.isEnabled();
    }

    public boolean handleParticle(ParticleEffect parameters, double x, double y, double z) {
        if (!isEnabled() || mc.world == null || mc.player == null) return false;
        long now = System.currentTimeMillis();

        String pStr = parameters.asString();
        String pType = parameters.getType().toString();

        // Optional debug logging of incoming particles to nexus_debug.log
        if (debugLog.isEnabled() && (pStr.contains("dust") || pStr.contains("yellow") || pStr.contains("gold") || pType.contains("dust") || pType.contains("totem") || pType.contains("crit"))) {
            try (FileWriter fw = new FileWriter("nexus_debug.log", true); PrintWriter pw = new PrintWriter(fw)) {
                pw.println("[STUN_PARTICLE] type=" + pType + ", asString=" + pStr + ", pos=(" + String.format("%.2f, %.2f, %.2f", x, y, z) + ")");
            } catch (Exception ignored) {}
        }

        boolean isStunCandidate = false;

        if (parameters instanceof DustParticleEffect) {
            DustParticleEffect dust = (DustParticleEffect) parameters;
            float r = dust.getRed();
            float g = dust.getGreen();
            float b = dust.getBlue();
            // Accept any yellow, gold, orange, red, amber dust
            if (r > 0.35f && g > 0.20f && b < 0.65f) {
                isStunCandidate = true;
            }
        } else if (pStr.contains("dust") || pStr.contains("yellow") || pStr.contains("gold") ||
                   parameters.getType() == ParticleTypes.TOTEM_OF_UNDYING ||
                   parameters.getType() == ParticleTypes.ENCHANT ||
                   parameters.getType() == ParticleTypes.FALLING_DUST ||
                   parameters.getType() == ParticleTypes.CRIT ||
                   parameters.getType() == ParticleTypes.FLAME) {
            isStunCandidate = true;
        }

        if (!isStunCandidate) return false;

        Vec3d pPos = mc.player.getPos();
        if (Math.hypot(x - pPos.x, z - pPos.z) > 50.0) return false;

        synchronized (activeZones) {
            for (StunZone zone : activeZones) {
                double dist = Math.hypot(x - zone.centerX, z - zone.centerZ);
                if (dist <= 12.0 && Math.abs(y - zone.groundY) <= 3.5) {
                    zone.addParticle(x, y, z, now);
                    return true; // Cancel particle inside stun trap
                }
            }

            if (activeZones.size() < 8) {
                activeZones.add(new StunZone(x, y, z, now));
                return true; // Cancel initial particle
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
                testZone = new StunZone(pPos.x - 4.0, pPos.y, pPos.z - 4.0, now);
                testZone.maxX = pPos.x + 4.0;
                testZone.maxZ = pPos.z + 4.0;
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
                if (!z.confirmed && (now - z.firstSeen > 800)) {
                    it.remove();
                    continue;
                }
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
        float h = height.getFloatValue();
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
            double pad = 0.25;
            double x1 = (zone.minX - pad) - camPos.x;
            double x2 = (zone.maxX + pad) - camPos.x;
            double z1 = (zone.minZ - pad) - camPos.z;
            double z2 = (zone.maxZ + pad) - camPos.z;
            double y1 = (Math.floor(zone.groundY)) - camPos.y;
            double y2 = y1 + h;

            // 1. Semi-transparent 4 Vertical Square Walls
            if (curStyle.equals("SquareBox") || curStyle.equals("ForcefieldPrism")) {
                buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
                int wallAlpha = Math.min(255, (int) (a * 0.30f));

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
