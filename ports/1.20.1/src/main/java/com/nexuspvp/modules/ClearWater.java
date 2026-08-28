package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;


import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.NumberSetting;

public class ClearWater extends Module {

    private final NumberSetting distance = addSetting(new NumberSetting("Distance", 180.0, 50.0, 300.0, 10.0));

    public ClearWater() {
        super("ClearWater", "Removes murky underwater fog for crystal clear vision", Category.RENDER);
    }

    public float getFogDistance() {
        return distance.getFloatValue();
    }
}