package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.world == null || mc.player == null) return;

        double maxDistSq = range.getValue() * range.getValue();
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity) || entity == mc.player) continue;
            if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) continue;

            LivingEntity living = (LivingEntity) entity;
            if (!living.isAlive() || living.isInvisibleTo(mc.player)) continue;

            double distSq = living.squaredDistanceTo(camPos.x, camPos.y, camPos.z);
            if (distSq > maxDistSq) continue;

            double interpX = MathHelper.lerp((double) tickDelta, living.prevX, living.getX());
            double interpY = MathHelper.lerp((double) tickDelta, living.prevY, living.getY());
            double interpZ = MathHelper.lerp((double) tickDelta, living.prevZ, living.getZ());

            matrices.push();
            matrices.translate(interpX - camPos.x, interpY - camPos.y + living.getHeight() + 0.55D, interpZ - camPos.z);
            matrices.multiply(mc.getEntityRenderDispatcher().getRotation());

            float scale = 0.020F;
            matrices.scale(scale, -scale, scale);

            renderGraphicalCard(matrices, living);

            matrices.pop();
        }
    }

    private void renderGraphicalCard(MatrixStack matrices, LivingEntity entity) {
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

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        // 1. Blurple border
        RenderUtils.drawRect(matrices, cardX - 1, cardY - 1, cardW + 2, cardH + 2, 0xEE5865F2);

        // 2. Dark Discord background
        RenderUtils.drawRect(matrices, cardX, cardY, cardW, cardH, 0xFA1E1F22);

        // 3. Health bar
        float barX = cardX + 4;
        float barY = cardY + 13;
        float barW = cardW - 8;
        float barH = 5;

        RenderUtils.drawRect(matrices, barX, barY, barW, barH, 0xFF2B2D31);

        if (ghostDamage.isEnabled() && ghostPct > 0) {
            float ghostW = barW * ghostPct;
            RenderUtils.drawRect(matrices, barX, barY, ghostW, barH, 0xFFF2F3F5);
        }

        if (healthPct > 0) {
            float fillW = barW * healthPct;
            RenderUtils.drawRect(matrices, barX, barY, fillW, barH, hpColor);
        }

        if (tracker.animatedAbsorption > 0) {
            float absPct = MathHelper.clamp(tracker.animatedAbsorption / 20.0f, 0.0f, 1.0f);
            float absW = barW * absPct;
            RenderUtils.drawRect(matrices, barX, barY + barH - 2, absW, 2, 0xFFFFD700);
        }

        float textY = cardY + 3;
        VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();

        mc.textRenderer.draw(
            name,
            cardX + 4,
            textY,
            0xFFF2F3F5,
            false,
            matrices.peek().getModel(),
            vertexConsumers,
            false,
            0,
            0xF000F0
        );

        float hpTextX = cardX + cardW - hpTextW - 4;
        mc.textRenderer.draw(
            hpText,
            hpTextX,
            textY,
            hpTextColor,
            false,
            matrices.peek().getModel(),
            vertexConsumers,
            false,
            0,
            0xF000F0
        );

        vertexConsumers.draw();

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
