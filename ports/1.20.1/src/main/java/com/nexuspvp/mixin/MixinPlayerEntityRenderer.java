package com.nexuspvp.mixin;
import com.nexuspvp.util.Compat;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.BabyMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;
        BabyMode babyMode = instance.getModuleManager().getModule(BabyMode.class);
        if (babyMode != null && babyMode.isEnabled()) {
            if (!babyMode.isOnlySelf() || player == MinecraftClient.getInstance().player) {
                float scale = babyMode.getScale();
                matrices.scale(scale, scale, scale);
            }
        }
    }
}
