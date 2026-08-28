package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.NoSlowFOV;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity {

    @Inject(method = "getFovMultiplier", at = @At("RETURN"), cancellable = true)
    private void onGetFovMultiplier(CallbackInfoReturnable<Float> cir) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;

        NoSlowFOV noSlowFOV = instance.getModuleManager().getModule(NoSlowFOV.class);
        if (noSlowFOV != null && noSlowFOV.isEnabled()) {
            float fov = cir.getReturnValue();
            if (noSlowFOV.isStaticFov()) {
                cir.setReturnValue(1.0f);
            } else if (noSlowFOV.isOnlySlowness()) {
                if (fov < 1.0f) {
                    cir.setReturnValue(1.0f);
                }
            }
        }
    }
}
