package com.nexuspvp.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class Compat {

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
        if (tr != null && text != null) {
            DrawContext context = new DrawContext(mc, mc.getBufferBuilders().getEntityVertexConsumers());
            context.drawTextWithShadow(tr, text, x, y, color);
        }
    }

    public static void drawWithShadow(TextRenderer tr, MatrixStack matrices, Text text, int x, int y, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (tr == null) tr = mc.textRenderer;
        if (tr != null && text != null) {
            DrawContext context = new DrawContext(mc, mc.getBufferBuilders().getEntityVertexConsumers());
            context.drawTextWithShadow(tr, text, x, y, color);
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
