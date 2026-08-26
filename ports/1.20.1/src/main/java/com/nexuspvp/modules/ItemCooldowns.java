package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.awt.Color;

public class ItemCooldowns extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "Numbers", "Numbers", "Overlay", "Both"));
    private final BooleanSetting pearlOnly = addSetting(new BooleanSetting("PearlOnly", false));
    private final ColorSetting textColor = addSetting(new ColorSetting("TextColor", new Color(255, 255, 255)));

    public ItemCooldowns() {
        super("ItemCooldowns", "Smooth circular cooldown timer over hotbar items", Category.HUD);
    }

    public void renderHotbarCooldowns(DrawContext context) {
        if (mc.player == null) return;

        int screenW = Compat.getScaledWidth();
        int screenH = Compat.getScaledHeight();
        int hotbarX = screenW / 2 - 90;
        int hotbarY = screenH - 22;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (mc.player.getItemCooldownManager().isCoolingDown(item)) {
                float progress = mc.player.getItemCooldownManager().getCooldownProgress(item, 0.0f);
                if (progress > 0.0f) {
                    int slotX = hotbarX + i * 20 + 3;
                    int slotY = hotbarY + 3;

                    if (style.getValue().equals("Overlay") || style.getValue().equals("Both")) {
                        int overlayH = (int) (16 * progress);
                        RenderUtils.drawRect(context.getMatrices(), slotX, slotY + (16 - overlayH), 16, overlayH, 0x88000000);
                    }

                    if (style.getValue().equals("Numbers") || style.getValue().equals("Both")) {
                        float remainingSec = progress * 15.0f; // Approx cooldown
                        String text = remainingSec > 1.0f ? String.format("%.0fs", remainingSec) : String.format("%.1fs", remainingSec);
                        int tw = mc.textRenderer.getWidth(text);
                        context.drawText(mc.textRenderer, text, slotX + (16 - tw) / 2, slotY + 4, textColor.getColor().getRGB() | 0xFF000000, true);
                    }
                }
            }
        }
    }
}
