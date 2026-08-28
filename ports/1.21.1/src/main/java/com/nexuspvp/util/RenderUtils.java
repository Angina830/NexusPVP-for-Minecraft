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

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x1, y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
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

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a);
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
    }

    public static void drawCircle3D(MatrixStack matrices, double x, double y, double z, float radius, Color color, float width) {
        drawNeonCircle3D(matrices, x, y, z, radius, color);
    }

    public static void drawNeonCircle3D(MatrixStack matrices, double x, double y, double z, float radius, Color color) {
        matrices.push();
        matrices.translate(x, y, z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 32; i++) {
            double angle = 2 * Math.PI * i / 32;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            buffer.vertex(matrix, (radius - 0.08f) * cos, 0f, (radius - 0.08f) * sin).color(r, g, b, 0);
            buffer.vertex(matrix, radius * cos, 0f, radius * sin).color(r, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
        RenderSystem.disableBlend();
    }

    public static void drawBox3D(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, Color outlineColor, Color fillColor, float lineWidth) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        if (fillColor != null && fillColor.getAlpha() > 0) {
            float fr = fillColor.getRed() / 255.0f;
            float fg = fillColor.getGreen() / 255.0f;
            float fb = fillColor.getBlue() / 255.0f;
            float fa = fillColor.getAlpha() / 255.0f;

            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            // Down
            buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x2, (float) y1, (float) z1).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x2, (float) y1, (float) z2).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x1, (float) y1, (float) z2).color(fr, fg, fb, fa);
            // Up
            buffer.vertex(matrix, (float) x1, (float) y2, (float) z1).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x1, (float) y2, (float) z2).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(fr, fg, fb, fa);
            buffer.vertex(matrix, (float) x2, (float) y2, (float) z1).color(fr, fg, fb, fa);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.disableBlend();
    }

    public static void startScissor(int x, int y, int width, int height) {
        if (width < 0) width = 0;
        if (height < 0) height = 0;
        int scale = (int) mc.getWindow().getScaleFactor();
        int screenH = mc.getWindow().getHeight();
        RenderSystem.enableScissor(x * scale, screenH - (y + height) * scale, width * scale, height * scale);
    }

    public static void endScissor() {
        RenderSystem.disableScissor();
    }

    public static void setupBloom3D() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void cleanupBloom3D() {
        RenderSystem.disableBlend();
    }
}
