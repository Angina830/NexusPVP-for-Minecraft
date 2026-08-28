package com.nexuspvp.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.ClearWater;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

    @Inject(method = "applyFog", at = @At("TAIL"))
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;

        ClearWater clearWater = instance.getModuleManager().getModule(ClearWater.class);
        if (clearWater != null && clearWater.isEnabled()) {
            RenderSystem.setShaderFogStart(0.0F);
            RenderSystem.setShaderFogEnd(clearWater.getFogDistance());
        }
    }
}
