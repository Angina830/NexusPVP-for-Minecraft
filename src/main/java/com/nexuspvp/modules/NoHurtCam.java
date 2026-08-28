package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.NumberSetting;

public class NoHurtCam extends Module {

    private final NumberSetting strength = addSetting(new NumberSetting("Strength", 0.0, 0.0, 1.0, 0.05));

    public NoHurtCam() {
        super("NoHurtCam", "Reduces or disables camera shake on hurt", Category.PVP);
    }

    public float getStrength() {
        return strength.getFloatValue();
    }
}