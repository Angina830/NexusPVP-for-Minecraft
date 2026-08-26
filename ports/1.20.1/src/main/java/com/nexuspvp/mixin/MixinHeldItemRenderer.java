package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.SwingAnimations;
import com.nexuspvp.modules.ViewModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    private void onRenderItemAfterPush(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;

        ViewModel viewModel = instance.getModuleManager().getModule(ViewModel.class);
        if (viewModel != null && viewModel.isEnabled()) {
            if (!viewModel.isOnlyMainHand() || hand == Hand.MAIN_HAND) {
                matrices.translate(viewModel.getTranslateX(), viewModel.getTranslateY(), viewModel.getTranslateZ());
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(viewModel.getRotateX()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(viewModel.getRotateY()));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(viewModel.getRotateZ()));
                matrices.scale(viewModel.getScaleX(), viewModel.getScaleY(), viewModel.getScaleZ());
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
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + f * -20.0F)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * f1 * -20.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -80.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
            } else if (style.equalsIgnoreCase("Smooth")) {
                matrices.translate((float) i * -0.1F * f1, 0.04F * f, -0.05F * f1);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + f * -25.0F)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * f1 * -25.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -75.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
            } else if (style.equalsIgnoreCase("Sigma")) {
                matrices.translate((float) i * -0.15F * f1, -0.05F * f, 0.1F * f1);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + f * -30.0F)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * f1 * -35.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -65.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
            } else if (style.equalsIgnoreCase("Spin")) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * 45.0F));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * progress * 360.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -50.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
            } else if (style.equalsIgnoreCase("Push")) {
                matrices.translate((float) i * -0.05F * f, 0.0F, -0.3F * f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + f * -15.0F)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * f1 * -10.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -50.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
            } else if (style.equalsIgnoreCase("Down")) {
                matrices.translate(0.0F, -0.18F * f1, 0.0F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * 45.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -90.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
            }
            ci.cancel();
        }
    }
}
