package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class OverheadHealth extends Module {

    private final BooleanSetting playersOnly = addSetting(new BooleanSetting("PlayersOnly", false));
    private final BooleanSetting showBar = addSetting(new BooleanSetting("ShowBar", true));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 35.0, 5.0, 60.0, 1.0));

    public OverheadHealth() {
        super("OverheadHealth", "Floating health bar and hearts above entities", Category.RENDER);
        setEnabled(true);
    }

    public void renderOverhead(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light) {
        if (mc.player == null || entity == mc.player || !entity.isAlive() || entity.isInvisible()) return;
        if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) return;
        if (entity.squaredDistanceTo(mc.player) > range.getValue() * range.getValue()) return;

        matrices.push();
        matrices.translate(0.0D, entity.getHeight() + 0.55F, 0.0D);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
        
        // Canonical nameplate scale
        float scale = 0.025F;
        matrices.scale(scale, -scale, scale);

        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float absorb = entity.getAbsorptionAmount();

        int hpColor = health > maxHealth * 0.55f ? 0xFF2ECC71 : (health > maxHealth * 0.25f ? 0xFFF39C12 : 0xFFE74C3C);

        String hpText = String.format("%.1f", health);
        if (absorb > 0) {
            hpText += " +" + String.format("%.1f", absorb);
        }
        hpText += " ❤";

        int textW = mc.textRenderer.getWidth(hpText);
        float textX = -textW / 2.0f;
        float textY = showBar.isEnabled() ? -10.0f : 0.0f;

        // Exact see-through background & foreground
        mc.textRenderer.draw(hpText, textX, textY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x88000000, 0xF000F0);
        mc.textRenderer.draw(hpText, textX, textY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);

        if (showBar.isEnabled()) {
            int barSegments = 22;
            float healthPct = Math.max(0.0f, Math.min(1.0f, health / maxHealth));
            int filled = (int) Math.round(healthPct * barSegments);
            if (filled < 1 && health > 0) filled = 1;

            StringBuilder sbFilled = new StringBuilder();
            for (int i = 0; i < filled; i++) sbFilled.append("█");

            StringBuilder sbEmpty = new StringBuilder();
            for (int i = 0; i < (barSegments - filled); i++) sbEmpty.append("░");

            String filledStr = sbFilled.toString();
            String emptyStr = sbEmpty.toString();
            String fullBar = filledStr + emptyStr;

            int barW = mc.textRenderer.getWidth(fullBar);
            float barX = -barW / 2.0f;
            float barY = 2.0f;

            // 1. Draw entire bar background track with dark outline pill
            mc.textRenderer.draw(fullBar, barX, barY, 0xFF35383E, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x88000000, 0xF000F0);
            mc.textRenderer.draw(fullBar, barX, barY, 0xFF35383E, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);

            // 2. Draw active solid health block on top in glowing color
            if (!filledStr.isEmpty()) {
                mc.textRenderer.draw(filledStr, barX, barY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
                mc.textRenderer.draw(filledStr, barX, barY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            }

            // 3. Draw absorption segment if active
            if (absorb > 0) {
                int absorbSegments = Math.min(barSegments - filled, (int) Math.ceil((absorb / maxHealth) * barSegments));
                if (absorbSegments > 0) {
                    StringBuilder sbAbs = new StringBuilder();
                    for (int i = 0; i < absorbSegments; i++) sbAbs.append("█");
                    String absStr = sbAbs.toString();
                    float absX = barX + mc.textRenderer.getWidth(filledStr);
                    mc.textRenderer.draw(absStr, absX, barY, 0xFFFFD700, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
                    mc.textRenderer.draw(absStr, absX, barY, 0xFFFFD700, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                }
            }
        }

        matrices.pop();
    }
}
