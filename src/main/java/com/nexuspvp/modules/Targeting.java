package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.nexuspvp.util.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class Targeting extends Module {
    private final ModeSetting style = addSetting(new ModeSetting("Style", "PulsingDoubleRing", "PulsingDoubleRing", "RotatingDiamond", "HologramBrackets", "NeonBeacon"));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 5.0, 2.0, 10.0, 0.5));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 5.0, 0.5));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 240, 255, 230)));

    private float animTicks = 0f;

    public Targeting() {
        super("Targeting", "Futuristic 3D holographic targeting reticle & glowing aura on target", Category.VISUAL);
    }

    @Override
    public void onTick() {
        animTicks += 0.05f;
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (!(entity instanceof LivingEntity) || entity == mc.player) return;
        if (mc.player.distanceTo(entity) > range.getFloatValue()) return;

        LivingEntity target = (LivingEntity) entity;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Vec3d targetPos = target.getPos();
        String curStyle = style.getValue();
        Color c = rainbow.isEnabled() ? ColorUtils.rainbow(0) : color.getColor();
        float lw = lineWidth.getFloatValue();

        RenderUtils.setupBloom3D();
        RenderSystem.pushMatrix();
        RenderSystem.translated(targetPos.x - cam.x, targetPos.y - cam.y, targetPos.z - cam.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        int a = c.getAlpha();

        if (curStyle.equals("PulsingDoubleRing")) {
            float pulse = (float) Math.sin(animTicks * 3.0f) * 0.08f;
            float r1 = 0.65f + pulse;
            float r2 = 0.85f - pulse;
            float rot1 = animTicks * 60f;
            float rot2 = -animTicks * 45f;

            RenderSystem.pushMatrix();
            RenderSystem.translated(0, 0.05, 0);
            RenderSystem.rotatef(rot1, 0, 1, 0);
            GL11.glLineWidth(lw);
            buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i < 32; i++) {
                double angle = 2 * Math.PI * i / 32;
                buffer.vertex((float) (r1 * Math.cos(angle)), 0f, (float) (r1 * Math.sin(angle))).color(r, g, b, a).next();
            }
            tessellator.draw();
            RenderSystem.popMatrix();

            RenderSystem.pushMatrix();
            RenderSystem.translated(0, 0.07, 0);
            RenderSystem.rotatef(rot2, 0, 1, 0);
            buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i < 24; i++) {
                double angle = 2 * Math.PI * i / 24;
                buffer.vertex((float) (r2 * Math.cos(angle)), 0f, (float) (r2 * Math.sin(angle))).color(r, g, b, a / 2).next();
            }
            tessellator.draw();
            RenderSystem.popMatrix();
        } else if (curStyle.equals("RotatingDiamond")) {
            float floatY = target.getHeight() * 0.5f + (float) Math.sin(animTicks * 2.5f) * 0.15f;
            RenderSystem.pushMatrix();
            RenderSystem.translated(0, floatY, 0);
            RenderSystem.rotatef(animTicks * 50f, 0, 1, 0);
            RenderSystem.rotatef(45f, 1, 0, 1);

            float dSize = 0.4f;
            GL11.glLineWidth(lw);
            buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
            buffer.vertex(0, dSize, 0).color(r, g, b, a).next();
            buffer.vertex(dSize, 0, 0).color(r, g, b, a).next();
            buffer.vertex(0, -dSize, 0).color(r, g, b, a).next();
            buffer.vertex(-dSize, 0, 0).color(r, g, b, a).next();
            tessellator.draw();
            RenderSystem.popMatrix();
        } else if (curStyle.equals("NeonBeacon")) {
            float h = target.getHeight() + 0.5f;
            buffer.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 16; i++) {
                double angle = 2 * Math.PI * i / 16;
                float cos = (float) Math.cos(angle) * 0.4f;
                float sin = (float) Math.sin(angle) * 0.4f;
                buffer.vertex(cos, 0f, sin).color(r, g, b, a / 3).next();
                buffer.vertex(cos, h, sin).color(r, g, b, 0).next();
            }
            tessellator.draw();
        } else {
            float h = target.getHeight();
            float w = target.getWidth() * 0.7f;
            GL11.glLineWidth(lw);
            RenderSystem.rotatef(-mc.gameRenderer.getCamera().getYaw(), 0f, 1f, 0f);

            buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
            buffer.vertex(-w, h, 0).color(r, g, b, a).next();
            buffer.vertex(-w + 0.3f, h, 0).color(r, g, b, a).next();
            buffer.vertex(-w, h, 0).color(r, g, b, a).next();
            buffer.vertex(-w, h - 0.3f, 0).color(r, g, b, a).next();

            buffer.vertex(w, h, 0).color(r, g, b, a).next();
            buffer.vertex(w - 0.3f, h, 0).color(r, g, b, a).next();
            buffer.vertex(w, h, 0).color(r, g, b, a).next();
            buffer.vertex(w, h - 0.3f, 0).color(r, g, b, a).next();

            buffer.vertex(-w, 0, 0).color(r, g, b, a).next();
            buffer.vertex(-w + 0.3f, 0, 0).color(r, g, b, a).next();
            buffer.vertex(-w, 0, 0).color(r, g, b, a).next();
            buffer.vertex(-w, 0.3f, 0).color(r, g, b, a).next();

            buffer.vertex(w, 0, 0).color(r, g, b, a).next();
            buffer.vertex(w - 0.3f, 0, 0).color(r, g, b, a).next();
            buffer.vertex(w, 0, 0).color(r, g, b, a).next();
            buffer.vertex(w, 0.3f, 0).color(r, g, b, a).next();
            tessellator.draw();
        }

        RenderSystem.popMatrix();
        RenderUtils.cleanupBloom3D();
    }
}
