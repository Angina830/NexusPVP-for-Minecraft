package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.util.Identifier;

public class MotionBlur extends Module {

    private final NumberSetting strength = addSetting(new NumberSetting("Strength", 5, 1, 10, 1));

    public MotionBlur() {
        super("MotionBlur", "Smooth cinematic camera motion blur effect", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.gameRenderer != null) {
            try {
                mc.gameRenderer.loadPostProcessor(Identifier.of("minecraft", "shaders/post/phosphor.json"));
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void onDisable() {
        if (mc.gameRenderer != null) {
            try {
                mc.gameRenderer.disablePostProcessor();
            } catch (Throwable ignored) {}
        }
    }

    public float getStrength() {
        return strength.getFloatValue();
    }
}
