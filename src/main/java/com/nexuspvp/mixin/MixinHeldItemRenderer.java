package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.SwingAnimations;
import com.nexuspvp.modules.ViewModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", ordinal = 0, shift = At.Shift.AFTER))
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;

        ViewModel viewModel = instance.getModuleManager().getModule(ViewModel.class);
        if (viewModel != null && viewModel.isEnabled()) {
            if (!viewModel.isOnlyMainHand() || hand == Hand.MAIN_HAND) {
                Arm arm = (hand == Hand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
                float armX = (arm == Arm.RIGHT ? 1.0f : -1.0f) * 0.56f;
                float armY = -0.52f;
                float armZ = -0.72f;

                // Move to hand local pivot
                matrices.translate(armX, armY, armZ);
                matrices.translate(viewModel.getTranslateX(), viewModel.getTranslateY(), viewModel.getTranslateZ());
                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(viewModel.getRotateX()));
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(viewModel.getRotateY()));
                matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(viewModel.getRotateZ()));
                matrices.scale(viewModel.getScaleX(), viewModel.getScaleY(), viewModel.getScaleZ());
                matrices.translate(-armX, -armY, -armZ);
            }
        }
    }

    @Inject(method = "applySwingOffset", at = @At("HEAD"), cancellable = true)
    private void onApplySwingOffset(MatrixStack matrices, Arm arm, float swingProgress, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;

        SwingAnimations swingAnim = instance.getModuleManager().getModule(SwingAnimations.class);
        if (swingAnim != null && swingAnim.isEnabled()) {
            float speed = swingAnim.getSpeed();
            float progress = Math.min(1.0f, swingProgress * speed);
            String style = swingAnim.getStyle();
            int i = (arm == Arm.RIGHT) ? 1 : -1;

            float f = MathHelper.sin(progress * progress * (float) Math.PI);
            float f1 = MathHelper.sin(MathHelper.sqrt(progress) * (float) Math.PI);

            if (style.equalsIgnoreCase("1.7")) {
                matrices.translate((float) i * -0.15F * f1, 0.08F * f, 0.05F * f1);
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) i * (45.0F + f * -20.0F)));
                matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float) i * f1 * -20.0F));
                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(f1 * -80.0F));
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) i * -45.0F));
            } else if (style.equalsIgnoreCase("Smooth")) {
                matrices.translate((float) i * -0.1F * f1, 0.04F * f, -0.05F * f1);
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) i * (45.0F + f * -25.0F)));
                matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float) i * f1 * -25.0F));
                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(f1 * -75.0F));
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) i * -45.0F));
            } else if (style.equalsIgnoreCase("Sigma")) {
                matrices.translate((float) i * -0.15F * f1, -0.05F * f, 0.1F * f1);
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) i * (45.0F + f * -30.0F)));
                matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float) i * f1 * -35.0F));
                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(f1 * -80.0F));
            } else if (style.equalsIgnoreCase("Spin")) {
                matrices.translate((float) i * -0.1F * f1, 0.0F, 0.0F);
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) i * progress * 360.0F));
            } else if (style.equalsIgnoreCase("Push")) {
                matrices.translate(0.0F, 0.0F, -0.2F * f1);
                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(f1 * -20.0F));
            } else if (style.equalsIgnoreCase("Down")) {
                matrices.translate(0.0F, -0.15F * f1, 0.0F);
                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(f1 * 30.0F));
            }
            ci.cancel();
        }
    }
}
