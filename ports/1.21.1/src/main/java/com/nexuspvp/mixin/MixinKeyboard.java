package com.nexuspvp.mixin;
import com.nexuspvp.util.Compat;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.gui.ClickGui;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.currentScreen == null) {
                    ClickGui.openCurrentStyleScreen();
                }
            }
            if (NexusPVP.getInstance() != null && NexusPVP.getInstance().getModuleManager() != null) {
                NexusPVP.getInstance().getModuleManager().onKeyPressed(key);
            }
        }
    }
}
