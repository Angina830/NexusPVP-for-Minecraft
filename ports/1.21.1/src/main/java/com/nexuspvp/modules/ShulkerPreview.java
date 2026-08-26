package com.nexuspvp.modules;

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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ShulkerPreview extends Module {

    private final ColorSetting borderColor = addSetting(new ColorSetting("Border", new Color(160, 0, 255, 200)));

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
        List<ItemStack> items = new ArrayList<>();
        if (stack == null || stack.isEmpty()) return items;
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            for (ItemStack s : container.iterateNonEmpty()) {
                items.add(s);
            }
        }
        return items;
    }

    public void renderShulkerPreview(DrawContext context, ItemStack shulkerStack, int mouseX, int mouseY) {
        if (context == null || shulkerStack == null || !isShulkerBox(shulkerStack.getItem())) return;
        List<ItemStack> items = getShulkerItems(shulkerStack);
        if (items.isEmpty()) return;

        int gridW = 9 * 18 + 6;
        int gridH = 3 * 18 + 6;
        int startX = mouseX + 12;
        int startY = mouseY - gridH - 6;
        if (startY < 10) startY = mouseY + 20;

        RenderUtils.drawRoundedRect(context.getMatrices(), startX - 1, startY - 1, gridW + 2, gridH + 2, 4, borderColor.getColor().getRGB());
        RenderUtils.drawRoundedRect(context.getMatrices(), startX, startY, gridW, gridH, 3, 0xF01E1F22);

        int idx = 0;
        for (ItemStack item : items) {
            if (idx >= 27) break;
            int row = idx / 9;
            int col = idx % 9;
            int slotX = startX + 3 + col * 18;
            int slotY = startY + 3 + row * 18;

            RenderUtils.drawRoundedRect(context.getMatrices(), slotX, slotY, 18, 18, 2, 0xFF2B2D31);
            if (!item.isEmpty()) {
                context.drawItem(item, slotX + 1, slotY + 1);
                context.drawItemOverlay(mc.textRenderer, item, slotX + 1, slotY + 1);
            }
            idx++;
        }
    }
}
