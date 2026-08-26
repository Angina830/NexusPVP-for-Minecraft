package com.nexuspvp.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public class MixinClientPlayerEntity {
    // Cleaned up unused jump hooks
}
