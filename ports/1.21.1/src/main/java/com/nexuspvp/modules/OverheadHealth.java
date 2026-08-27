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

public class OverheadHealth extends Module {

    private final BooleanSetting playersOnly = addSetting(new BooleanSetting("PlayersOnly", false));
    private final BooleanSetting showName = addSetting(new BooleanSetting("ShowName", true));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 35.0, 5.0, 60.0, 1.0));

    public OverheadHealth() {
        super("OverheadHealth", "TargetHUD-styled mini health card above entities with block occlusion", Category.RENDER);
        setEnabled(true);
    }

    public void renderOverhead(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light) {
        if (mc.player == null || entity == mc.player || !entity.isAlive() || entity.isInvisible()) return;
        if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) return;
        if (entity.squaredDistanceTo(mc.player) > range.getValue() * range.getValue()) return;

        // Block Occlusion: Do NOT render if occluded behind solid blocks/walls!
        if (!mc.player.canSee(entity)) return;

        matrices.push();
        matrices.translate(0.0D, entity.getHeight() + 0.55F, 0.0D);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());

        // Standard canonical nameplate matrix scale (positive X, negative Y)
        float scale = 0.020F;
        matrices.scale(scale, -scale, scale);

        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float absorb = entity.getAbsorptionAmount();

        float healthPct = MathHelper.clamp(health / maxHealth, 0.0f, 1.0f);
        int hpColor = healthPct > 0.55f ? 0xFF23A55A : (healthPct > 0.25f ? 0xFFFEE75C : 0xFFED4245);

        String entityName = entity.getName().getString();
        if (entityName.length() > 14) entityName = entityName.substring(0, 12) + "..";

        String hpText = String.format("%.1f", health);
        if (absorb > 0) {
            hpText += " (+" + String.format("%.1f", absorb) + ")";
        }
        hpText += " ❤";

        // Card Dimensions & Layout
        int barLength = 18;
        int filled = (int) Math.round(healthPct * barLength);
        if (filled < 1 && health > 0) filled = 1;

        StringBuilder sbFilled = new StringBuilder();
        for (int i = 0; i < filled; i++) sbFilled.append("█");

        StringBuilder sbEmpty = new StringBuilder();
        for (int i = 0; i < (barLength - filled); i++) sbEmpty.append("░");

        String filledStr = sbFilled.toString();
        String emptyStr = sbEmpty.toString();
        String fullBar = filledStr + emptyStr;

        int nameW = mc.textRenderer.getWidth(entityName);
        int hpW = mc.textRenderer.getWidth(hpText);
        int barW = mc.textRenderer.getWidth(fullBar);
        int cardW = Math.max(barW, Math.max(nameW, hpW)) + 16;
        int cardH = showName.isEnabled() ? 26 : 18;

        float cardX = -cardW / 2.0f;
        float cardY = -cardH / 2.0f;

        // Render Background Card (Dark Discord/TargetHUD container, NORMAL layer = depth tested!)
        int bgColor = 0xDD1E1F22;
        int accent = ThemeManager.getInstance().getAccentColor().getRGB() | 0xFF000000;

        // 1. Header Name (if enabled)
        if (showName.isEnabled()) {
            float nameX = cardX + 8;
            float nameY = cardY + 2;
            mc.textRenderer.draw(entityName, nameX, nameY, 0xFFF2F3F5, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, bgColor, light);

            float hpTextX = cardX + cardW - hpW - 6;
            mc.textRenderer.draw(hpText, hpTextX, nameY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

            // 2. Health Bar below text
            float bX = cardX + (cardW - barW) / 2.0f;
            float bY = cardY + 14;

            // Background track
            mc.textRenderer.draw(fullBar, bX, bY, 0xFF35383E, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, bgColor, light);

            // Filled health segment
            if (!filledStr.isEmpty()) {
                mc.textRenderer.draw(filledStr, bX, bY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            }

            // Absorption overlay segment
            if (absorb > 0) {
                int absorbSegments = Math.min(barLength - filled, (int) Math.ceil((absorb / maxHealth) * barLength));
                if (absorbSegments > 0) {
                    StringBuilder sbAbs = new StringBuilder();
                    for (int i = 0; i < absorbSegments; i++) sbAbs.append("█");
                    float absX = bX + mc.textRenderer.getWidth(filledStr);
                    mc.textRenderer.draw(sbAbs.toString(), absX, bY, 0xFFFFD700, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
                }
            }
        } else {
            // Compact Mode
            float hpTextX = -hpW / 2.0f;
            float hpTextY = cardY + 1;
            mc.textRenderer.draw(hpText, hpTextX, hpTextY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, bgColor, light);

            float bX = cardX + (cardW - barW) / 2.0f;
            float bY = cardY + 10;

            mc.textRenderer.draw(fullBar, bX, bY, 0xFF35383E, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, bgColor, light);

            if (!filledStr.isEmpty()) {
                mc.textRenderer.draw(filledStr, bX, bY, hpColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            }
        }

        matrices.pop();
    }
}
