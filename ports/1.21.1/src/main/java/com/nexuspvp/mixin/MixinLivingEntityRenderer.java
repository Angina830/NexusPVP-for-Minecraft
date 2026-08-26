package com.nexuspvp.mixin;
import com.nexuspvp.util.Compat;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.HitColor;
import com.nexuspvp.modules.OverheadHealth;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {

    private LivingEntity currentRenderEntity;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        this.currentRenderEntity = entity;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderReturn(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            OverheadHealth overhead = instance.getModuleManager().getModule(OverheadHealth.class);
            if (overhead != null && overhead.isEnabled()) {
                overhead.renderOverhead(entity, matrices, tickDelta);
            }
        }
    }

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

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void redirectModelRender(EntityModel<T> model, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null && currentRenderEntity != null) {
            HitColor hitColor = instance.getModuleManager().getModule(HitColor.class);
            if (hitColor != null && hitColor.isEnabled() && (hitColor.isEntityHit(currentRenderEntity.getId()) || currentRenderEntity.hurtTime > 0)) {
                Color c = hitColor.getColor();
                float hitAlpha = hitColor.getHitAlpha(currentRenderEntity.getId());
                if (hitAlpha <= 0) hitAlpha = hitColor.getOpacity() / 255.0f;
                
                float targetR = c.getRed() / 255.0f;
                float targetG = c.getGreen() / 255.0f;
                float targetB = c.getBlue() / 255.0f;

                float blendedR = red * (1.0f - hitAlpha) + targetR * hitAlpha;
                float blendedG = green * (1.0f - hitAlpha) + targetG * hitAlpha;
                float blendedB = blue * (1.0f - hitAlpha) + targetB * hitAlpha;
                
                model.render(matrices, vertices, light, overlay, 0xFFFFFFFF);
                return;
            }
        }
        model.render(matrices, vertices, light, overlay, 0xFFFFFFFF);
    }
}