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
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.LinkedList;

public class Trails extends Module {
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Ribbon", "Ribbon", "Sparks", "GradientLine"));
    private final NumberSetting length = addSetting(new NumberSetting("Length", 24, 6, 60, 1));
    private final NumberSetting width = addSetting(new NumberSetting("Width", 0.35, 0.05, 1.5, 0.05));
    private final BooleanSetting taper = addSetting(new BooleanSetting("Taper", true));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(180, 0, 255, 230)));

    private final LinkedList<TrailPoint> points = new LinkedList<>();

    public Trails() {
        super("Trails", "Smooth glowing movement ribbon behind player", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        Vec3d p = mc.player.getPos();
        Color c = rainbow.isEnabled() ? ColorUtils.rainbow(0) : color.getColor();
        points.addFirst(new TrailPoint(new Vec3d(p.x, p.y + 0.1, p.z), c));

        int maxPoints = length.getIntValue();
        while (points.size() > maxPoints) {
            points.removeLast();
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null || points.size() < 2) return;

        RenderUtils.setupBloom3D();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        String curStyle = style.getValue();
        float baseWidth = width.getFloatValue();
        boolean doTaper = taper.isEnabled();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if (curStyle.equals("Ribbon")) {
            buffer.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            int size = points.size();

            for (int i = 0; i < size; i++) {
                TrailPoint pt = points.get(i);
                float progress = (float) i / (float) size;
                float alphaFactor = (float) Math.pow(1.0f - progress, 1.3);
                int alpha = (int) (pt.color.getAlpha() * alphaFactor);
                if (alpha <= 0) continue;

                float curW = doTaper ? baseWidth * (1.0f - progress) : baseWidth;
                double rx = pt.pos.x - cam.x;
                double ry = pt.pos.y - cam.y;
                double rz = pt.pos.z - cam.z;

                int r = pt.color.getRed();
                int g = pt.color.getGreen();
                int b = pt.color.getBlue();

                buffer.vertex((float) rx, (float) (ry + curW), (float) rz).color(r, g, b, alpha).next();
                buffer.vertex((float) rx, (float) (ry - (curW * 0.2f)), (float) rz).color(r, g, b, 0).next();
            }
            tessellator.draw();
        } else if (curStyle.equals("GradientLine")) {
            GL11.glLineWidth(3.5f);
            buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
            int size = points.size();

            for (int i = 0; i < size; i++) {
                TrailPoint pt = points.get(i);
                float progress = (float) i / (float) size;
                float alphaFactor = 1.0f - progress;
                int alpha = (int) (pt.color.getAlpha() * alphaFactor);

                double rx = pt.pos.x - cam.x;
                double ry = pt.pos.y - cam.y;
                double rz = pt.pos.z - cam.z;

                buffer.vertex((float) rx, (float) ry, (float) rz)
                        .color(pt.color.getRed(), pt.color.getGreen(), pt.color.getBlue(), alpha).next();
            }
            tessellator.draw();
        } else {
            int size = points.size();
            for (int i = 0; i < size; i += 2) {
                TrailPoint pt = points.get(i);
                float progress = (float) i / (float) size;
                float alphaFactor = 1.0f - progress;
                int alpha = (int) (pt.color.getAlpha() * alphaFactor);
                if (alpha <= 0) continue;

                RenderSystem.pushMatrix();
                RenderSystem.translated(pt.pos.x - cam.x, pt.pos.y - cam.y + (progress * 0.2), pt.pos.z - cam.z);

                float pSize = baseWidth * (1.0f - (progress * 0.5f)) * 0.4f;
                buffer.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
                buffer.vertex(0f, 0f, 0f).color(pt.color.getRed(), pt.color.getGreen(), pt.color.getBlue(), alpha).next();
                for (int s = 0; s <= 12; s++) {
                    double angle = 2 * Math.PI * s / 12;
                    buffer.vertex((float) (pSize * Math.cos(angle)), (float) (pSize * Math.sin(angle)), 0f)
                            .color(pt.color.getRed(), pt.color.getGreen(), pt.color.getBlue(), 0).next();
                }
                tessellator.draw();

                RenderSystem.popMatrix();
            }
        }

        RenderUtils.cleanupBloom3D();
    }

    private static class TrailPoint {
        final Vec3d pos;
        final Color color;

        TrailPoint(Vec3d pos, Color color) {
            this.pos = pos;
            this.color = color;
        }
    }
}
