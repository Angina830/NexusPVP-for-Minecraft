package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;


import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ArmorHUD extends Module {

    private final ModeSetting orientation = addSetting(new ModeSetting("Orientation", "Horizontal", "Horizontal", "Vertical"));
    private final NumberSetting posX = addSetting(new NumberSetting("PosX", 0, -500, 500, 5));
    private final NumberSetting posY = addSetting(new NumberSetting("PosY", -48, -400, 400, 5));
    private final BooleanSetting showDurability = addSetting(new BooleanSetting("ShowDurability", true));
    private final BooleanSetting exactDurability = addSetting(new BooleanSetting("ExactDurability", true));
    private final BooleanSetting lowDurabilityAlert = addSetting(new BooleanSetting("LowDurabilityAlert", true));
    private final BooleanSetting showTotemCount = addSetting(new BooleanSetting("ShowTotems", true));
    private final BooleanSetting showArrowCount = addSetting(new BooleanSetting("ShowArrows", true));
    private final NumberSetting scale = addSetting(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.05));

    public ArmorHUD() {
        super("ArmorHUD", "Displays equipped armor, durability and item counters", Category.HUD);
    }

    public NumberSetting getPosX() { return posX; }
    public NumberSetting getPosY() { return posY; }
    public NumberSetting getScale() { return scale; }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null) return;

        int screenW = Compat.getScaledWidth();
        int screenH = Compat.getScaledHeight();
        int centerX = screenW / 2 + posX.getIntValue();
        int centerY = screenH / 2 + posY.getIntValue();

        List<ItemStack> items = new ArrayList<>();
        ItemStack offhand = mc.player.getOffHandStack();
        if (!offhand.isEmpty()) items.add(offhand);

        for (int i = 3; i >= 0; i--) {
            ItemStack armor = mc.player.getInventory().armor.get(i);
            if (!armor.isEmpty()) items.add(armor);
        }

        ItemStack mainHand = mc.player.getMainHandStack();
        if (!mainHand.isEmpty()) items.add(mainHand);

        if (items.isEmpty()) return;

        boolean isHorizontal = orientation.getValue().equals("Horizontal");
        int itemSize = 18;
        int spacing = 20;

        int totalW = isHorizontal ? items.size() * spacing : itemSize;
        int startX = centerX - totalW / 2;
        int startY = centerY;

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            int ix = isHorizontal ? startX + i * spacing : startX;
            int iy = isHorizontal ? startY : startY + i * spacing;

            float pct = 1.0f;
            int remain = 0;
            if (stack.isDamageable()) {
                int maxDmg = stack.getMaxDamage();
                int curDmg = stack.getDamage();
                remain = maxDmg - curDmg;
                pct = (float) remain / maxDmg;
            }

            if (lowDurabilityAlert.isEnabled() && stack.isDamageable() && pct < 0.12f) {
                int pulseRed = (System.currentTimeMillis() % 600 < 300) ? 0xFFED4245 : 0x66ED4245;
                RenderUtils.drawRoundedRect(matrices, ix - 2, iy - 2, itemSize + 4, itemSize + 4, 4, pulseRed);
            }

            RenderUtils.drawRoundedRect(matrices, ix - 1, iy - 1, itemSize + 2, itemSize + 2, 3, 0xBB1E1F22);
            // mc.getItemRenderer().renderInGui(stack, ix + 1, iy + 1);
            // mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, stack, ix + 1, iy + 1);

            if (showDurability.isEnabled() && stack.isDamageable()) {
                int barW = itemSize;
                int fillW = (int) (barW * pct);
                int barY = iy + itemSize + 2;

                int col = pct > 0.6f ? 0xFF23A55A : (pct > 0.25f ? 0xFFFEE75C : 0xFFED4245);
                RenderUtils.drawRect(matrices, ix, barY, barW, 2, 0xFF2B2D31);
                if (fillW > 0) {
                    RenderUtils.drawRect(matrices, ix, barY, fillW, 2, col);
                }

                String text = exactDurability.isEnabled() ? String.valueOf(remain) : (int) (pct * 100) + "%";
                int pw = mc.textRenderer.getWidth(text);
                matrices.push();
                matrices.translate(ix + (itemSize - pw * 0.5f) / 2, barY + 3, 0);
                matrices.scale(0.5f, 0.5f, 1.0f);
                Compat.drawText(matrices, text, 0, 0, col);
                matrices.pop();
            }
        }

        if (showTotemCount.isEnabled()) {
            int totems = countItem(Items.TOTEM_OF_UNDYING);
            if (totems > 0) {
                int tx = startX + totalW + 4;
                int ty = startY;
                RenderUtils.drawRoundedRect(matrices, tx, ty, 38, itemSize, 4, 0xEE1E1F22);
                // mc.getItemRenderer().renderInGui(new ItemStack(Items.TOTEM_OF_UNDYING), tx + 1, ty + 1);
                Compat.drawText(matrices, "x" + totems, tx + 19, ty + 5, 0xFFFFD700);
            }
        }

        if (showArrowCount.isEnabled()) {
            int arrows = countItem(Items.ARROW) + countItem(Items.SPECTRAL_ARROW) + countItem(Items.TIPPED_ARROW);
            if (arrows > 0) {
                int ax = startX - 42;
                int ay = startY;
                RenderUtils.drawRoundedRect(matrices, ax, ay, 38, itemSize, 4, 0xEE1E1F22);
                // mc.getItemRenderer().renderInGui(new ItemStack(Items.ARROW), ax + 1, ay + 1);
                Compat.drawText(matrices, "x" + arrows, ax + 19, ay + 5, 0xFFDBDEE1);
            }
        }
    }

    private int countItem(net.minecraft.item.Item item) {
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!s.isEmpty() && s.getItem() == item) {
                count += s.getCount();
            }
        }
        return count;
    }
}
