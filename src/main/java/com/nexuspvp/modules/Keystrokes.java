package com.nexuspvp.modules;

import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Keystrokes extends Module {

    private final NumberSetting posX = addSetting(new NumberSetting("PosX", 20, 0, 1920, 5));
    private final NumberSetting posY = addSetting(new NumberSetting("PosY", 140, 0, 1080, 5));
    private final NumberSetting scale = addSetting(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.05));
    private final BooleanSetting showWASD = addSetting(new BooleanSetting("ShowWASD", true));
    private final BooleanSetting showMouse = addSetting(new BooleanSetting("ShowMouse", true));
    private final BooleanSetting showSpace = addSetting(new BooleanSetting("ShowSpace", true));
    private final BooleanSetting showCPS = addSetting(new BooleanSetting("ShowCPS", true));

    private final List<Long> lmbClicks = new ArrayList<>();
    private final List<Long> rmbClicks = new ArrayList<>();
    private boolean prevLmb = false;
    private boolean prevRmb = false;

    // Smooth press animations (0.0 to 1.0)
    private float wAnim = 0.0f, aAnim = 0.0f, sAnim = 0.0f, dAnim = 0.0f;
    private float lmbAnim = 0.0f, rmbAnim = 0.0f, spaceAnim = 0.0f;

    public Keystrokes() {
        super("Keystrokes", "Displays pressed keys and CPS counter", Category.HUD);
    }

    public NumberSetting getPosX() { return posX; }
    public NumberSetting getPosY() { return posY; }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        boolean lmbDown = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmbDown = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        if (lmbDown && !prevLmb) {
            lmbClicks.add(now);
        }
        if (rmbDown && !prevRmb) {
            rmbClicks.add(now);
        }
        prevLmb = lmbDown;
        prevRmb = rmbDown;

        lmbClicks.removeIf(t -> now - t > 1000);
        rmbClicks.removeIf(t -> now - t > 1000);
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.options == null) return;

        int x = posX.getIntValue();
        int y = posY.getIntValue();
        float sc = scale.getFloatValue();
        int accent = ThemeManager.getInstance().getAccentColor().getRGB();

        boolean wDown = mc.options.keyForward.isPressed();
        boolean aDown = mc.options.keyLeft.isPressed();
        boolean sDown = mc.options.keyBack.isPressed();
        boolean dDown = mc.options.keyRight.isPressed();
        boolean spaceDown = mc.options.keyJump.isPressed();
        boolean lmbDown = mc.options.keyAttack.isPressed();
        boolean rmbDown = mc.options.keyUse.isPressed();

        wAnim += ((wDown ? 1.0f : 0.0f) - wAnim) * 0.35f;
        aAnim += ((aDown ? 1.0f : 0.0f) - aAnim) * 0.35f;
        sAnim += ((sDown ? 1.0f : 0.0f) - sAnim) * 0.35f;
        dAnim += ((dDown ? 1.0f : 0.0f) - dAnim) * 0.35f;
        spaceAnim += ((spaceDown ? 1.0f : 0.0f) - spaceAnim) * 0.35f;
        lmbAnim += ((lmbDown ? 1.0f : 0.0f) - lmbAnim) * 0.35f;
        rmbAnim += ((rmbDown ? 1.0f : 0.0f) - rmbAnim) * 0.35f;

        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(sc, sc, 1.0f);

        int curY = 0;
        int keySize = 22;
        int gap = 2;

        if (showWASD.isEnabled()) {
            // W Key (Center top)
            drawKey(matrices, keySize + gap, curY, keySize, keySize, "W", wAnim, accent);
            curY += keySize + gap;

            // A, S, D Keys
            drawKey(matrices, 0, curY, keySize, keySize, "A", aAnim, accent);
            drawKey(matrices, keySize + gap, curY, keySize, keySize, "S", sAnim, accent);
            drawKey(matrices, (keySize + gap) * 2, curY, keySize, keySize, "D", dAnim, accent);
            curY += keySize + gap;
        }

        if (showMouse.isEnabled()) {
            int mouseW = (keySize * 3 + gap * 2 - gap) / 2;
            int lmbCPS = lmbClicks.size();
            int rmbCPS = rmbClicks.size();

            String lmbText = showCPS.isEnabled() ? "LMB\n" + lmbCPS : "LMB";
            String rmbText = showCPS.isEnabled() ? "RMB\n" + rmbCPS : "RMB";

            drawMouseKey(matrices, 0, curY, mouseW, keySize + 4, "LMB", lmbCPS, lmbAnim, accent);
            drawMouseKey(matrices, mouseW + gap, curY, mouseW, keySize + 4, "RMB", rmbCPS, rmbAnim, accent);
            curY += keySize + 4 + gap;
        }

        if (showSpace.isEnabled()) {
            int spaceW = keySize * 3 + gap * 2;
            drawKey(matrices, 0, curY, spaceW, 12, "—", spaceAnim, accent);
        }

        matrices.pop();
    }

    private void drawKey(MatrixStack matrices, int kx, int ky, int kw, int kh, String name, float anim, int accent) {
        int bg = getBgColor(anim, accent);
        int border = (anim > 0.05f) ? accent : 0x40000000;

        RenderUtils.drawRoundedRect(matrices, kx - 1, ky - 1, kw + 2, kh + 2, 4, border);
        RenderUtils.drawRoundedRect(matrices, kx, ky, kw, kh, 3, bg);

        int textW = mc.textRenderer.getWidth(name);
        int textCol = anim > 0.5f ? 0xFFFFFFFF : 0xFFDBDEE1;
        mc.textRenderer.drawWithShadow(matrices, name, kx + (kw - textW) / 2.0f, ky + (kh - 8) / 2.0f, textCol);
    }

    private void drawMouseKey(MatrixStack matrices, int kx, int ky, int kw, int kh, String name, int cps, float anim, int accent) {
        int bg = getBgColor(anim, accent);
        int border = (anim > 0.05f) ? accent : 0x40000000;

        RenderUtils.drawRoundedRect(matrices, kx - 1, ky - 1, kw + 2, kh + 2, 4, border);
        RenderUtils.drawRoundedRect(matrices, kx, ky, kw, kh, 3, bg);

        int textW = mc.textRenderer.getWidth(name);
        int textCol = anim > 0.5f ? 0xFFFFFFFF : 0xFFDBDEE1;
        mc.textRenderer.drawWithShadow(matrices, name, kx + (kw - textW) / 2.0f, ky + 3, textCol);

        if (showCPS.isEnabled()) {
            String cpsText = cps + " CPS";
            int cpsW = mc.textRenderer.getWidth(cpsText);
            mc.textRenderer.drawWithShadow(matrices, cpsText, kx + (kw - cpsW) / 2.0f, ky + 13, 0xFF949BA4);
        }
    }

    private int getBgColor(float anim, int accent) {
        int r = (int) (20 + (((accent >> 16) & 0xFF) - 20) * anim * 0.75f);
        int g = (int) (22 + (((accent >> 8) & 0xFF) - 22) * anim * 0.75f);
        int b = (int) (26 + ((accent & 0xFF) - 26) * anim * 0.75f);
        int a = (int) (160 + 80 * anim);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}