package com.nexuspvp.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class Compat {

    private static DrawContext activeContext;

    public static void setContext(DrawContext context) {
        activeContext = context;
    }

    public static DrawContext getContext() {
        return activeContext;
    }

    public static void setScreen(MinecraftClient client, Screen screen) {
        if (client != null) {
            client.setScreen(screen);
        }
    }

    public static void setWidgetPos(ClickableWidget widget, int x, int y) {
        if (widget != null) {
            widget.setX(x);
            widget.setY(y);
        }
    }

    public static void sendChat(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            if (msg.startsWith("/")) {
                mc.player.networkHandler.sendChatCommand(msg.substring(1));
            } else {
                mc.player.networkHandler.sendChatMessage(msg);
            }
        }
    }

    public static void drawText(MatrixStack matrices, String text, double x, double y, int color) {
        drawWithShadow(null, matrices, text, (int) x, (int) y, color);
    }

    public static void drawWithShadow(TextRenderer tr, MatrixStack matrices, String text, int x, int y, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (tr == null) tr = mc.textRenderer;
        if (tr == null || text == null) return;

        if (activeContext != null) {
            activeContext.drawTextWithShadow(tr, text, x, y, color);
        } else {
            tr.draw(text, (float) x, (float) y, color, true, matrices.peek().getPositionMatrix(),
                    mc.getBufferBuilders().getEntityVertexConsumers(),
                    TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        }
    }

    public static void drawWithShadow(TextRenderer tr, MatrixStack matrices, Text text, int x, int y, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (tr == null) tr = mc.textRenderer;
        if (tr == null || text == null) return;

        if (activeContext != null) {
            activeContext.drawTextWithShadow(tr, text, x, y, color);
        } else {
            tr.draw(text, (float) x, (float) y, color, true, matrices.peek().getPositionMatrix(),
                    mc.getBufferBuilders().getEntityVertexConsumers(),
                    TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        }
    }

    public static void drawItem(MatrixStack matrices, ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty()) return;
        if (activeContext != null) {
            activeContext.drawItem(stack, x, y);
        }
    }

    public static void drawSkinHead(MatrixStack matrices, Identifier skin, int x, int y, int size) {
        if (skin == null) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (activeContext != null) {
            // DrawContext: drawTexture(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight)
            // 1. Base head layer: U=8, V=8, Region=8x8, Texture=64x64
            activeContext.drawTexture(skin, x, y, size, size, 8.0f, 8.0f, 8, 8, 64, 64);
            // 2. Outer hat/helmet layer: U=40, V=8, Region=8x8, Texture=64x64
            activeContext.drawTexture(skin, x, y, size, size, 40.0f, 8.0f, 8, 8, 64, 64);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, skin);
            Matrix4f mat = matrices.peek().getPositionMatrix();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

            // 1. Base head layer
            float u1 = 8.0f / 64.0f, v1 = 8.0f / 64.0f;
            float u2 = 16.0f / 64.0f, v2 = 16.0f / 64.0f;
            buffer.vertex(mat, x, y + size, 0).texture(u1, v2).next();
            buffer.vertex(mat, x + size, y + size, 0).texture(u2, v2).next();
            buffer.vertex(mat, x + size, y, 0).texture(u2, v1).next();
            buffer.vertex(mat, x, y, 0).texture(u1, v1).next();

            // 2. Outer hat layer
            float hu1 = 40.0f / 64.0f, hv1 = 8.0f / 64.0f;
            float hu2 = 48.0f / 64.0f, hv2 = 16.0f / 64.0f;
            buffer.vertex(mat, x, y + size, 0).texture(hu1, hv2).next();
            buffer.vertex(mat, x + size, y + size, 0).texture(hu2, hv2).next();
            buffer.vertex(mat, x + size, y, 0).texture(hu2, hv1).next();
            buffer.vertex(mat, x, y, 0).texture(hu1, hv1).next();

            tessellator.draw();
        }
    }

    public static int getScaledWidth() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.getWindow().getScaledWidth();
    }

    public static int getScaledHeight() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.getWindow().getScaledHeight();
    }
}
