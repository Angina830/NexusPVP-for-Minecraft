package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.MotionBlur;
import com.nexuspvp.modules.NoHurtCam;
import com.nexuspvp.modules.TotemPop;
import com.nexuspvp.modules.Zoom;
import net.minecraft.client.gl.PostProcessShader;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow private ShaderEffect shader;
    @Shadow private boolean shadersEnabled;
    @Shadow protected abstract void loadShader(Identifier id);
    @Shadow public abstract void disableShader();

    private static final Identifier PHOSPHOR_SHADER = new Identifier("shaders/post/phosphor.json");

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            MotionBlur mb = instance.getModuleManager().getModule(MotionBlur.class);
            if (mb != null) {
                if (mb.isEnabled()) {
                    if (this.shader == null) {
                        try {
                            this.loadShader(PHOSPHOR_SHADER);
                        } catch (Exception ignored) {}
                    }
                    if (this.shader != null) {
                        try {
                            float factor = 0.40f + (mb.getStrength() / 10.0f) * 0.55f;
                            for (PostProcessShader pass : ((MixinShaderEffect) (Object) this.shader).getPasses()) {
                                if (pass.getProgram() != null && pass.getProgram().getUniformByName("Phosphor") != null) {
                                    pass.getProgram().getUniformByName("Phosphor").set(factor, factor, factor);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                } else if (this.shader != null && this.shader.getName().contains("phosphor")) {
                    this.disableShader();
                }
            }
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;
        Zoom zoom = instance.getModuleManager().getModule(Zoom.class);
        if (zoom != null && zoom.isEnabled() && zoom.isZooming()) {
            cir.setReturnValue(cir.getReturnValue() / zoom.getFactor());
        }
    }

    @Inject(method = "bobViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onBobViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;
        NoHurtCam noHurtCam = instance.getModuleManager().getModule(NoHurtCam.class);
        if (noHurtCam != null && noHurtCam.isEnabled()) {
            float strength = noHurtCam.getStrength();
            if (strength <= 0.0f) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onRenderFloatingItem(int scaledWidth, int scaledHeight, float tickDelta, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;
        TotemPop totemPop = instance.getModuleManager().getModule(TotemPop.class);
        if (totemPop != null && totemPop.isCleanTotem()) {
            ci.cancel();
        }
    }
}
