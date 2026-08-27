package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.ShulkerPreview;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen {

    @Shadow protected Slot focusedSlot;

    @Inject(method = "drawMouseoverTooltip", at = @At("RETURN"))
    private void onDrawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;
        ShulkerPreview sp = instance.getModuleManager().getModule(ShulkerPreview.class);
        if (sp != null && sp.isEnabled() && focusedSlot != null && focusedSlot.hasStack()) {
            ItemStack stack = focusedSlot.getStack();
            if (ShulkerPreview.isShulkerBox(stack.getItem())) {
                sp.renderShulkerPreview(context, stack, x, y);
            }
        }
    }
}
