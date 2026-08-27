package com.nexuspvp.modules;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.gui.ClickGui;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.util.Compat;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Module {

    public ClickGuiModule() {
        super("ClickGui", "Module that opens the ClickGui screen", Category.GUI, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen == null) {
            ClickGui.openCurrentStyleScreen();
        }
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen != null) {
            Compat.setScreen(mc, null);
        }
    }
}
