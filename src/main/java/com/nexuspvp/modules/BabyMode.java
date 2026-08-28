package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;

public class BabyMode extends Module {

    private final NumberSetting scale = new NumberSetting("Scale", 0.5, 0.1, 1.0, 0.05);
    private final BooleanSetting onlySelf = new BooleanSetting("OnlySelf", true);

    public BabyMode() {
        super("BabyMode", "Shrink player model to baby size", Category.PLAYER);
        addSetting(scale);
        addSetting(onlySelf);
    }

    public float getScale() {
        return scale.getFloatValue();
    }

    public boolean isOnlySelf() {
        return onlySelf.isEnabled();
    }
}
