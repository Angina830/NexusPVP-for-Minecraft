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
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Particles extends Module {

    private final BooleanSetting damageNumbers = new BooleanSetting("DamageNumbers", true);
    private final BooleanSetting hearts = new BooleanSetting("Hearts", true);
    private final ColorSetting color = new ColorSetting("Color", Color.RED);
    private final NumberSetting size = new NumberSetting("Size", 1.0, 0.5, 3.0, 0.1);
    private final ModeSetting style = new ModeSetting("Style", "Default", "Default", "Crit", "Hearts");

    public Particles() {
        super("Particles", "Damage numbers and hit particles", Category.VISUAL, 0);
        addSetting(damageNumbers);
        addSetting(hearts);
        addSetting(color);
        addSetting(size);
        addSetting(style);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {}
}
