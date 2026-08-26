package com.nexuspvp.mixin;
import com.nexuspvp.util.Compat;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.ShulkerPreview;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen {

    @Inject(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true)
    private void onRenderTooltip(MatrixStack matrices, ItemStack stack, int x, int y, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            ShulkerPreview preview = instance.getModuleManager().getModule(ShulkerPreview.class);
            if (preview != null && preview.isEnabled() && preview.isShulkerBox(stack)) {
                if (preview.renderShulkerPreview(matrices, stack, x, y)) {
                    ci.cancel();
                }
            }
        }
    }
}