package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;

public class NoSlowFOV extends Module {

    private final BooleanSetting onlySlowness = addSetting(new BooleanSetting("OnlySlowness", true));
    private final BooleanSetting staticFov = addSetting(new BooleanSetting("StaticFOV", false));

    public NoSlowFOV() {
        super("NoSlowFOV", "Prevents FOV from decreasing when slowed", Category.HUD);
    }

    public boolean isOnlySlowness() {
        return onlySlowness.isEnabled();
    }

    public boolean isStaticFov() {
        return staticFov.isEnabled();
    }
}