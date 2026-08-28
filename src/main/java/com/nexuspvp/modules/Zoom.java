package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {

    private final NumberSetting factor = new NumberSetting("Factor", 4.0, 1.5, 10.0, 0.5);
    private final BooleanSetting smooth = new BooleanSetting("Smooth", true);
    private final NumberSetting scrollStep = new NumberSetting("ScrollStep", 0.5, 0.1, 2.0, 0.1);

    public Zoom() {
        super("Zoom", "Camera zoom on key hold", Category.PLAYER, GLFW.GLFW_KEY_C);
        addSetting(factor);
        addSetting(smooth);
        addSetting(scrollStep);
    }

    public float getFactor() {
        return factor.getFloatValue();
    }
    
    public void setFactor(float f) {
        // We'd have a setValue on NumberSetting theoretically
        // factor.setValue(f);
    }

    public boolean isSmooth() {
        return smooth.isEnabled();
    }

    public float getScrollStep() {
        return scrollStep.getFloatValue();
    }
    
    public boolean isZooming() {
        return isEnabled() && GLFW.glfwGetKey(mc.getWindow().getHandle(), getKeyBind()) == GLFW.GLFW_PRESS;
    }
}
