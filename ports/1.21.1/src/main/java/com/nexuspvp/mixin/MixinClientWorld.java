package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.StunVisuals;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void onAddParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            StunVisuals stun = instance.getModuleManager().getModule(StunVisuals.class);
            if (stun != null && stun.isEnabled()) {
                if (stun.handleParticle(parameters, x, y, z)) {
                    if (stun.isHideParticles()) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;ZDDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void onAddParticleAlways(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            StunVisuals stun = instance.getModuleManager().getModule(StunVisuals.class);
            if (stun != null && stun.isEnabled()) {
                if (stun.handleParticle(parameters, x, y, z)) {
                    if (stun.isHideParticles()) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
