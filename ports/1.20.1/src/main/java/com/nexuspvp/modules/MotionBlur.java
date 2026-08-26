package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.NumberSetting;

public class MotionBlur extends Module {

    private final NumberSetting strength = addSetting(new NumberSetting("Strength", 5, 1, 10, 1));

    public MotionBlur() {
        super("MotionBlur", "Smooth cinematic camera motion blur effect", Category.RENDER);
    }

    public float getStrength() {
        return strength.getFloatValue();
    }
}