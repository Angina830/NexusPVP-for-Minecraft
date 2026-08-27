package com.nexuspvp.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import java.awt.Color;

public class RenderUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static final java.util.Deque<int[]> scissorStack = new java.util.ArrayDeque<>();

    public static void pushScissor(int x, int y, int width, int height) {
        MinecraftClient mc = MinecraftClient.getInstance();
        double scale = mc.getWindow().getScaleFactor();
        int screenH = mc.getWindow().getScaledHeight();
        int screenW = mc.getWindow().getScaledWidth();

        int curX = Math.max(0, x);
        int curY = Math.max(0, y);
        int curW = Math.min(screenW - curX, Math.max(0, width));
        int curH = Math.min(screenH - curY, Math.max(0, height));

        if (!scissorStack.isEmpty()) {
            int[] parent = scissorStack.peek();
            int px = parent[0];
            int py = parent[1];
            int pw = parent[2];
            int ph = parent[3];

            int newX = Math.max(curX, px);
            int newY = Math.max(curY, py);
            int newW = Math.max(0, Math.min(curX + curW, px + pw) - newX);
            int newH = Math.max(0, Math.min(curY + curH, py + ph) - newY);

            curX = newX;
            curY = newY;
            curW = newW;
            curH = newH;
        }

        scissorStack.push(new int[]{curX, curY, curW, curH});
        applyScissor(curX, curY, curW, curH, scale, screenH);
    }

    public static void popScissor() {
        if (!scissorStack.isEmpty()) {
            scissorStack.pop();
        }

        if (scissorStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            int[] top = scissorStack.peek();
            MinecraftClient mc = MinecraftClient.getInstance();
            applyScissor(top[0], top[1], top[2], top[3], mc.getWindow().getScaleFactor(), mc.getWindow().getScaledHeight());
        }
    }

    private static void applyScissor(int x, int y, int width, int height, double scale, int screenH) {
        int scissorX = (int) (x * scale);
        int scissorY = (int) ((screenH - (y + height)) * scale);
        int scissorW = (int) (width * scale);
        int scissorH = (int) (height * scale);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(Math.max(0, scissorX), Math.max(0, scissorY), Math.max(0, scissorW), Math.max(0, scissorH));
    }

    public static void startScissor(int x, int y, int width, int height) {
        pushScissor(x, y, width, height);
    }

    public static void endScissor() {
        popScissor();
    }

    public static void setup3D() {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    public static void cleanup3D() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public static void drawBox3D(MatrixStack matrices, net.minecraft.util.math.Box box, Color color) {
        Matrix4f matrix = matrices.peek().getModel();
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).next();
        tessellator.draw();
    }

    public static void drawBoxOutline3D(MatrixStack matrices, net.minecraft.util.math.Box box, Color color, float lineWidth) {
        Matrix4f matrix = matrices.peek().getModel();
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;

        GL11.glLineWidth(lineWidth);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).next();

        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).next();

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).next();

        tessellator.draw();
    }

    public static void drawLine3D(Vec3d start, Vec3d end, Color color, float lineWidth) {
        setup3D();
        GL11.glLineWidth(lineWidth);
        
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.pushMatrix();
        RenderSystem.translated(-cam.x, -cam.y, -cam.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex((float) start.x, (float) start.y, (float) start.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        buffer.vertex((float) end.x, (float) end.y, (float) end.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        tessellator.draw();
        
        RenderSystem.popMatrix();
        cleanup3D();
    }

    public static void setupBloom3D() {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    public static void cleanupBloom3D() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawBloomLine3D(Vec3d start, Vec3d end, Color color, float coreWidth) {
        setupBloom3D();
        
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.pushMatrix();
        RenderSystem.translated(-cam.x, -cam.y, -cam.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Layer 1: Wide soft outer aura
        GL11.glLineWidth(coreWidth * 4.0f);
        int a1 = Math.max(12, color.getAlpha() / 5);
        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex((float) start.x, (float) start.y, (float) start.z).color(color.getRed(), color.getGreen(), color.getBlue(), a1).next();
        buffer.vertex((float) end.x, (float) end.y, (float) end.z).color(color.getRed(), color.getGreen(), color.getBlue(), a1).next();
        tessellator.draw();

        // Layer 2: Medium glowing halo
        GL11.glLineWidth(coreWidth * 2.2f);
        int a2 = Math.max(35, color.getAlpha() / 3);
        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex((float) start.x, (float) start.y, (float) start.z).color(color.getRed(), color.getGreen(), color.getBlue(), a2).next();
        buffer.vertex((float) end.x, (float) end.y, (float) end.z).color(color.getRed(), color.getGreen(), color.getBlue(), a2).next();
        tessellator.draw();

        // Layer 3: Sharp core bright line
        GL11.glLineWidth(coreWidth);
        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex((float) start.x, (float) start.y, (float) start.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        buffer.vertex((float) end.x, (float) end.y, (float) end.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        tessellator.draw();

        RenderSystem.popMatrix();
        cleanupBloom3D();
    }

    public static void drawBloomCircle3D(Vec3d pos, double radius, int segments, Color color, float coreWidth) {
        setupBloom3D();
        
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.pushMatrix();
        RenderSystem.translated(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Layer 1: Wide soft outer glow
        GL11.glLineWidth(coreWidth * 4.0f);
        int a1 = Math.max(12, color.getAlpha() / 5);
        buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            buffer.vertex(x, 0f, z).color(color.getRed(), color.getGreen(), color.getBlue(), a1).next();
        }
        tessellator.draw();

        // Layer 2: Medium glowing halo
        GL11.glLineWidth(coreWidth * 2.2f);
        int a2 = Math.max(35, color.getAlpha() / 3);
        buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            buffer.vertex(x, 0f, z).color(color.getRed(), color.getGreen(), color.getBlue(), a2).next();
        }
        tessellator.draw();

        // Layer 3: Sharp core bright ring
        GL11.glLineWidth(coreWidth);
        buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            buffer.vertex(x, 0f, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        }
        tessellator.draw();

        RenderSystem.popMatrix();
        cleanupBloom3D();
    }

    public static void drawCircle3D(Vec3d pos, double radius, int segments, Color color, float lineWidth) {
        setup3D();
        GL11.glLineWidth(lineWidth);
        
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.pushMatrix();
        RenderSystem.translated(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            buffer.vertex(x, 0f, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        }
        tessellator.draw();
        
        RenderSystem.popMatrix();
        cleanup3D();
    }

    public static void drawFilledCircle3D(Vec3d pos, double radius, int segments, Color color) {
        setup3D();
        
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.pushMatrix();
        RenderSystem.translated(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(0f, 0f, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            buffer.vertex(x, 0f, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        }
        tessellator.draw();
        
        RenderSystem.popMatrix();
        cleanup3D();
    }

    public static void drawCone3D(Vec3d pos, double radius, double height, int segments, Color color, float rotation) {
        setup3D();
        
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.pushMatrix();
        RenderSystem.translated(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        if (rotation != 0) {
            RenderSystem.rotatef(rotation, 0, 1, 0);
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(0f, (float) height, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        Color baseColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, color.getAlpha() - 80));
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            buffer.vertex(x, 0f, z).color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), baseColor.getAlpha()).next();
        }
        tessellator.draw();
        
        RenderSystem.popMatrix();
        cleanup3D();
    }

    public static void drawTorus3D(Vec3d pos, double majorRadius, double minorRadius, int majorSegments, int minorSegments, Color color) {
        setup3D();
        
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.pushMatrix();
        RenderSystem.translated(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (int i = 0; i < majorSegments; i++) {
            double theta1 = 2 * Math.PI * i / majorSegments;
            double theta2 = 2 * Math.PI * (i + 1) / majorSegments;

            buffer.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int j = 0; j <= minorSegments; j++) {
                double phi = 2 * Math.PI * j / minorSegments;
                for (int k = 0; k < 2; k++) {
                    double theta = k == 0 ? theta1 : theta2;
                    double x = (majorRadius + minorRadius * Math.cos(phi)) * Math.cos(theta);
                    double y = minorRadius * Math.sin(phi);
                    double z = (majorRadius + minorRadius * Math.cos(phi)) * Math.sin(theta);
                    buffer.vertex((float) x, (float) y, (float) z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                }
            }
            tessellator.draw();
        }
        
        RenderSystem.popMatrix();
        cleanup3D();
    }

    public static void drawDisc3D(Vec3d pos, double innerRadius, double outerRadius, int segments, Color innerColor, Color outerColor, float rotation) {
        setup3D();
        
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.pushMatrix();
        RenderSystem.translated(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        if (rotation != 0) {
            RenderSystem.rotatef(rotation, 0, 1, 0);
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            float xInner = (float) (innerRadius * cos);
            float zInner = (float) (innerRadius * sin);
            buffer.vertex(xInner, 0f, zInner).color(innerColor.getRed(), innerColor.getGreen(), innerColor.getBlue(), innerColor.getAlpha()).next();
            float xOuter = (float) (outerRadius * cos);
            float zOuter = (float) (outerRadius * sin);
            buffer.vertex(xOuter, 0f, zOuter).color(outerColor.getRed(), outerColor.getGreen(), outerColor.getBlue(), outerColor.getAlpha()).next();
        }
        tessellator.draw();
        
        RenderSystem.popMatrix();
        cleanup3D();
    }

    // 2D methods
    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, int color) {
        Matrix4f matrix = matrices.peek().getModel();
        float a = (color >> 24 & 0xFF) / 255.0f;
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y + height, 0).color(r, g, b, a).next();
        buffer.vertex(matrix, x + width, y + height, 0).color(r, g, b, a).next();
        buffer.vertex(matrix, x + width, y, 0).color(r, g, b, a).next();
        buffer.vertex(matrix, x, y, 0).color(r, g, b, a).next();
        tessellator.draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRectOutline(MatrixStack matrices, float x, float y, float width, float height, int color, float lineWidth) {
        Matrix4f matrix = matrices.peek().getModel();
        float a = (color >> 24 & 0xFF) / 255.0f;
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        GL11.glLineWidth(lineWidth);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y, 0).color(r, g, b, a).next();
        buffer.vertex(matrix, x + width, y, 0).color(r, g, b, a).next();
        buffer.vertex(matrix, x + width, y + height, 0).color(r, g, b, a).next();
        buffer.vertex(matrix, x, y + height, 0).color(r, g, b, a).next();
        tessellator.draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawGradientRect(MatrixStack matrices, float x, float y, float width, float height, int colorTop, int colorBottom) {
        Matrix4f matrix = matrices.peek().getModel();
        float a1 = (colorTop >> 24 & 0xFF) / 255.0f;
        float r1 = (colorTop >> 16 & 0xFF) / 255.0f;
        float g1 = (colorTop >> 8 & 0xFF) / 255.0f;
        float b1 = (colorTop & 0xFF) / 255.0f;
        float a2 = (colorBottom >> 24 & 0xFF) / 255.0f;
        float r2 = (colorBottom >> 16 & 0xFF) / 255.0f;
        float g2 = (colorBottom >> 8 & 0xFF) / 255.0f;
        float b2 = (colorBottom & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y, 0).color(r1, g1, b1, a1).next();
        buffer.vertex(matrix, x, y + height, 0).color(r2, g2, b2, a2).next();
        buffer.vertex(matrix, x + width, y + height, 0).color(r2, g2, b2, a2).next();
        buffer.vertex(matrix, x + width, y, 0).color(r1, g1, b1, a1).next();
        tessellator.draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
        drawRect(matrices, x + radius, y, width - 2 * radius, height, color);
        drawRect(matrices, x, y + radius, radius, height - 2 * radius, color);
        drawRect(matrices, x + width - radius, y + radius, radius, height - 2 * radius, color);
        drawFilledArc2D(matrices, x + radius, y + radius, radius, 180, 270, color);
        drawFilledArc2D(matrices, x + width - radius, y + radius, radius, 270, 360, color);
        drawFilledArc2D(matrices, x + width - radius, y + height - radius, radius, 0, 90, color);
        drawFilledArc2D(matrices, x + radius, y + height - radius, radius, 90, 180, color);
    }

    public static void drawFilledArc2D(MatrixStack matrices, float cx, float cy, float radius, float startAngle, float endAngle, int color) {
        Matrix4f matrix = matrices.peek().getModel();
        float a = (color >> 24 & 0xFF) / 255.0f;
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        int segments = 12;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cx, cy, 0).color(r, g, b, a).next();
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(startAngle + (endAngle - startAngle) * i / segments);
            buffer.vertex(matrix, (float) (cx + Math.cos(angle) * radius), (float) (cy + Math.sin(angle) * radius), 0)
                    .color(r, g, b, a).next();
        }
        tessellator.draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    
    public static void drawBlockOutline3D(net.minecraft.util.math.BlockPos pos, Color color, float lineWidth, boolean fill, int fillAlpha) {
        setup3D();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.pushMatrix();
        RenderSystem.translated(-cam.x, -cam.y, -cam.z);

        double minX = pos.getX();
        double minY = pos.getY();
        double minZ = pos.getZ();
        double maxX = pos.getX() + 1.0;
        double maxY = pos.getY() + 1.0;
        double maxZ = pos.getZ() + 1.0;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if (fill && fillAlpha > 0) {
            float r = color.getRed() / 255.0f;
            float g = color.getGreen() / 255.0f;
            float b = color.getBlue() / 255.0f;
            float a = fillAlpha / 255.0f;

            buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
            // Down
            buffer.vertex(minX, minY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, minY, maxZ).color(r, g, b, a).next();
            // Up
            buffer.vertex(minX, maxY, minZ).color(r, g, b, a).next();
            buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).next();
            // North
            buffer.vertex(minX, minY, minZ).color(r, g, b, a).next();
            buffer.vertex(minX, maxY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY, minZ).color(r, g, b, a).next();
            // South
            buffer.vertex(minX, minY, maxZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).next();
            // West
            buffer.vertex(minX, minY, minZ).color(r, g, b, a).next();
            buffer.vertex(minX, minY, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).next();
            buffer.vertex(minX, maxY, minZ).color(r, g, b, a).next();
            // East
            buffer.vertex(maxX, minY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).next();
            buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).next();
            buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).next();
            tessellator.draw();
        }

        // Glowing Neon outline
        GL11.glLineWidth(lineWidth);
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;

        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        // Bottom
        buffer.vertex(minX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(maxX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(maxX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(minX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(minX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(minX, minY, minZ).color(r, g, b, a).next();
        // Top
        buffer.vertex(minX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(minX, maxY, minZ).color(r, g, b, a).next();
        // Pillars
        buffer.vertex(minX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(minX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(maxX, minY, minZ).color(r, g, b, a).next();
        buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).next();
        buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).next();
        buffer.vertex(minX, minY, maxZ).color(r, g, b, a).next();
        buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).next();
        tessellator.draw();

        RenderSystem.popMatrix();
        cleanup3D();
    }

public static Vec3d getInterpolatedPos(net.minecraft.entity.Entity entity, float tickDelta) {
        double x = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
        double y = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;
        return new Vec3d(x, y, z);
    }
}
