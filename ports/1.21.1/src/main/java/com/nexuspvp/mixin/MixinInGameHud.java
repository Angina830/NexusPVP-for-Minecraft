package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.Crosshair;
import com.nexuspvp.modules.ItemCooldowns;
import com.nexuspvp.util.Compat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            Compat.setContext(context);
            instance.getModuleManager().onRender2D(context.getMatrices(), tickCounter.getTickDelta(false));
            ItemCooldowns cd = instance.getModuleManager().getModule(ItemCooldowns.class);
            if (cd != null && cd.isEnabled()) {
                cd.renderHotbarCooldowns(context);
            }
            Compat.setContext(null);
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            Crosshair crosshair = instance.getModuleManager().getModule(Crosshair.class);
            if (crosshair != null && crosshair.isEnabled()) {
                ci.cancel();
                crosshair.renderCustomCrosshair(context);
            }
        }
    }
}
