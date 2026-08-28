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
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.LinkedList;

public class Trails extends Module {
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Ribbon", "Ribbon", "GradientLine"));
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

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float baseWidth = width.getFloatValue();
        boolean doTaper = taper.isEnabled();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        int size = points.size();

        for (int i = 0; i < size; i++) {
            TrailPoint pt = points.get(i);
            float progress = (float) i / (float) size;
            float alphaFactor = (float) Math.pow(1.0f - progress, 1.3);
            int alpha = (int) (pt.color.getAlpha() * alphaFactor);
            if (alpha <= 0) continue;

            float curW = doTaper ? baseWidth * (1.0f - progress) : baseWidth;
            int r = pt.color.getRed();
            int g = pt.color.getGreen();
            int b = pt.color.getBlue();

            buffer.vertex(matrix, (float) pt.pos.x, (float) (pt.pos.y + curW), (float) pt.pos.z).color(r, g, b, alpha);
            buffer.vertex(matrix, (float) pt.pos.x, (float) (pt.pos.y - (curW * 0.2f)), (float) pt.pos.z).color(r, g, b, 0);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
        RenderSystem.disableBlend();
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
