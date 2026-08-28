package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;

public class SwingAnimations extends Module {

    private final ModeSetting style = new ModeSetting("Style", "1.7", "1.7", "Smooth", "Sigma", "Spin", "Push", "Down");
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.3, 3.0, 0.1);

    public SwingAnimations() {
        super("SwingAnimations", "Custom attack/swing animations", Category.PLAYER);
        addSetting(style);
        addSetting(speed);
    }

    public String getStyle() {
        return style.getValue();
    }

    public float getSpeed() {
        return speed.getFloatValue();
    }
}