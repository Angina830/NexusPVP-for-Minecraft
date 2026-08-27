package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SwordSlash extends Module {
    private final ModeSetting style = addSetting(new ModeSetting("Style", "AnimeArc", "AnimeArc", "FlameSlash", "Electric"));
    private final NumberSetting arcRadius = addSetting(new NumberSetting("ArcRadius", 1.8, 0.8, 3.5, 0.1));
    private final NumberSetting duration = addSetting(new NumberSetting("Duration", 1.0, 0.5, 2.5, 0.1));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 240, 255, 240)));

    private final List<SlashArc> activeArcs = new ArrayList<>();
    private float lastHandSwingProgress = 0f;

    public SwordSlash() {
        super("SwordSlash", "Glowing 3D anime slash arcs sweeping on attack", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        float swing = mc.player.getHandSwingProgress(1.0f);
        if (swing > 0.05f && lastHandSwingProgress <= 0.05f) {
            spawnSlashArc();
        }
        lastHandSwingProgress = swing;

        float fadeSpeed = 0.07f / duration.getFloatValue();
        Iterator<SlashArc> it = activeArcs.iterator();
        while (it.hasNext()) {
            SlashArc arc = it.next();
            arc.progress += fadeSpeed;
            arc.alpha = Math.max(0.0f, 1.0f - arc.progress);
            if (arc.progress >= 1.0f) {
                it.remove();
            }
        }
    }

    private void spawnSlashArc() {
        if (mc.player == null) return;
        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        Color c = rainbow.isEnabled() ? ColorUtils.rainbow(0) : color.getColor();
        activeArcs.add(new SlashArc(eyePos, yaw, pitch, c));
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null || activeArcs.isEmpty()) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float rMax = arcRadius.getFloatValue();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (SlashArc arc : activeArcs) {
            if (arc.alpha <= 0.01f) continue;

            matrices.push();
            matrices.translate(arc.origin.x - cam.x, arc.origin.y - cam.y, arc.origin.z - cam.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-arc.yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(arc.pitch));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-25f));
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            int alpha = (int) (arc.color.getAlpha() * arc.alpha);
            int r = arc.color.getRed();
            int g = arc.color.getGreen();
            int b = arc.color.getBlue();

            int segments = 24;
            double startAngle = -Math.PI * 0.45;
            double endAngle = Math.PI * 0.45;

            buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double t = (double) i / segments;
                double angle = startAngle + (endAngle - startAngle) * t;

                float arcWidth = (float) Math.sin(t * Math.PI) * 0.35f;
                float rInner = rMax - arcWidth;
                float rOuter = rMax + arcWidth;

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                buffer.vertex(matrix, cos * rInner, sin * rInner, 0f).color(r, g, b, 0).next();
                buffer.vertex(matrix, cos * rOuter, sin * rOuter, 0f).color(r, g, b, alpha).next();
            }
            tessellator.draw();

            matrices.pop();
        }

        RenderSystem.disableBlend();
    }

    private static class SlashArc {
        final Vec3d origin;
        final float yaw;
        final float pitch;
        final Color color;
        float progress = 0.0f;
        float alpha = 1.0f;

        SlashArc(Vec3d origin, float yaw, float pitch, Color color) {
            this.origin = origin;
            this.yaw = yaw;
            this.pitch = pitch;
            this.color = color;
        }
    }
}
