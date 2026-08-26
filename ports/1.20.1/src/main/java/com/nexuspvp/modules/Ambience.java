package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;


import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import java.awt.Color;

public class Ambience extends Module {

    private final NumberSetting time = new NumberSetting("Time", 6000, 0, 24000, 100);
    private final BooleanSetting customTime = new BooleanSetting("CustomTime", false);
    
    private final ColorSetting skyColor = new ColorSetting("SkyColor", new Color(120, 170, 255));
    private final BooleanSetting customSky = new BooleanSetting("CustomSky", false);
    
    private final NumberSetting brightness = new NumberSetting("Brightness", 1.0, 0.0, 15.0, 0.1);
    private final BooleanSetting fullbright = new BooleanSetting("Fullbright", false);

    public Ambience() {
        super("Ambience", "Change game lighting/time/sky color", Category.RENDER);
        addSetting(time);
        addSetting(customTime);
        addSetting(skyColor);
        addSetting(customSky);
        addSetting(brightness);
        addSetting(fullbright);
    }

    public long getTime() {
        return (long) time.getFloatValue();
    }

    public boolean isCustomTime() {
        return customTime.isEnabled();
    }

    public Color getSkyColor() {
        return skyColor.getColor();
    }

    public boolean isCustomSky() {
        return customSky.isEnabled();
    }

    public float getBrightness() {
        if (fullbright.isEnabled()) {
            return 15.0f;
        }
        return brightness.getFloatValue();
    }

    public boolean isFullbright() {
        return fullbright.isEnabled();
    }

    @Override
    public void onTick() {
        if (mc.world != null && isEnabled() && isCustomTime()) {
            mc.world.setTimeOfDay(getTime());
        }
        if (mc.options != null && isEnabled()) {
            if (fullbright.isEnabled()) {
                mc.options.getGamma().setValue((double)(15.0));
            } else if (brightness.getFloatValue() != 1.0f) {
                mc.options.getGamma().setValue((double)((double) brightness.getFloatValue()));
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getGamma().setValue((double)(1.0));
        }
    }
}
