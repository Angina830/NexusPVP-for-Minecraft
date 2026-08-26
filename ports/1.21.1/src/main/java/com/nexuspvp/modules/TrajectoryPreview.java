package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryPreview extends Module {

    private final ColorSetting color = new ColorSetting("Color", new Color(0, 255, 200, 220));
    private final BooleanSetting showHitBlock = new BooleanSetting("HitBox", true);
    private final ModeSetting style = new ModeSetting("Style", "Smooth", "Smooth", "Dotted");
    private final NumberSetting lineWidth = new NumberSetting("Width", 2.0, 1.0, 5.0, 0.5);

    public TrajectoryPreview() {
        super("TrajectoryPreview", "Draws flight trajectory for pearls, bows, and potions", Category.VISUAL, 0);
        addSetting(color);
        addSetting(showHitBlock);
        addSetting(style);
        addSetting(lineWidth);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null) return;
        ItemStack stack = mc.player.getMainHandStack();
        if (!(stack.getItem() instanceof EnderPearlItem || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof PotionItem || stack.getItem() instanceof TridentItem)) {
            stack = mc.player.getOffHandStack();
        }
        if (!(stack.getItem() instanceof EnderPearlItem || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof PotionItem || stack.getItem() instanceof TridentItem)) {
            return;
        }
    }
}
