package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.HitColor;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(OverlayTexture.class)
public class MixinOverlayTexture {
    @Shadow private NativeImageBackedTexture texture;

    @Inject(method = "setupOverlayColor", at = @At("HEAD"))
    private void onSetupOverlayColor(CallbackInfo ci) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            HitColor hc = instance.getModuleManager().getModule(HitColor.class);
            if (hc != null && hc.isEnabled()) {
                NativeImage image = this.texture.getImage();
                if (image != null) {
                    Color c = hc.getColor();
                    // ABGR format for NativeImage
                    int r = c.getRed();
                    int g = c.getGreen();
                    int b = c.getBlue();
                    int a = c.getAlpha(); // use alpha from setting!
                    
                    int color = (a << 24) | (b << 16) | (g << 8) | r;
                    
                    // The damage overlay is at V=3 in 1.16.5, let's just write all V rows to be safe, or V=3.
                    // Vanilla does: for i=0..15 { image.setColor(i, 3, red_tint); }
                    for (int i = 0; i < 16; i++) {
                        image.setPixelColor(i, 3, color);
                    }
                    this.texture.upload();
                    return;
                }
            }
        }
        
        // Restore vanilla if disabled
        NativeImage image = this.texture.getImage();
        if (image != null) {
            int vanillaColor = (170 << 24) | (0 << 16) | (0 << 8) | 255; // ABGR for red (alpha 170, blue 0, green 0, red 255) in vanilla
            for (int i = 0; i < 16; i++) {
                // To be safe, check if it's already vanilla to avoid uploading every frame
                if (image.getPixelColor(i, 3) != vanillaColor) {
                    image.setPixelColor(i, 3, vanillaColor);
                }
            }
            this.texture.upload();
        }
    }
}