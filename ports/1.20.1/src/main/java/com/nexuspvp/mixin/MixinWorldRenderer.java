package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.BlockOutline;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            BlockOutline bo = instance.getModuleManager().getModule(BlockOutline.class);
            if (bo != null && bo.isEnabled()) {
                ci.cancel();
            }
        }
    }
}
