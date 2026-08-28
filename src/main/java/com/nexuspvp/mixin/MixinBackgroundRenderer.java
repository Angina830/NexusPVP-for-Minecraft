package com.nexuspvp.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.ClearWater;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

    @Inject(method = "applyFog", at = @At("TAIL"))
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            ClearWater clearWater = instance.getModuleManager().getModule(ClearWater.class);
            if (clearWater != null && clearWater.isEnabled()) {
                FluidState fluidState = camera.getSubmergedFluidState();
                if (fluidState.isIn(FluidTags.WATER)) {
                    RenderSystem.fogStart(0.0F);
                    RenderSystem.fogEnd(clearWater.getFogDistance());
                }
            }
        }
    }
}