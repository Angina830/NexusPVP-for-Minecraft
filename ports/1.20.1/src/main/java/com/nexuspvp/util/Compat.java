package com.nexuspvp.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
        MinecraftClient mc = MinecraftClient.getInstance();
        if (activeContext != null) {
            activeContext.drawItem(stack, x, y);
            activeContext.drawStackOverlay(mc.textRenderer, stack, x, y);
        }
    }

    public static void drawSkinHead(MatrixStack matrices, Identifier skin, int x, int y, int size) {
        if (skin == null) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (activeContext != null) {
            activeContext.drawTexture(skin, x, y, 8, 8, 8, 8, size, size, 64, 64);
            activeContext.drawTexture(skin, x, y, 40, 8, 8, 8, size, size, 64, 64);
        }
    }

    public static int getScaledWidth() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.getWindow() != null ? mc.getWindow().getScaledWidth() : 800;
    }

    public static int getScaledHeight() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.getWindow() != null ? mc.getWindow().getScaledHeight() : 600;
    }
}
