package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.ShulkerPreview;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public class MixinDrawContext {

    @Inject(method = "drawItemTooltip", at = @At("RETURN"))
    private void onDrawItemTooltip(TextRenderer textRenderer, ItemStack stack, int x, int y, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            ShulkerPreview sp = instance.getModuleManager().getModule(ShulkerPreview.class);
            if (sp != null && sp.isEnabled()) {
                sp.renderShulkerPreview((DrawContext) (Object) this, stack, x, y);
            }
        }
    }
}
