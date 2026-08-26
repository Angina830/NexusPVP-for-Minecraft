package com.nexuspvp.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class RenderUtils {

    public static void drawRect(MatrixStack matrices, double x, double y, double width, double height, int color) {
        if (Compat.getContext() != null) {
            Compat.getContext().fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
            return;
        }

        int x2 = (int) (x + width);
        int y2 = (int) (y + height);
        int x1 = (int) x;
        int y1 = (int) y;
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        buffer.vertex(matrix, (float) x1, (float) y2, 0.0F).color(r, g, b, a);
        buffer.vertex(matrix, (float) x2, (float) y2, 0.0F).color(r, g, b, a);
        buffer.vertex(matrix, (float) x2, (float) y1, 0.0F).color(r, g, b, a);
        buffer.vertex(matrix, (float) x1, (float) y1, 0.0F).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
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

    public static void drawFilledArc2D(MatrixStack matrices, double cx, double cy, double radius, double startAngle, double endAngle, int color) {
        drawRect(matrices, cx - radius, cy - radius, radius * 2, radius * 2, color);
    }

    public static void startScissor(int x, int y, int width, int height) {
        if (Compat.getContext() != null) {
            Compat.getContext().enableScissor(x, y, x + width, y + height);
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        double scale = mc.getWindow().getScaleFactor();
        int sx = (int) (x * scale);
        int sy = (int) ((mc.getWindow().getScaledHeight() - (y + height)) * scale);
        int sw = (int) (width * scale);
        int sh = (int) (height * scale);

        if (sw > 0 && sh > 0) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(Math.max(0, sx), Math.max(0, sy), sw, sh);
        }
    }

    public static void endScissor() {
        if (Compat.getContext() != null) {
            Compat.getContext().disableScissor();
            return;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static Vec3d getInterpolatedPos(Entity entity, float tickDelta) {
        if (entity == null) return Vec3d.ZERO;
        return entity.getLerpedPos(tickDelta);
    }

    public static void drawBlockOutline3D(BlockPos pos, Color color, float lineWidth, boolean fill, int fillAlpha) {}
    public static void drawCircle3D(MatrixStack matrices, double x, double y, double z, float radius, Color color, float lineWidth) {}
    public static void drawBloomCircle3D(Vec3d pos, float radius, int points, Color color, float lineWidth) {}
    public static void drawTorus3D(MatrixStack matrices, double x, double y, double z, float majorRadius, float minorRadius, Color color) {}
    public static void drawCone3D(MatrixStack matrices, double x, double y, double z, float radius, float height, Color color) {}
    public static void drawDisc3D(MatrixStack matrices, double x, double y, double z, float radius, Color color) {}
    public static void drawLine3D(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, Color color, float width) {}
    public static void drawBox3D(MatrixStack matrices, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color, float lineWidth) {}
}
