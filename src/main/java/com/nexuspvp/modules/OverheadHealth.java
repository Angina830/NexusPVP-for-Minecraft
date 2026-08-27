package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

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

            // Strictly require direct line of sight
            if (!mc.player.canSee(living)) continue;

            double interpX = MathHelper.lerp((double) tickDelta, living.lastRenderX, living.getX());
            double interpY = MathHelper.lerp((double) tickDelta, living.lastRenderY, living.getY());
            double interpZ = MathHelper.lerp((double) tickDelta, living.lastRenderZ, living.getZ());

            RenderSystem.pushMatrix();
            RenderSystem.translated(interpX - camPos.x, interpY - camPos.y + living.getHeight() + 0.55D, interpZ - camPos.z);
            RenderSystem.rotatef(-mc.gameRenderer.getCamera().getYaw(), 0.0F, 1.0F, 0.0F);
            RenderSystem.rotatef(mc.gameRenderer.getCamera().getPitch(), 1.0F, 0.0F, 0.0F);

            float scale = 0.020F;
            RenderSystem.scalef(-scale, -scale, scale);

            renderGraphicalCard(living);

            RenderSystem.popMatrix();
        }
    }

    private void renderGraphicalCard(LivingEntity entity) {
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
        RenderSystem.disableAlphaTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        // 1. Blurple border
        drawDirectQuad(cardX - 1, cardY - 1, cardW + 2, cardH + 2, 0xEE5865F2);

        // 2. Dark Discord background
        drawDirectQuad(cardX, cardY, cardW, cardH, 0xFA1E1F22);

        // 3. Health bar track
        float barX = cardX + 4;
        float barY = cardY + 13;
        float barW = cardW - 8;
        float barH = 5;

        drawDirectQuad(barX, barY, barW, barH, 0xFF2B2D31);

        // Active health bar (Green / Yellow / Red)
        if (healthPct > 0) {
            float fillW = barW * healthPct;
            drawDirectQuad(barX, barY, fillW, barH, hpColor);
        }

        // Ghost damage bar: ONLY draw the trailing damage lost segment when ghost > active health
        if (ghostDamage.isEnabled() && ghostPct > healthPct + 0.005f) {
            float ghostStart = barX + (barW * healthPct);
            float ghostWidth = barW * (ghostPct - healthPct);
            drawDirectQuad(ghostStart, barY, ghostWidth, barH, 0xFFF2F3F5);
        }

        // Absorption bar
        if (tracker.animatedAbsorption > 0) {
            float absPct = MathHelper.clamp(tracker.animatedAbsorption / 20.0f, 0.0f, 1.0f);
            float absW = barW * absPct;
            drawDirectQuad(barX, barY + barH - 2, absW, 2, 0xFFFFD700);
        }

        float textY = cardY + 3;
        VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();

        Matrix4f identity = new Matrix4f();
        identity.loadIdentity();

        mc.textRenderer.draw(
            name,
            cardX + 4,
            textY,
            0xFFF2F3F5,
            false,
            identity,
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
            identity,
            vertexConsumers,
            false,
            0,
            0xF000F0
        );

        vertexConsumers.draw();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private void drawDirectQuad(float x, float y, float width, float height, int color) {
        float a = (color >> 24 & 0xFF) / 255.0f;
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x, y + height, 0).color(r, g, b, a).next();
        buffer.vertex(x + width, y + height, 0).color(r, g, b, a).next();
        buffer.vertex(x + width, y, 0).color(r, g, b, a).next();
        buffer.vertex(x, y, 0).color(r, g, b, a).next();
        tessellator.draw();

        RenderSystem.enableTexture();
    }
}
