package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;


import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.util.Identifier;

import java.awt.Color;

public class AttackVignette extends Module {

    private final NumberSetting maxOpacity = addSetting(new NumberSetting("MaxOpacity", 140, 20, 255, 5));
    private final BooleanSetting onlyWeapon = addSetting(new BooleanSetting("OnlyWeapon", false));
    private final BooleanSetting flashOnReady = addSetting(new BooleanSetting("FlashOnReady", true));
    private final NumberSetting vignetteSize = addSetting(new NumberSetting("VignetteSize", 40, 15, 100, 5));

    private float readyFlashAnim = 0.0f;
    private boolean wasReady = false;

    private static final Identifier VIGNETTE_TEXTURE = new Identifier("textures/misc/vignette.png");

    public AttackVignette() {
        super("AttackVignette", "Screen vignette transitions red to green on attack cooldown", Category.PVP);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        float progress = mc.player.getAttackCooldownProgress(0.0f);
        boolean isReady = (progress >= 0.99f);

        if (isReady && !wasReady) {
            readyFlashAnim = 1.0f;
        }
        wasReady = isReady;

        readyFlashAnim += (0.0f - readyFlashAnim) * 0.12f;
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null) return;

        if (onlyWeapon.isEnabled()) {
            boolean hasWeapon = mc.player.getMainHandStack().getItem() instanceof SwordItem ||
                               mc.player.getMainHandStack().getItem() instanceof AxeItem;
            if (!hasWeapon) return;
        }

        float progress = mc.player.getAttackCooldownProgress(tickDelta);
        int screenW = Compat.getScaledWidth();
        int screenH = Compat.getScaledHeight();

        // Calculate smooth color from Red (0.0) -> Orange -> Yellow -> Green (1.0)
        float r, g, b;
        if (progress < 0.5f) {
            // Red to Yellow
            float factor = progress * 2.0f;
            r = 1.0f;
            g = factor * 0.85f;
            b = 0.05f;
        } else {
            // Yellow to Green
            float factor = (progress - 0.5f) * 2.0f;
            r = 1.0f - factor * 0.85f;
            g = 0.85f + factor * 0.15f;
            b = 0.1f * factor;
        }

        float baseAlpha = (maxOpacity.getIntValue() / 255.0f);
        
        // Slight opacity modulation: stronger when cooldown is low or flashing on ready
        float alpha = baseAlpha * (0.6f + (1.0f - progress) * 0.4f);
        if (flashOnReady.isEnabled() && readyFlashAnim > 0.01f) {
            alpha = Math.min(1.0f, alpha + readyFlashAnim * 0.5f);
        }

        int alphaInt = (int) (Math.max(0.0f, Math.min(1.0f, alpha)) * 255);
        if (alphaInt <= 2) return;

        // Render soft border vignette gradients
        int size = vignetteSize.getIntValue();
        int edgeColor = (alphaInt << 24) | (((int) (r * 255)) << 16) | (((int) (g * 255)) << 8) | ((int) (b * 255));
        int transparent = (0x00 << 24) | (((int) (r * 255)) << 16) | (((int) (g * 255)) << 8) | ((int) (b * 255));

        // Top edge
        RenderUtils.drawGradientRect(matrices, 0, 0, screenW, size, edgeColor, transparent);
        // Bottom edge
        RenderUtils.drawGradientRect(matrices, 0, screenH - size, screenW, size, transparent, edgeColor);
        // Left & Right subtle edges
        RenderUtils.drawRect(matrices, 0, 0, 3, screenH, edgeColor);
        RenderUtils.drawRect(matrices, screenW - 3, 0, 3, screenH, edgeColor);
    }
}