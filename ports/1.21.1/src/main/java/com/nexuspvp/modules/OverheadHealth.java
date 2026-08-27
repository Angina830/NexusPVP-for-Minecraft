package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class OverheadHealth extends Module {

    private final BooleanSetting playersOnly = addSetting(new BooleanSetting("PlayersOnly", false));
    private final BooleanSetting showName = addSetting(new BooleanSetting("ShowName", true));
    private final BooleanSetting ghostDamage = addSetting(new BooleanSetting("GhostDamage", true));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 35.0, 5.0, 60.0, 1.0));

    private static class HealthTracker {
        float ghostHp;
        float previousHp;
        long lastHitTime;
    }

    private final Map<Integer, HealthTracker> trackers = new HashMap<>();

    public OverheadHealth() {
        super("OverheadHealth", "Clean pixel health bar above entities with Dota-style ghost damage", Category.RENDER);
        setEnabled(true);
    }

    public void renderOverhead(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light) {
        if (mc.player == null || entity == mc.player || !entity.isAlive() || entity.isInvisible()) return;
        if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) return;
        if (entity.squaredDistanceTo(mc.player) > range.getValue() * range.getValue()) return;
        if (!mc.player.canSee(entity)) return;

        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float absorb = entity.getAbsorptionAmount();

        // Ghost damage tracking
        HealthTracker tracker = trackers.computeIfAbsent(entity.getId(), id -> {
            HealthTracker t = new HealthTracker();
            t.ghostHp = health;
            t.previousHp = health;
            t.lastHitTime = 0;
            return t;
        });

        if (health < tracker.previousHp) {
            tracker.ghostHp = tracker.previousHp;
            tracker.lastHitTime = System.currentTimeMillis();
        }
        tracker.previousHp = health;

        if (System.currentTimeMillis() - tracker.lastHitTime > 400) {
            tracker.ghostHp = MathHelper.lerp(0.08f, tracker.ghostHp, health);
        }
        if (health > tracker.ghostHp) {
            tracker.ghostHp = health;
        }

        float healthPct = MathHelper.clamp(health / maxHealth, 0.0f, 1.0f);
        float ghostPct = MathHelper.clamp(tracker.ghostHp / maxHealth, 0.0f, 1.0f);

        matrices.push();
        matrices.translate(0.0D, entity.getHeight() + 0.5F, 0.0D);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());

        float scale = 0.02F;
        matrices.scale(scale, -scale, scale);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Bar dimensions (in scaled units)
        float barW = 80.0f;
        float barH = 5.0f;
        float barX = -barW / 2.0f;
        float barY = showName.isEnabled() ? 2.0f : -2.0f;

        // Health color gradient
        int hpColor;
        if (healthPct > 0.55f) {
            hpColor = 0xFF23A55A; // Green
        } else if (healthPct > 0.25f) {
            hpColor = 0xFFFEE75C; // Yellow
        } else {
            hpColor = 0xFFED4245; // Red
        }

        // ---- RENDER WITH VERTEX CONSUMER (depth-tested, no see-through) ----
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getTextBackgroundSeeThrough());

        // 1. Dark outer border
        drawFlatQuad(matrix, vc, barX - 1, barY - 1, barW + 2, barH + 2, 0xDD000000);

        // 2. Background track (dark grey)
        drawFlatQuad(matrix, vc, barX, barY, barW, barH, 0xDD2B2D31);

        // 3. Ghost damage (white segment, Dota-style)
        if (ghostDamage.isEnabled() && ghostPct > healthPct) {
            float ghostW = barW * ghostPct;
            drawFlatQuad(matrix, vc, barX, barY, ghostW, barH, 0xDDFFFFFF);
        }

        // 4. Active health fill
        float fillW = barW * healthPct;
        if (fillW > 0) {
            drawFlatQuad(matrix, vc, barX, barY, fillW, barH, hpColor);
        }

        // 5. Absorption (gold, after health)
        if (absorb > 0) {
            float absPct = MathHelper.clamp(absorb / maxHealth, 0.0f, 1.0f - healthPct);
            float absW = barW * absPct;
            drawFlatQuad(matrix, vc, barX + fillW, barY, absW, barH, 0xDDFFD700);
        }

        // 6. Name text above bar
        if (showName.isEnabled()) {
            String name = entity.getName().getString();
            if (name.length() > 16) name = name.substring(0, 14) + "..";
            int nameW = mc.textRenderer.getWidth(name);
            float nameX = -nameW / 2.0f;
            float nameY = -10.0f;
            mc.textRenderer.draw(name, nameX, nameY, 0xFFF2F3F5, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x88000000, light);
        }

        // 7. HP text (right-aligned below bar)
        String hpText = String.format("%.0f/%.0f", health, maxHealth);
        int hpTextW = mc.textRenderer.getWidth(hpText);
        mc.textRenderer.draw(hpText, -hpTextW / 2.0f, barY + barH + 1.5f, hpColor, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

        matrices.pop();
    }

    private void drawFlatQuad(Matrix4f matrix, VertexConsumer vc, float x, float y, float w, float h, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        vc.vertex(matrix, x, y, 0).color(r, g, b, a).light(0xF000F0);
        vc.vertex(matrix, x, y + h, 0).color(r, g, b, a).light(0xF000F0);
        vc.vertex(matrix, x + w, y + h, 0).color(r, g, b, a).light(0xF000F0);
        vc.vertex(matrix, x + w, y, 0).color(r, g, b, a).light(0xF000F0);
    }
}
