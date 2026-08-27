package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.gui.ClickGui;
import com.nexuspvp.gui.styles.ClassicGuiScreen;
import com.nexuspvp.gui.styles.CompactListScreen;
import com.nexuspvp.gui.styles.GlassDashboardScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == 1) { // GLFW_PRESS
            NexusPVP instance = NexusPVP.getInstance();
            if (instance != null && instance.getModuleManager() != null) {
                if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.currentScreen == null) {
                        ClickGui.openCurrentStyleScreen();
                    } else if (mc.currentScreen instanceof ClickGui ||
                               mc.currentScreen instanceof ClassicGuiScreen ||
                               mc.currentScreen instanceof GlassDashboardScreen ||
                               mc.currentScreen instanceof CompactListScreen) {
                        mc.currentScreen.close();
                    }
                } else {
                    instance.getModuleManager().onKeyPress(key);
                }
            }
        }
    }
}
