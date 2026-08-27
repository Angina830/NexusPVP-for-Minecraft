package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.OverheadHealth;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

    @Inject(method = "render", at = @At("RETURN"))
    private <E extends Entity> void onRenderEntity(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof LivingEntity) {
            NexusPVP instance = NexusPVP.getInstance();
            if (instance != null && instance.getModuleManager() != null) {
                OverheadHealth overhead = instance.getModuleManager().getModule(OverheadHealth.class);
                if (overhead != null && overhead.isEnabled()) {
                    overhead.renderOverhead((LivingEntity) entity, matrices, vertexConsumers, tickDelta, light);
                }
            }
        }
    }
}
