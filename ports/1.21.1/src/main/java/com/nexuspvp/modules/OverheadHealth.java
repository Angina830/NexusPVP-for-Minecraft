package com.nexuspvp.modules;

import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class OverheadHealth extends Module {

    private final BooleanSetting playersOnly = addSetting(new BooleanSetting("PlayersOnly", false));
    private final BooleanSetting showName = addSetting(new BooleanSetting("ShowName", true));
    private final BooleanSetting ghostDamage = addSetting(new BooleanSetting("GhostDamage", true));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 35.0, 5.0, 60.0, 1.0));

    private static class HealthTracker {
        float currentHp;
        float ghostHp;
        float previousHp;
        long lastHitTime;
    }

    private final Map<Integer, HealthTracker> trackers = new HashMap<>();

    public OverheadHealth() {
        super("OverheadHealth", "TargetHUD mini health card with Dota-style white ghost damage trail", Category.RENDER);
        setEnabled(true);
    }

    public void renderOverhead(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light) {
        if (mc.player == null || entity == mc.player || !entity.isAlive() || entity.isInvisible()) return;
        if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) return;
        if (entity.squaredDistanceTo(mc.player) > range.getValue() * range.getValue()) return;

        // Block Occlusion: Do NOT render if occluded behind solid blocks/walls
        if (!mc.player.canSee(entity)) return;

        matrices.push();
        matrices.translate(0.0D, entity.getHeight() + 0.55F, 0.0D);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());

        float scale = 0.020F;
        matrices.scale(scale, -scale, scale);

        float actualHp = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float absorb = entity.getAbsorptionAmount();

        // Track Dota-style ghost damage
        HealthTracker tracker = trackers.computeIfAbsent(entity.getId(), id -> {
            HealthTracker t = new HealthTracker();
            t.currentHp = actualHp;
            t.ghostHp = actualHp;
            t.previousHp = actualHp;
            t.lastHitTime = 0;
            return t;
        });

        if (actualHp < tracker.previousHp) {
            tracker.ghostHp = tracker.previousHp; // Ghost stays at old HP
            tracker.lastHitTime = System.currentTimeMillis();
        }
        tracker.previousHp = actualHp;
        tracker.currentHp = MathHelper.lerp(0.22f, tracker.currentHp, actualHp);

        // Delay then smooth drain
        if (System.currentTimeMillis() - tracker.lastHitTime > 300) {
            tracker.ghostHp = MathHelper.lerp(0.09f, tracker.ghostHp, actualHp);
        }
        if (actualHp > tracker.ghostHp) {
            tracker.ghostHp = actualHp;
        }

        float healthPct = MathHelper.clamp(tracker.currentHp / maxHealth, 0.0f, 1.0f);
        float ghostPct = MathHelper.clamp(tracker.ghostHp / maxHealth, 0.0f, 1.0f);
        int hpColor = healthPct > 0.55f ? 0xFF23A55A : (healthPct > 0.25f ? 0xFFFEE75C : 0xFFED4245);

        String entityName = entity.getName().getString();
        if (entityName.length() > 14) entityName = entityName.substring(0, 12) + "..";

        String hpText = String.format("%.1f", actualHp);
        if (absorb > 0) {
            hpText += " (+" + String.format("%.1f", absorb) + ")";
        }
        hpText += " ❤";

        int barLength = 18;
        int activeFilled = (int) Math.round(healthPct * barLength);
        if (activeFilled < 1 && actualHp > 0) activeFilled = 1;

        int ghostFilled = (int) Math.round(ghostPct * barLength);
        if (ghostFilled < activeFilled) ghostFilled = activeFilled;

        StringBuilder sbActive = new StringBuilder();
        for (int i = 0; i < activeFilled; i++) sbActive.append("█");

        StringBuilder sbGhost = new StringBuilder();
        for (int i = 0; i < (ghostFilled - activeFilled); i++) sbGhost.append("█");

        StringBuilder sbEmpty = new StringBuilder();
        for (int i = 0; i < (barLength - ghostFilled); i++) sbEmpty.append("░");

        String activeStr = sbActive.toString();
        String ghostStr = sbGhost.toString();
        String emptyStr = sbEmpty.toString();
        String fullBar = activeStr + ghostStr + emptyStr;

        int nameW = mc.textRenderer.getWidth(entityName);
        int hpW = mc.textRenderer.getWidth(hpText);
        int barW = mc.textRenderer.getWidth(fullBar);
        int cardW = Math.max(barW, Math.max(nameW, hpW)) + 16;
        int cardH = showName.isEnabled() ? 26 : 18;

        float cardX = -cardW / 2.0f;
        float cardY = -cardH / 2.0f;

        int bgColor = 0xDD1E1F22;

        if (showName.isEnabled()) {
            float nameX = cardX + 8;
            float nameY = cardY + 2;
            mc.textRenderer.draw(entityName, nameX, nameY, 0xFFF2F3F5, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, bgColor, light);

            float hpTextX = cardX + cardW - hpW - 6;
            mc.textRenderer.draw(hpText, hpTextX, nameY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

            float bX = cardX + (cardW - barW) / 2.0f;
            float bY = cardY + 14;

            // 1. Dark background track
            mc.textRenderer.draw(fullBar, bX, bY, 0xFF35383E, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, bgColor, light);

            // 2. Dota-style White Ghost Damage Bar
            if (ghostDamage.isEnabled() && !ghostStr.isEmpty()) {
                float ghostX = bX + mc.textRenderer.getWidth(activeStr);
                mc.textRenderer.draw(ghostStr, ghostX, bY, 0xFFFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            }

            // 3. Active Health Bar
            if (!activeStr.isEmpty()) {
                mc.textRenderer.draw(activeStr, bX, bY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            }

            // 4. Absorption segment
            if (absorb > 0) {
                int absorbSegments = Math.min(barLength - activeFilled, (int) Math.ceil((absorb / maxHealth) * barLength));
                if (absorbSegments > 0) {
                    StringBuilder sbAbs = new StringBuilder();
                    for (int i = 0; i < absorbSegments; i++) sbAbs.append("█");
                    float absX = bX + mc.textRenderer.getWidth(activeStr);
                    mc.textRenderer.draw(sbAbs.toString(), absX, bY, 0xFFFFD700, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
                }
            }
        } else {
            float hpTextX = -hpW / 2.0f;
            float hpTextY = cardY + 1;
            mc.textRenderer.draw(hpText, hpTextX, hpTextY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, bgColor, light);

            float bX = cardX + (cardW - barW) / 2.0f;
            float bY = cardY + 10;

            mc.textRenderer.draw(fullBar, bX, bY, 0xFF35383E, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, bgColor, light);

            if (ghostDamage.isEnabled() && !ghostStr.isEmpty()) {
                float ghostX = bX + mc.textRenderer.getWidth(activeStr);
                mc.textRenderer.draw(ghostStr, ghostX, bY, 0xFFFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            }

            if (!activeStr.isEmpty()) {
                mc.textRenderer.draw(activeStr, bX, bY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            }
        }

        matrices.pop();
    }
}
