package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.NumberSetting;

public class LowFire extends Module {

    private final NumberSetting height = addSetting(new NumberSetting("Height", 35, 0, 80, 5));

    public LowFire() {
        super("LowFire", "Lowers screen fire height for better visibility", Category.RENDER);
    }

    public float getOffset() {
        return (height.getIntValue() / 100.0f) * 0.7f;
    }
}
