package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.awt.Color;

public class ItemCooldowns extends Module {

    private final BooleanSetting circular = addSetting(new BooleanSetting("Circular", true));
    private final BooleanSetting showSeconds = addSetting(new BooleanSetting("Seconds", true));
    private final BooleanSetting customColor = addSetting(new BooleanSetting("CustomColor", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 200, 255)));

    public ItemCooldowns() {
        super("ItemCooldowns", "Smooth circular cooldown timer over hotbar items", Category.HUD);
    }

    public void renderCooldownOverlay(MatrixStack matrices, int x, int y, float progress, Item item) {
        if (progress <= 0.0f || mc.player == null) return;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int accent = customColor.isEnabled() ? color.getColor().getRGB() : ThemeManager.getInstance().getAccentColor().getRGB();

        // Dark dim backdrop over item
        RenderUtils.drawRoundedRect(matrices, x, y, 16, 16, 2, 0x88000000);

        if (circular.isEnabled()) {
            // Circular arc progress
            float angle = 360.0f * progress;
            RenderUtils.drawFilledArc2D(matrices, x + 8, y + 8, 7.0f, -90, -90 + angle, (0xBB << 24) | (accent & 0x00FFFFFF));
        }

        if (showSeconds.isEnabled()) {
            // Accurate seconds calculation
            float maxCdSeconds = getMaxCooldownSeconds(item);
            float remainingSec = progress * maxCdSeconds;
            String secText = remainingSec >= 10.0f ? String.format("%.0f", remainingSec) : String.format("%.1f", remainingSec);
            
            int tw = mc.textRenderer.getWidth(secText);

            matrices.push();
            float sc = 0.70f;
            float cx = x + 8;
            float cy = y + 8;
            matrices.translate(cx, cy, 0);
            matrices.scale(sc, sc, 1.0f);
            matrices.translate(-cx, -cy, 0);

            mc.textRenderer.drawWithShadow(matrices, secText, x + 8 - tw / 2.0f, y + 4.5f, 0xFFFFFFFF);
            matrices.pop();
        }

        RenderSystem.enableDepthTest();
    }

    private float getMaxCooldownSeconds(Item item) {
        if (item == Items.ENDER_PEARL) return 15.0f;
        if (item == Items.CHORUS_FRUIT) return 1.0f;
        if (item == Items.SHIELD) return 5.0f;
        if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) return 2.0f;
        return 10.0f;
    }
}