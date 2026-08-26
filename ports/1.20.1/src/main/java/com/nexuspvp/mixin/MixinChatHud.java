package com.nexuspvp.mixin;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.ChatTweaks;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public class MixinChatHud {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), argsOnly = true)
    private Text onAddMessage(Text message) {
        NexusPVP instance = NexusPVP.getInstance();
        if (instance != null && instance.getModuleManager() != null) {
            ChatTweaks chatTweaks = instance.getModuleManager().getModule(ChatTweaks.class);
            if (chatTweaks != null && chatTweaks.isEnabled()) {
                return chatTweaks.modifyChatMessage(message);
            }
        }
        return message;
    }
}
