package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class GalaxySky extends Module {

    private final ColorSetting starColor = new ColorSetting("StarColor", new Color(180, 100, 255));
    private final ColorSetting nebulaColor1 = new ColorSetting("Nebula1", new Color(40, 0, 80));
    private final ColorSetting nebulaColor2 = new ColorSetting("Nebula2", new Color(0, 30, 80));
    private final NumberSetting starCount = new NumberSetting("Stars", 300, 50, 1000, 25);
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);
    private final BooleanSetting rotate = new BooleanSetting("Rotate", true);

    public GalaxySky() {
        super("GalaxySky", "Transforms the night sky with animated nebulae and custom starfield", Category.RENDER, 0);
        addSetting(starColor);
        addSetting(nebulaColor1);
        addSetting(nebulaColor2);
        addSetting(starCount);
        addSetting(speed);
        addSetting(rotate);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {}
}
