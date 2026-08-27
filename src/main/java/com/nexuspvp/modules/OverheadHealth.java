package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class OverheadHealth extends Module {

    private final BooleanSetting playersOnly = addSetting(new BooleanSetting("PlayersOnly", false));
    private final BooleanSetting ghostDamage = addSetting(new BooleanSetting("GhostDamage", true));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 35.0, 5.0, 60.0, 1.0));

    private static class HealthTracker {
        float animatedHealth;
        float damageGhostHealth;
        float previousHealth;
        float animatedAbsorption;
        long lastDamageTime;
    }

    private final Map<Integer, HealthTracker> trackers = new HashMap<>();

    public OverheadHealth() {
        super("OverheadHealth", "Mini-TargetHUD floating health card above entities", Category.PVP);
        setEnabled(true);
    }

    public int getTrackedEntitiesCount() {
        return trackers.size();
    }

    public void renderOverhead(LivingEntity entity, double x, double y, double z, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light) {
        if (mc.player == null || entity == mc.player || !entity.isAlive() || entity.isInvisible()) return;
        if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) return;
        if (entity.squaredDistanceTo(mc.player) > range.getValue() * range.getValue()) return;

        float currentHp = entity.getHealth();
        float maxHp = Math.max(1.0f, entity.getMaxHealth());
        float currentAbs = entity.getAbsorptionAmount();

        HealthTracker tracker = trackers.computeIfAbsent(entity.getEntityId(), id -> {
            HealthTracker t = new HealthTracker();
            t.animatedHealth = currentHp;
            t.damageGhostHealth = currentHp;
            t.previousHealth = currentHp;
            t.animatedAbsorption = currentAbs;
            t.lastDamageTime = 0;
            return t;
        });

        if (currentHp < tracker.previousHealth) {
            tracker.damageGhostHealth = tracker.previousHealth;
            tracker.lastDamageTime = System.currentTimeMillis();
        }
        tracker.previousHealth = currentHp;

        tracker.animatedHealth = MathHelper.lerp(0.20f, tracker.animatedHealth, currentHp);
        tracker.animatedAbsorption = MathHelper.lerp(0.20f, tracker.animatedAbsorption, currentAbs);

        if (System.currentTimeMillis() - tracker.lastDamageTime > 280) {
            tracker.damageGhostHealth = MathHelper.lerp(0.08f, tracker.damageGhostHealth, currentHp);
        }
        if (currentHp > tracker.damageGhostHealth) {
            tracker.damageGhostHealth = currentHp;
        }

        String name = entity.getName().getString();
        if (name.length() > 14) {
            name = name.substring(0, 12) + "..";
        }

        String hpText = String.format("%.1f", currentHp) + " / " + String.format("%.0f", maxHp) + " \u2764";
        if (currentAbs > 0) {
            hpText += " (+" + String.format("%.1f", currentAbs) + ")";
        }

        float healthPct = MathHelper.clamp(tracker.animatedHealth / maxHp, 0.0f, 1.0f);
        float ghostPct = MathHelper.clamp(tracker.damageGhostHealth / maxHp, 0.0f, 1.0f);
        int hpColor = healthPct > 0.6f ? 0xFF23A55A : (healthPct > 0.3f ? 0xFFFEE75C : 0xFFED4245);
        int hpTextColor = currentAbs > 0 ? 0xFFFFD700 : hpColor;

        int nameW = mc.textRenderer.getWidth(name);
        int hpTextW = mc.textRenderer.getWidth(hpText);

        int cardW = Math.max(nameW + hpTextW + 16, 95);
        int cardH = 22;
        float cardX = -cardW / 2.0f;
        float cardY = -cardH / 2.0f;

        matrices.push();
        matrices.translate(x, y + entity.getHeight() + 0.55D, z);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());

        float scale = 0.020F;
        matrices.scale(-scale, -scale, scale);

        Matrix4f mat = matrices.peek().getModel();
        int fullLight = 0xF000F0;
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getTextSeeThrough(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE));

        // 1. Blurple border
        drawBatchedQuad(buffer, mat, cardX - 1, cardY - 1, cardX + cardW + 1, cardY + cardH + 1, 0xEE5865F2, fullLight);

        // 2. Dark Discord background
        drawBatchedQuad(buffer, mat, cardX, cardY, cardX + cardW, cardY + cardH, 0xFA1E1F22, fullLight);

        // 3. Health bar
        float barX = cardX + 4;
        float barY = cardY + 13;
        float barW = cardW - 8;
        float barH = 5;

        // Health bar track
        drawBatchedQuad(buffer, mat, barX, barY, barX + barW, barY + barH, 0xFF2B2D31, fullLight);

        // Ghost damage bar
        if (ghostDamage.isEnabled() && ghostPct > 0) {
            float ghostW = barW * ghostPct;
            drawBatchedQuad(buffer, mat, barX, barY, barX + ghostW, barY + barH, 0xFFF2F3F5, fullLight);
        }

        // Active health bar
        if (healthPct > 0) {
            float fillW = barW * healthPct;
            drawBatchedQuad(buffer, mat, barX, barY, barX + fillW, barY + barH, hpColor, fullLight);
        }

        // Absorption bar
        if (tracker.animatedAbsorption > 0) {
            float absPct = MathHelper.clamp(tracker.animatedAbsorption / 20.0f, 0.0f, 1.0f);
            float absW = barW * absPct;
            drawBatchedQuad(buffer, mat, barX, barY + barH - 2, barX + absW, barY + barH, 0xFFFFD700, fullLight);
        }

        // 4. Text rendered via TextRenderer
        float textY = cardY + 3;

        mc.textRenderer.draw(
            name,
            cardX + 4,
            textY,
            0xFFF2F3F5,
            false,
            mat,
            vertexConsumers,
            false,
            0,
            fullLight
        );

        float hpTextX = cardX + cardW - hpTextW - 4;
        mc.textRenderer.draw(
            hpText,
            hpTextX,
            textY,
            hpTextColor,
            false,
            mat,
            vertexConsumers,
            false,
            0,
            fullLight
        );

        matrices.pop();
    }

    private void drawBatchedQuad(VertexConsumer buffer, Matrix4f mat, float x1, float y1, float x2, float y2, int color, int light) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        buffer.vertex(mat, x1, y1, 0.0f).color(r, g, b, a).light(light).next();
        buffer.vertex(mat, x1, y2, 0.0f).color(r, g, b, a).light(light).next();
        buffer.vertex(mat, x2, y2, 0.0f).color(r, g, b, a).light(light).next();
        buffer.vertex(mat, x2, y1, 0.0f).color(r, g, b, a).light(light).next();
    }
}
