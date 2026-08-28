package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import net.minecraft.client.input.Input;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SmartSprint extends Module {

    private final BooleanSetting ctrlOnlyRun = addSetting(new BooleanSetting("CtrlRun", true));
    private final BooleanSetting pauseOnS = addSetting(new BooleanSetting("PauseOnS", true));

    public SmartSprint() {
        super("SmartSprint", "Sprint by pressing Ctrl with S-pause mechanic", Category.PLAYER);
    }

    public void modifyInput(Input input) {
        if (mc.player == null || mc.currentScreen != null || !ctrlOnlyRun.isEnabled()) return;

        long handle = mc.getWindow().getHandle();
        boolean ctrlDown = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) 
                        || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
        boolean sDown = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_S) || mc.options.keyBack.isPressed();

        if (ctrlDown) {
            if (pauseOnS.isEnabled() && sDown) {
                // When pressing Ctrl + S: STAND COMPLETELY STILL (no backwards, no forwards)
                input.movementForward = 0.0f;
                input.pressingForward = false;
                input.pressingBack = false;
                mc.player.setSprinting(false);
            } else {
                // When pressing Ctrl (and S is NOT pressed): sprint forward!
                input.movementForward = 1.0f;
                input.pressingForward = true;
                input.pressingBack = false;
                mc.player.setSprinting(true);
            }
        }
    }
}