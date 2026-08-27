package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import net.minecraft.text.Text;

public class ChatTweaks extends Module {

    private final BooleanSetting timestamps = new BooleanSetting("Timestamps", true);
    private final BooleanSetting infiniteChat = new BooleanSetting("InfiniteChat", true);

    public ChatTweaks() {
        super("ChatTweaks", "Adds timestamps and infinite chat history", Category.MISC, 0);
        addSetting(timestamps);
        addSetting(infiniteChat);
    }

    public boolean isInfiniteChat() {
        return isEnabled() && infiniteChat.isEnabled();
    }

    public Text modifyChatMessage(Text message) {
        return message;
    }
}
