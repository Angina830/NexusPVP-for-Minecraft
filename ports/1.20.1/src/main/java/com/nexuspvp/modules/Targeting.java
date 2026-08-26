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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.awt.Color;

public class Targeting extends Module {

    private final ColorSetting color = new ColorSetting("Color", new Color(255, 60, 60, 200));
    private final NumberSetting range = new NumberSetting("Range", 4.0, 2.0, 8.0, 0.5);
    private final ModeSetting style = new ModeSetting("Style", "Circle", "Circle", "Box", "Diamond");
    private final NumberSetting lineWidth = new NumberSetting("LineWidth", 2.0, 1.0, 5.0, 0.5);
    private final BooleanSetting animate = new BooleanSetting("Animate", true);

    public Targeting() {
        super("Targeting", "Highlights targeted entity with visual effects", Category.VISUAL, 0);
        addSetting(color);
        addSetting(range);
        addSetting(style);
        addSetting(lineWidth);
        addSetting(animate);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.targetedEntity == null || !(mc.targetedEntity instanceof LivingEntity)) return;
    }
}
