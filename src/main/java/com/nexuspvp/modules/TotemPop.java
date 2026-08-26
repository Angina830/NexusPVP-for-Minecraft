package com.nexuspvp.modules;

import com.nexuspvp.gui.LanguageManager;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TotemPop extends Module {

    private final BooleanSetting onScreen = addSetting(new BooleanSetting("OnScreen", true));
    private final BooleanSetting cleanTotem = addSetting(new BooleanSetting("CleanTotem", true));
    private final BooleanSetting playSound = addSetting(new BooleanSetting("PlaySound", true));
    private final ModeSetting sound = addSetting(new ModeSetting("Sound", "Ding", "Ding", "Bell", "Orb", "Anvil", "Crit"));
    private final NumberSetting volume = addSetting(new NumberSetting("Volume", 1.0, 0.1, 2.0, 0.1));
    private final NumberSetting posX = addSetting(new NumberSetting("PosX", 10, 0, 1920, 5));
    private final NumberSetting posY = addSetting(new NumberSetting("PosY", 60, 0, 1080, 5));

    private static TotemPop instance;
    private final Map<String, Integer> popCounts = new ConcurrentHashMap<>();
    private final List<PopNotification> notifications = new ArrayList<>();

    public TotemPop() {
        super("TotemPop", "Tracks totem pops with clean HUD alerts", Category.PVP);
        instance = this;
    }

    public static TotemPop getInstance() {
        return instance;
    }

    public boolean isCleanTotem() {
        return isEnabled() && cleanTotem.isEnabled();
    }

    public NumberSetting getPosX() { return posX; }
    public NumberSetting getPosY() { return posY; }

    public static void onPop(Entity entity) {
        if (instance == null || !instance.isEnabled() || entity == null) return;
        instance.handlePop(entity);
    }

    public void handlePop(Entity entity) {
        String name = entity.getName().asString();
        int count = popCounts.getOrDefault(name, 0) + 1;
        popCounts.put(name, count);

        notifications.add(new PopNotification(name, count));

        if (playSound.isEnabled() && mc.player != null) {
            float vol = volume.getFloatValue();
            switch (sound.getValue()) {
                case "Bell":
                    mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BELL_USE, 1.2f, vol));
                    break;
                case "Ding":
                    mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, vol));
                    break;
                case "Orb":
                    mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.5f, vol));
                    break;
                case "Anvil":
                    mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_LAND, 1.8f, vol));
                    break;
                case "Crit":
                    mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, vol));
                    break;
            }
        }
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();
        notifications.removeIf(n -> now - n.startTime > 3000);
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (!onScreen.isEnabled() || mc.player == null || notifications.isEmpty()) return;

        int x = posX.getIntValue();
        int y = posY.getIntValue();
        int accent = ThemeManager.getInstance().getAccentColor().getRGB();
        long now = System.currentTimeMillis();

        int curY = y;
        for (PopNotification n : notifications) {
            long age = now - n.startTime;
            float progress = Math.min(1.0f, age / 3000.0f);
            float alphaProgress = progress > 0.8f ? (1.0f - progress) / 0.2f : 1.0f;
            int alpha = (int) (alphaProgress * 255);
            if (alpha <= 5) continue;

            String text = LanguageManager.getInstance().isRussian() ?
                "[!] " + n.name + " -1 \u0442\u043E\u0442\u0435\u043C (x" + n.count + ")" :
                "[!] " + n.name + " popped totem (x" + n.count + ")";

            int textW = mc.textRenderer.getWidth(text);
            int badgeW = textW + 16;
            int badgeH = 18;

            int bg = (alpha / 2 << 24) | 0x1E1F22;
            int border = (alpha << 24) | (accent & 0x00FFFFFF);

            // Pop-in slide animation
            float slide = Math.min(1.0f, age / 150.0f);
            int drawX = (int) (x - (1.0f - slide) * 30);

            RenderUtils.drawRoundedRect(matrices, drawX - 1, curY - 1, badgeW + 2, badgeH + 2, 4, border);
            RenderUtils.drawRoundedRect(matrices, drawX, curY, badgeW, badgeH, 3, bg);

            mc.textRenderer.drawWithShadow(matrices, text, drawX + 8, curY + 5, (alpha << 24) | 0xFFFFFF);

            curY += badgeH + 4;
        }
    }

    private static class PopNotification {
        String name;
        int count;
        long startTime;

        PopNotification(String name, int count) {
            this.name = name;
            this.count = count;
            this.startTime = System.currentTimeMillis();
        }
    }
}