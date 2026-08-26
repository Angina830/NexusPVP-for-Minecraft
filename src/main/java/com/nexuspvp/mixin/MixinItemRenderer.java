package com.nexuspvp.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.ItemCooldowns;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow public float zOffset;

    @Shadow protected abstract void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha);

    @Redirect(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiQuad(Lnet/minecraft/client/render/BufferBuilder;IIIIIIII)V"))
    private void redirectVanillaCooldownQuad(ItemRenderer itemRenderer, BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            ItemCooldowns cooldowns = instance.getModuleManager().getModule(ItemCooldowns.class);
            if (cooldowns != null && cooldowns.isEnabled() && alpha == 127) {
                // Cancel only vanilla cooldown quad (alpha 127) and restore OpenGL texture/depth state
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
                return;
            }
        }
        this.renderGuiQuad(buffer, x, y, width, height, red, green, blue, alpha);
    }

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void onRenderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        if (stack.isEmpty()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;

        float progress = player.getItemCooldownManager().getCooldownProgress(stack.getItem(), mc.getTickDelta());
        if (progress > 0.0f) {
            NexusPVP instance = NexusPVP.getInstance();
            if (instance != null && instance.getModuleManager() != null) {
                ItemCooldowns cooldowns = instance.getModuleManager().getModule(ItemCooldowns.class);
                if (cooldowns != null && cooldowns.isEnabled()) {
                    MatrixStack matrices = new MatrixStack();
                    matrices.push();
                    matrices.translate(0, 0, this.zOffset + 250.0f);
                    cooldowns.renderCooldownOverlay(matrices, x, y, progress, stack.getItem());
                    matrices.pop();
                }
            }
        }
    }
}
