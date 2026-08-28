package com.nexuspvp.mixin;
import com.nexuspvp.util.Compat;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.Ambience;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
    @Shadow @Final private NativeImageBackedTexture texture;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void onUpdate(float delta, CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance == null || instance.getModuleManager() == null) return;
        Ambience ambience = instance.getModuleManager().getModule(Ambience.class);
        
        if (ambience != null && ambience.isEnabled() && ambience.isFullbright()) {
            NativeImage image = this.texture.getImage();
            if (image != null) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        image.setColor(x, y, 0xFFFFFFFF);
                    }
                }
                this.texture.upload();
            }
            ci.cancel();
        }
    }
}
