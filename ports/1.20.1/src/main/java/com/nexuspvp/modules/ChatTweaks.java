package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatTweaks extends Module {

    private final BooleanSetting timestamps = addSetting(new BooleanSetting("Timestamps", true));

    public ChatTweaks() {
        super("ChatTweaks", "Adds timestamps and enhancements to chat messages", Category.HUD);
    }

    public Text formatMessage(Text message) {
        if (!isEnabled()) return message;

        if (timestamps.isEnabled()) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timeStr = "[" + sdf.format(new Date()) + "] ";
            LiteralText timePrefix = Text.literal(timeStr);
            timePrefix.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
            return timePrefix.append(message);
        }
        return message;
    }
}