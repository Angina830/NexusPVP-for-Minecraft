package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ShulkerPreview extends Module {

    private final BooleanSetting showEmpty = new BooleanSetting("ShowEmpty", false);
    private final ColorSetting borderColor = new ColorSetting("Border", new Color(160, 0, 255, 200));

    public ShulkerPreview() {
        super("ShulkerPreview", "Preview shulker box items on tooltip hover", Category.HUD, 0);
        addSetting(showEmpty);
        addSetting(borderColor);
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
        NbtCompound tag = null;
        if (tag == null || !tag.contains("BlockEntityTag", 10)) return items;
        NbtCompound blockEntityTag = tag.getCompound("BlockEntityTag");
        if (!blockEntityTag.contains("Items", 9)) return items;
        NbtList tagList = blockEntityTag.getList("Items", 10);
        for (int i = 0; i < tagList.size(); i++) {
            NbtCompound itemTag = tagList.getCompound(i);
            items.add(ItemStack.fromNbt(itemTag));
        }
        return items;
    }
}
