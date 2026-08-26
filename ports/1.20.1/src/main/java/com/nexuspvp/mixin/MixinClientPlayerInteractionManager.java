package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.Crosshair;
import com.nexuspvp.modules.DamageIndicator;
import com.nexuspvp.modules.HitSounds;
import com.nexuspvp.modules.TargetHUD;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (target != null) {
            DamageIndicator.recordAttack(target.getEntityId());
            Crosshair.recordHit();

            NexusPVP instance = NexusPVP.getInstance();
            if (instance != null && instance.getModuleManager() != null) {
                HitSounds hs = instance.getModuleManager().getModule(HitSounds.class);
                if (hs != null) hs.playHitSound();

                if (target instanceof LivingEntity) {
                    TargetHUD th = instance.getModuleManager().getModule(TargetHUD.class);
                    if (th != null) {
                        th.setTarget((LivingEntity) target);
                    }
                }
            }
        }
    }
}