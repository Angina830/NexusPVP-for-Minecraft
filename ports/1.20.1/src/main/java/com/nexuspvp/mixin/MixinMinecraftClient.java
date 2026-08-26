package com.nexuspvp.mixin;
import com.nexuspvp.util.Compat;


import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void onInput(CallbackInfo ci) {
        // Module key handling is done via ClientTickEvents in NexusPVP.java
    }
}
