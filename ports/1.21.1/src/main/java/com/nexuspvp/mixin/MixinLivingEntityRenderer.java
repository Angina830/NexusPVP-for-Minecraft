package com.nexuspvp.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {
    // Pure, untampered vanilla entity rendering to preserve all mob models and textures
}
