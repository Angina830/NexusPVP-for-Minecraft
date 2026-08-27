package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;

import java.awt.Color;

public class ItemCooldowns extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "Both", "Numbers", "Overlay", "Both"));
    private final BooleanSetting pearlOnly = addSetting(new BooleanSetting("PearlOnly", false));
    private final ColorSetting textColor = addSetting(new ColorSetting("TextColor", new Color(255, 255, 255)));

    public ItemCooldowns() {
        super("ItemCooldowns", "Smooth circular cooldown timer over hotbar items", Category.HUD);
    }

    public void renderHotbarCooldowns(DrawContext context) {
        if (mc.player == null) return;

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int hotbarX = screenW / 2 - 90;
        int hotbarY = screenH - 22;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack == null || stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (pearlOnly.isEnabled() && !(item instanceof EnderPearlItem)) continue;

            if (mc.player.getItemCooldownManager().isCoolingDown(item)) {
                float progress = mc.player.getItemCooldownManager().getCooldownProgress(item, 0.0f);
                if (progress > 0.0f) {
                    int slotX = hotbarX + i * 20 + 3;
                    int slotY = hotbarY + 3;

                    if (style.getValue().equals("Overlay") || style.getValue().equals("Both")) {
                        int overlayH = (int) (16 * progress);
                        context.fill(slotX, slotY + (16 - overlayH), slotX + 16, slotY + 16, 0x88000000);
                    }

                    if (style.getValue().equals("Numbers") || style.getValue().equals("Both")) {
                        float sec = progress * (item instanceof EnderPearlItem ? 15.0f : (item instanceof ShieldItem ? 5.0f : 10.0f));
                        String text = sec > 1.0f ? String.format("%.0fs", sec) : String.format("%.1fs", sec);
                        int tw = mc.textRenderer.getWidth(text);
                        context.drawTextWithShadow(mc.textRenderer, text, slotX + (16 - tw) / 2, slotY + 4, textColor.getColor().getRGB() | 0xFF000000);
                    }
                }
            }
        }
    }
}
