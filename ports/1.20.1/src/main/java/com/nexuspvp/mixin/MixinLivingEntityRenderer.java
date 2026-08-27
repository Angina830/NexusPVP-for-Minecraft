package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.HitColor;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

    @Inject(method = "getOverlay", at = @At("HEAD"), cancellable = true)
    private static void onGetOverlay(LivingEntity entity, float whiteOverlayProgress, CallbackInfoReturnable<Integer> cir) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            HitColor hitColor = instance.getModuleManager().getModule(HitColor.class);
            if (hitColor != null && hitColor.isEnabled() && (hitColor.isEntityHit(entity.getId()) || entity.hurtTime > 0)) {
                cir.setReturnValue(OverlayTexture.DEFAULT_UV);
            }
        }
    }
}
