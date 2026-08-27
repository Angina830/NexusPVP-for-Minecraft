package com.nexuspvp.modules;

import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShulkerPreview extends Module {

    private final ColorSetting borderColor = addSetting(new ColorSetting("Border", new Color(160, 0, 255, 220)));

    public ShulkerPreview() {
        super("ShulkerPreview", "Shows 3x9 item inventory preview on hovering shulker boxes", Category.HUD);
    }

    public static boolean isShulkerBox(Item item) {
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            return block instanceof ShulkerBoxBlock;
        }
        return false;
    }

    public static List<ItemStack> getShulkerItems(ItemStack stack) {
        List<ItemStack> items = new ArrayList<>(Collections.nCopies(27, ItemStack.EMPTY));
        if (stack == null || stack.isEmpty()) return items;
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            DefaultedList<ItemStack> list = DefaultedList.ofSize(27, ItemStack.EMPTY);
            container.copyTo(list);
            return list;
        }
        return items;
    }

    public void renderShulkerPreview(DrawContext context, ItemStack shulkerStack, int mouseX, int mouseY) {
        if (context == null || shulkerStack == null || !isShulkerBox(shulkerStack.getItem())) return;
        List<ItemStack> items = getShulkerItems(shulkerStack);

        int gridW = 9 * 18 + 8; // 170
        int gridH = 3 * 18 + 8; // 62
        int startX = mouseX + 12;
        int startY = mouseY - gridH - 12;
        if (startY < 8) startY = mouseY + 16;
        if (startX + gridW > mc.getWindow().getScaledWidth() - 4) {
            startX = mc.getWindow().getScaledWidth() - gridW - 4;
        }

        int border = borderColor.getColor().getRGB() | 0xFF000000;
        int bg = 0xF2101114;
        int slotBg = 0xFF1E1F22;
        int slotBorder = 0xFF2B2D31;

        // Background & Border
        context.fill(startX - 1, startY - 1, startX + gridW + 1, startY + gridH + 1, border);
        context.fill(startX, startY, startX + gridW, startY + gridH, bg);

        for (int i = 0; i < 27; i++) {
            int row = i / 9;
            int col = i % 9;
            int slotX = startX + 4 + col * 18;
            int slotY = startY + 4 + row * 18;

            // Draw slot box
            context.fill(slotX, slotY, slotX + 18, slotY + 18, slotBorder);
            context.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, slotBg);

            if (i < items.size()) {
                ItemStack item = items.get(i);
                if (item != null && !item.isEmpty()) {
                    context.drawItemInSlot(mc.textRenderer, item, slotX + 1, slotY + 1);
                }
            }
        }
    }
}
