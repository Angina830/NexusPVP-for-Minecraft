package com.nexuspvp.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {
    // Left completely clean to preserve mob model shaders and textures
}
