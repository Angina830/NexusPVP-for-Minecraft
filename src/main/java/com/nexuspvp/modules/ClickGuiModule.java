package com.nexuspvp.modules;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Module {

    public ClickGuiModule() {
        super("ClickGui", "Module that opens the ClickGui screen", Category.GUI, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen == null) {
            mc.openScreen(new com.nexuspvp.gui.ClickGui());
        }
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen instanceof com.nexuspvp.gui.ClickGui) {
            mc.openScreen(null);
        }
    }
}
