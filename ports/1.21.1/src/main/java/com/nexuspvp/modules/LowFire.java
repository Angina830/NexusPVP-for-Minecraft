package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;


import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.NumberSetting;

public class LowFire extends Module {

    private final NumberSetting offset = addSetting(new NumberSetting("Height", 0.35, 0.0, 0.8, 0.05));

    public LowFire() {
        super("LowFire", "Lowers screen fire height for better visibility", Category.HUD);
    }

    public float getOffset() {
        return offset.getFloatValue();
    }
}