package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

public class ShulkerPreview extends Module {

    private final BooleanSetting showEmpty = addSetting(new BooleanSetting("ShowEmpty", true));

    public ShulkerPreview() {
        super("ShulkerPreview", "Shows a 3x9 grid preview of items inside shulker boxes", Category.HUD);
    }

    public boolean isShulkerBox(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock;
    }

    public boolean renderShulkerPreview(MatrixStack matrices, ItemStack stack, int mouseX, int mouseY) {
        if (!isEnabled() || !isShulkerBox(stack) || mc.player == null) return false;

        NbtCompound blockEntityTag = stack.getSubTag("BlockEntityTag");
        if (blockEntityTag == null || !blockEntityTag.contains("Items", 9)) {
            return false;
        }

        NbtList itemsTag = blockEntityTag.getList("Items", 10);
        DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);

        for (int i = 0; i < itemsTag.size(); i++) {
            NbtCompound itemTag = itemsTag.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < 27) {
                items.set(slot, ItemStack.fromNbt(itemTag));
            }
        }

        int width = 166;
        int height = 74;

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int x = mouseX + 12;
        int y = mouseY - 12;

        if (x + width > screenW - 4) {
            x = mouseX - width - 8;
        }
        if (y + height > screenH - 4) {
            y = screenH - height - 4;
        }
        if (y < 4) {
            y = 4;
        }

        int accent = ThemeManager.getInstance().getAccentColor().getRGB();

        // 1. Draw Background and Slots
        RenderSystem.disableDepthTest();
        RenderUtils.drawRoundedRect(matrices, x - 1, y - 1, width + 2, height + 2, 5, accent);
        RenderUtils.drawRoundedRect(matrices, x, y, width, height, 4, 0xFA1E1F22);

        // Header: Shulker Box name
        String title = stack.getName().getString();
        if (mc.textRenderer.getWidth(title) > width - 12) {
            title = title.substring(0, Math.min(title.length(), 22)) + "..";
        }
        mc.textRenderer.drawWithShadow(matrices, title, x + 6, y + 5, 0xFFF2F3F5);

        // Separator line
        RenderUtils.drawRect(matrices, x + 4, y + 16, width - 8, 1, 0xFF2B2D31);

        int startSlotX = x + 3;
        int startSlotY = y + 19;

        // Draw slot rectangles
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = startSlotX + col * 18;
                int slotY = startSlotY + row * 18;
                RenderUtils.drawRoundedRect(matrices, slotX, slotY, 17, 17, 2, 0xFF2B2D31);
            }
        }
        RenderSystem.enableDepthTest();

        // 2. Render all 27 Items with custom zOffset
        mc.getItemRenderer().zOffset = 300.0F;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                int slotX = startSlotX + col * 18;
                int slotY = startSlotY + row * 18;

                ItemStack item = items.get(index);
                if (!item.isEmpty()) {
                    mc.getItemRenderer().renderInGuiWithOverrides(item, slotX + 1, slotY + 1);
                    mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, item, slotX + 1, slotY + 1);
                }
            }
        }
        mc.getItemRenderer().zOffset = 0.0F;

        return true;
    }
}
