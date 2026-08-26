package com.nexuspvp.modules;

import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;

public class BlockOutline extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "Theme", "Theme", "Custom", "Rainbow"));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(88, 101, 242)));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 6.0, 0.5));
    private final BooleanSetting fill = addSetting(new BooleanSetting("Fill", true));
    private final NumberSetting fillAlpha = addSetting(new NumberSetting("FillAlpha", 30, 0, 150, 5));

    public BlockOutline() {
        super("BlockOutline", "Custom glowing block selection outline", Category.VISUAL);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult bhr = (BlockHitResult) mc.crosshairTarget;
        BlockPos pos = bhr.getBlockPos();

        if (mc.world.isAir(pos)) return;

        Color c;
        if (style.getValue().equals("Rainbow")) {
            c = ColorUtils.rainbow(System.currentTimeMillis());
        } else if (style.getValue().equals("Custom")) {
            c = color.getColor();
        } else {
            c = ThemeManager.getInstance().getAccentColor();
        }

        RenderUtils.drawBlockOutline3D(pos, c, lineWidth.getFloatValue(), fill.isEnabled(), fillAlpha.getIntValue());
    }
}
