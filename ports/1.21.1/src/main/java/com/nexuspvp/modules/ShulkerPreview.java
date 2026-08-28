package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.awt.Color;

public class ShulkerPreview extends Module {
    private final BooleanSetting showEmpty = addSetting(new BooleanSetting("ShowEmpty", false));
    private final NumberSetting scale = addSetting(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final ColorSetting borderColor = addSetting(new ColorSetting("BorderColor", new Color(160, 0, 255, 200)));

    public ShulkerPreview() {
        super("ShulkerPreview", "Preview shulker box contents on hover", Category.MISC);
    }

    public static boolean isShulkerBox(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String name = stack.getItem().getTranslationKey();
        return name.contains("shulker_box");
    }

    public static DefaultedList<ItemStack> getShulkerItems(ItemStack stack) {
        DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
        if (stack == null || stack.isEmpty()) return items;

        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            int slot = 0;
            for (ItemStack item : container.iterateNonEmpty()) {
                if (slot < 27) {
                    items.set(slot++, item);
                }
            }
        }
        return items;
    }

    public void renderShulkerPreview(net.minecraft.client.gui.DrawContext context, ItemStack stack, int x, int y) {
        if (!isEnabled() || !isShulkerBox(stack)) return;
        DefaultedList<ItemStack> items = getShulkerItems(stack);
        int previewX = x + 10;
        int previewY = y - 30;
        int previewW = 9 * 18 + 8;
        int previewH = 3 * 18 + 8;
        context.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0xDD12121A);
        for (int i = 0; i < 27; i++) {
            int ix = previewX + 4 + (i % 9) * 18;
            int iy = previewY + 4 + (i / 9) * 18;
            context.fill(ix, iy, ix + 16, iy + 16, 0x44000000);
            ItemStack item = items.get(i);
            if (!item.isEmpty()) {
                context.drawItem(item, ix, iy);
                context.drawItemInSlot(mc.textRenderer, item, ix, iy);
            }
        }
    }

}
