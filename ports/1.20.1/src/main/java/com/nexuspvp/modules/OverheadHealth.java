package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

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
        super("OverheadHealth", "Mini-TargetHUD floating health card above entities", Category.RENDER);
        setEnabled(true);
    }

    public void renderOverhead(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light) {
        if (mc.player == null || entity == mc.player || !entity.isAlive() || entity.isInvisible()) return;
        if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) return;
        if (entity.squaredDistanceTo(mc.player) > range.getValue() * range.getValue()) return;
        if (!mc.player.canSee(entity)) return;

        float currentHp = entity.getHealth();
        float maxHp = entity.getMaxHealth();
        float currentAbs = entity.getAbsorptionAmount();

        // TargetHUD-identical smooth animation and Dota-style Ghost Damage tracking
        HealthTracker tracker = trackers.computeIfAbsent(entity.getId(), id -> {
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

        tracker.animatedHealth = MathHelper.lerp(0.18f, tracker.animatedHealth, currentHp);
        tracker.animatedAbsorption = MathHelper.lerp(0.18f, tracker.animatedAbsorption, currentAbs);

        if (System.currentTimeMillis() - tracker.lastDamageTime > 280) {
            tracker.damageGhostHealth = MathHelper.lerp(0.08f, tracker.damageGhostHealth, currentHp);
        }
        if (currentHp > tracker.damageGhostHealth) {
            tracker.damageGhostHealth = currentHp;
        }

        // Entity name
        String name = entity.getName().getString();
        if (name.length() > 16) {
            name = name.substring(0, 14) + "..";
        }

        // HP text
        String hpText = String.format("%.1f", currentHp) + " / " + String.format("%.0f", maxHp) + " ❤";
        if (currentAbs > 0) {
            hpText += " (+" + String.format("%.1f", currentAbs) + ")";
        }

        float healthPct = MathHelper.clamp(tracker.animatedHealth / maxHp, 0.0f, 1.0f);
        float ghostPct = MathHelper.clamp(tracker.damageGhostHealth / maxHp, 0.0f, 1.0f);
        int hpColor = healthPct > 0.6f ? 0xFF23A55A : (healthPct > 0.3f ? 0xFFFEE75C : 0xFFED4245);
        int hpTextColor = currentAbs > 0 ? 0xFFFFD700 : hpColor;

        int nameW = mc.textRenderer.getWidth(name);
        int hpTextW = mc.textRenderer.getWidth(hpText);

        // Compact Mini-TargetHUD card dimensions
        int contentW = Math.max(nameW + hpTextW + 12, 85);
        int cardW = contentW + 8;
        int cardH = 22;
        int cardX = -cardW / 2;
        int cardY = -cardH / 2;

        matrices.push();
        matrices.translate(0.0D, entity.getHeight() + 0.55F, 0.0D);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());

        float scale = 0.020F;
        matrices.scale(scale, -scale, scale);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getTextBackground());

        // 1. Blurple border (identical to TargetHUD 0xEE5865F2)
        drawQuad(matrix, vc, cardX - 1, cardY - 1, cardW + 2, cardH + 2, 0.01f, 0xEE5865F2, light);

        // 2. Dark Discord background (identical to TargetHUD 0xFA1E1F22)
        drawQuad(matrix, vc, cardX, cardY, cardW, cardH, 0.008f, 0xFA1E1F22, light);

        // 3. Health bar
        int barX = cardX + 4;
        int barY = cardY + 13;
        int barW = cardW - 8;
        int barH = 5;

        // Health bar track background (0xFF2B2D31)
        drawQuad(matrix, vc, barX, barY, barW, barH, 0.006f, 0xFF2B2D31, light);

        // Ghost damage bar (White 0xFFF2F3F5)
        if (ghostDamage.isEnabled() && ghostPct > 0) {
            float ghostW = barW * ghostPct;
            drawQuad(matrix, vc, barX, barY, ghostW, barH, 0.004f, 0xFFF2F3F5, light);
        }

        // Active health bar (hpColor)
        if (healthPct > 0) {
            float fillW = barW * healthPct;
            drawQuad(matrix, vc, barX, barY, fillW, barH, 0.002f, hpColor, light);
        }

        // Absorption bar (Gold 0xFFFFD700)
        if (tracker.animatedAbsorption > 0) {
            float absPct = MathHelper.clamp(tracker.animatedAbsorption / 20.0f, 0.0f, 1.0f);
            float absW = barW * absPct;
            drawQuad(matrix, vc, barX, barY + barH - 2, absW, 2, 0.0f, 0xFFFFD700, light);
        }

        // 4. Text - rendered via normal TextRenderer (depth tested)
        int textY = cardY + 3;

        // Name on the left
        mc.textRenderer.draw(
            name,
            cardX + 4,
            textY,
            0xFFF2F3F5,
            false,
            matrix,
            vertexConsumers,
            TextRenderer.TextLayerType.NORMAL,
            0,
            light
        );

        // HP text on the right
        int hpTextX = cardX + cardW - hpTextW - 4;
        mc.textRenderer.draw(
            hpText,
            hpTextX,
            textY,
            hpTextColor,
            false,
            matrix,
            vertexConsumers,
            TextRenderer.TextLayerType.NORMAL,
            0,
            light
        );

        matrices.pop();
    }

    private void drawQuad(Matrix4f matrix, VertexConsumer vc, float x, float y, float w, float h, float z, int color, int light) {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        vc.vertex(matrix, x, y, z).color(r, g, b, a).light(light);
        vc.vertex(matrix, x, y + h, z).color(r, g, b, a).light(light);
        vc.vertex(matrix, x + w, y + h, z).color(r, g, b, a).light(light);
        vc.vertex(matrix, x + w, y, z).color(r, g, b, a).light(light);
    }
}
