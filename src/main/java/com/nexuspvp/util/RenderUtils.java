package com.nexuspvp.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.Color;

public class RenderUtils {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void drawRect(MatrixStack matrices, double x, double y, double width, double height, int color) {
        if (Compat.getContext() != null) {
            Compat.getContext().fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
            return;
        }
        drawQuad(matrices, (float) x, (float) y, (float) (x + width), (float) (y + height), color);
    }

    public static void drawRoundedRect(MatrixStack matrices, double x, double y, double width, double height, double radius, int color) {
        drawRect(matrices, x, y, width, height, color);
    }

    public static void drawGradientRect(MatrixStack matrices, double x, double y, double width, double height, int startColor, int endColor) {
        if (Compat.getContext() != null) {
            Compat.getContext().fillGradient((int) x, (int) y, (int) (x + width), (int) (y + height), startColor, endColor);
            return;
        }
        drawRect(matrices, x, y, width, height, startColor);
    }

    public static void drawQuad(MatrixStack matrices, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float a = ((color >> 24) & 255) / 255.0f;
        float r = ((color >> 16) & 255) / 255.0f;
        float g = ((color >> 8) & 255) / 255.0f;
        float b = (color & 255) / 255.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x1, y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public static void drawLine3D(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, Color color, float width) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(width);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a);
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public static void drawNeonLine3D(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        drawLine3D(matrices, x1, y1, z1, x2, y2, z2, new Color(r, g, b, (int) (a * 0.18f)), 6.0f);
        drawLine3D(matrices, x1, y1, z1, x2, y2, z2, new Color(r, g, b, (int) (a * 0.45f)), 3.5f);
        int coreR = Math.min(255, (int) (r * 0.7f + 255 * 0.3f));
        int coreG = Math.min(255, (int) (g * 0.7f + 255 * 0.3f));
        int coreB = Math.min(255, (int) (b * 0.7f + 255 * 0.3f));
        drawLine3D(matrices, x1, y1, z1, x2, y2, z2, new Color(coreR, coreG, coreB, a), 1.5f);
    }

    public static void drawBox3D(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, Color color, float width) {
        drawLine3D(matrices, x1, y1, z1, x2, y1, z1, color, width);
        drawLine3D(matrices, x2, y1, z1, x2, y1, z2, color, width);
        drawLine3D(matrices, x2, y1, z2, x1, y1, z2, color, width);
        drawLine3D(matrices, x1, y1, z2, x1, y1, z1, color, width);

        drawLine3D(matrices, x1, y2, z1, x2, y2, z1, color, width);
        drawLine3D(matrices, x2, y2, z1, x2, y2, z2, color, width);
        drawLine3D(matrices, x2, y2, z2, x1, y2, z2, color, width);
        drawLine3D(matrices, x1, y2, z2, x1, y2, z1, color, width);

        drawLine3D(matrices, x1, y1, z1, x1, y2, z1, color, width);
        drawLine3D(matrices, x2, y1, z1, x2, y2, z1, color, width);
        drawLine3D(matrices, x2, y1, z2, x2, y2, z2, color, width);
        drawLine3D(matrices, x1, y1, z2, x1, y2, z2, color, width);
    }

    public static void drawNeonBox3D(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, Color color, boolean fill, int fillAlpha) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        if (fill && fillAlpha > 0) {
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float fr = r / 255.0f;
            float fg = g / 255.0f;
            float fb = b / 255.0f;
            float fa = Math.min(255, fillAlpha) / 255.0f;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x2, (float) y1, (float) z1).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x2, (float) y1, (float) z2).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x1, (float) y1, (float) z2).color(fr, fg, fb, fa);

            buffer.vertex(matrix, (float) x1, (float) y2, (float) z1).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x1, (float) y2, (float) z2).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x2, (float) y2, (float) z1).color(fr, fg, fb, fa);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        drawBox3D(matrices, x1, y1, z1, x2, y2, z2, new Color(r, g, b, (int) (a * 0.22f)), 5.5f);
        drawBox3D(matrices, x1, y1, z1, x2, y2, z2, new Color(r, g, b, (int) (a * 0.55f)), 3.0f);
        int coreR = Math.min(255, (int) (r * 0.75f + 255 * 0.25f));
        int coreG = Math.min(255, (int) (g * 0.75f + 255 * 0.25f));
        int coreB = Math.min(255, (int) (b * 0.75f + 255 * 0.25f));
        drawBox3D(matrices, x1, y1, z1, x2, y2, z2, new Color(coreR, coreG, coreB, a), 1.5f);
    }

    public static void drawCircle3D(MatrixStack matrices, double x, double y, double z, float radius, Color color, float lineWidth) {
        int segments = 32;
        for (int i = 0; i < segments; i++) {
            double angle1 = (i * 2.0 * Math.PI) / segments;
            double angle2 = ((i + 1) * 2.0 * Math.PI) / segments;

            double x1 = x + Math.cos(angle1) * radius;
            double z1 = z + Math.sin(angle1) * radius;
            double x2 = x + Math.cos(angle2) * radius;
            double z2 = z + Math.sin(angle2) * radius;

            drawLine3D(matrices, x1, y, z1, x2, y, z2, color, lineWidth);
        }
    }

    public static void drawNeonCircle3D(MatrixStack matrices, double x, double y, double z, float radius, Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        drawCircle3D(matrices, x, y, z, radius, new Color(r, g, b, (int) (a * 0.20f)), 5.5f);
        drawCircle3D(matrices, x, y, z, radius, new Color(r, g, b, (int) (a * 0.50f)), 3.0f);
        int coreR = Math.min(255, (int) (r * 0.75f + 255 * 0.25f));
        int coreG = Math.min(255, (int) (g * 0.75f + 255 * 0.25f));
        int coreB = Math.min(255, (int) (b * 0.75f + 255 * 0.25f));
        drawCircle3D(matrices, x, y, z, radius, new Color(coreR, coreG, coreB, a), 1.5f);
    }

        public static void startScissor(int x, int y, int width, int height) {
        if (Compat.getContext() != null) {
            Compat.getContext().enableScissor(x, y, x + width, y + height);
        } else {
            if (mc.getWindow() == null) return;
            double scale = mc.getWindow().getScaleFactor();
            int screenH = mc.getWindow().getScaledHeight();
            int scissorY = (int) ((screenH - (y + height)) * scale);
            RenderSystem.enableScissor((int) (x * scale), Math.max(0, scissorY), (int) (width * scale), (int) (height * scale));
        }
    }

    public static void endScissor() {
        if (Compat.getContext() != null) {
            Compat.getContext().disableScissor();
        } else {
            RenderSystem.disableScissor();
        }
    }
    public static net.minecraft.util.math.Vec3d getInterpolatedPos(net.minecraft.entity.Entity entity, float tickDelta) {
        double x = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
        double y = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;
        return new net.minecraft.util.math.Vec3d(x, y, z);
    }
}