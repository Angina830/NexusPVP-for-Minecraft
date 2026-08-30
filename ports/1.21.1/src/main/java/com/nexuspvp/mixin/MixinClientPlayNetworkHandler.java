package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.StunVisuals;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "onParticle", at = @At("HEAD"), cancellable = true)
    private void onParticlePacket(ParticleS2CPacket packet, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            StunVisuals stun = instance.getModuleManager().getModule(StunVisuals.class);
            if (stun != null && stun.isEnabled()) {
                if (stun.handleParticle(packet.getParameters(), packet.getX(), packet.getY(), packet.getZ())) {
                    if (stun.isHideParticles()) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
