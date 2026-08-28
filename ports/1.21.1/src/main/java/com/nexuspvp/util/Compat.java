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
        }
    }

    public static void drawWithShadow(TextRenderer tr, MatrixStack matrices, Text text, int x, int y, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (tr == null) tr = mc.textRenderer;
        if (tr == null || text == null) return;

        if (activeContext != null) {
            activeContext.drawTextWithShadow(tr, text, x, y, color);
        }
    }

    public static void drawItem(MatrixStack matrices, ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty()) return;
        if (activeContext != null) {
            activeContext.drawItem(stack, x, y);
        }
    }

    public static void drawSkinHead(MatrixStack matrices, Identifier skin, int x, int y, int size) {
        if (skin == null || activeContext == null) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        activeContext.drawTexture(skin, x, y, size, size, 8.0f, 8.0f, 8, 8, 64, 64);
        activeContext.drawTexture(skin, x, y, size, size, 40.0f, 8.0f, 8, 8, 64, 64);
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
