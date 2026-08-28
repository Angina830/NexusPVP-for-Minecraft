package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class HitSounds extends Module {

    private final ModeSetting sound = addSetting(new ModeSetting("Sound", "Bell", "Bell", "Ding", "Skeet", "Pop", "Crit", "Anvil", "Orb"));
    private final NumberSetting volume = addSetting(new NumberSetting("Volume", 0.8, 0.1, 1.5, 0.05));
    private final NumberSetting pitch = addSetting(new NumberSetting("Pitch", 1.2, 0.5, 2.0, 0.05));
    private final BooleanSetting comboPitch = addSetting(new BooleanSetting("ComboPitch", true));

    private int comboCount = 0;
    private long lastHitTime = 0;

    public HitSounds() {
        super("HitSounds", "Plays custom audio feedback on hitting entities", Category.PVP);
    }

    public void playHitSound() {
        if (!isEnabled() || mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastHitTime < 2200) {
            comboCount++;
        } else {
            comboCount = 1;
        }
        lastHitTime = now;

        SoundEvent event;
        String mode = sound.getValue();
        float p = pitch.getFloatValue();

        switch (mode) {
            case "Ding":
            case "Bell":
                event = SoundEvents.BLOCK_NOTE_BLOCK_BELL;
                break;
            case "Skeet":
                event = SoundEvents.UI_BUTTON_CLICK;
                p = 1.8f;
                break;
            case "Pop":
                event = SoundEvents.ENTITY_ITEM_PICKUP;
                break;
            case "Crit":
                event = SoundEvents.ENTITY_PLAYER_ATTACK_CRIT;
                break;
            case "Anvil":
                event = SoundEvents.BLOCK_ANVIL_LAND;
                p = 1.5f;
                break;
            case "Orb":
            default:
                event = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
                break;
        }

        if (comboPitch.isEnabled()) {
            p += Math.min(6, comboCount - 1) * 0.08f;
        }

        mc.getSoundManager().play(PositionedSoundInstance.master(event, Math.min(2.0f, p), volume.getFloatValue()));
    }
}
