package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;


import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;

public class PotionHUD extends Module {

    private final NumberSetting posX = addSetting(new NumberSetting("PosX", 10, 0, 1000, 5));
    private final NumberSetting posY = addSetting(new NumberSetting("PosY", 120, 0, 800, 5));
    private final BooleanSetting background = addSetting(new BooleanSetting("Background", true));
    private final BooleanSetting durationBar = addSetting(new BooleanSetting("DurationBar", true));
    private final NumberSetting scale = addSetting(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.05));

    private final Map<StatusEffect, Integer> maxDurations = new HashMap<>();

    public PotionHUD() {
        super("PotionHUD", "Displays active potion status effects and duration timers", Category.HUD);
    }

    public NumberSetting getPosX() { return posX; }
    public NumberSetting getPosY() { return posY; }
    public NumberSetting getScale() { return scale; }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null) return;

        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        if (effects.isEmpty()) return;

        int startX = posX.getIntValue();
        int startY = posY.getIntValue();
        float sc = scale.getFloatValue();

        matrices.push();
        matrices.translate(startX, startY, 0);
        matrices.scale(sc, sc, 1.0f);

        int currentY = 0;
        for (StatusEffectInstance effect : effects) {
            StatusEffect type = effect.getEffectType();
            int curDuration = effect.getDuration();

            int maxDur = maxDurations.compute(type, (k, v) -> (v == null || curDuration > v) ? curDuration : v);

            String name = I18n.translate(type.getTranslationKey());
            if (effect.getAmplifier() > 0) {
                name += " " + (effect.getAmplifier() + 1);
            }

            int totalSeconds = curDuration / 20;
            int mins = totalSeconds / 60;
            int secs = totalSeconds % 60;
            String duration = String.format("%d:%02d", mins, secs);
            String fullText = name + " (" + duration + ")";

            int textW = mc.textRenderer.getWidth(fullText);
            int badgeW = textW + 16;
            int badgeH = 16;

            int effectColor = type.getColor() | 0xFF000000;

            if (background.isEnabled()) {
                RenderUtils.drawRoundedRect(matrices, 0, currentY, badgeW, badgeH, 4, 0xDD1E1F22);
                RenderUtils.drawRoundedRect(matrices, 2, currentY + 3, 3, badgeH - 6, 2, effectColor);
            }

            Compat.drawText(matrices, fullText, 8, currentY + 4, effectColor);

            if (durationBar.isEnabled() && maxDur > 0) {
                float pct = Math.max(0.0f, Math.min(1.0f, (float) curDuration / (float) maxDur));
                int barW = (int) ((badgeW - 6) * pct);
                RenderUtils.drawRect(matrices, 3, currentY + badgeH - 2, badgeW - 6, 1, 0xFF2B2D31);
                if (barW > 0) {
                    RenderUtils.drawRect(matrices, 3, currentY + badgeH - 2, barW, 1, effectColor);
                }
            }

            currentY += badgeH + 4;
        }

        matrices.pop();
    }
}
