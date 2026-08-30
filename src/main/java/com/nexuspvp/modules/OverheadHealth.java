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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OverheadHealth extends Module {

    private final BooleanSetting playersOnly = addSetting(new BooleanSetting("PlayersOnly", false));
    private final BooleanSetting hideBehindWalls = addSetting(new BooleanSetting("HideBehindWalls", true));
    private final BooleanSetting ghostDamage = addSetting(new BooleanSetting("GhostDamage", true));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 35.0, 5.0, 60.0, 1.0));

    private static class HealthTracker {
        float animatedHealth;
        float damageGhostHealth;
        float healGhostHealth;
        float previousHealth;
        long lastHealTime;
        float animatedAbsorption;
        long lastDamageTime;
        long lastSeenTime;
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
    public void onTick() {
        if (trackers.isEmpty()) return;
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, HealthTracker>> it = trackers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, HealthTracker> entry = it.next();
            if (now - entry.getValue().lastSeenTime > 4000) {
                it.remove();
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.world == null || mc.player == null) return;

        double maxDistSq = range.getValue() * range.getValue();
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        long now = System.currentTimeMillis();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity) || entity == mc.player) continue;
            if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) continue;

            LivingEntity living = (LivingEntity) entity;
            if (!living.isAlive() || living.isInvisibleTo(mc.player)) continue;

            double distSq = living.squaredDistanceTo(camPos.x, camPos.y, camPos.z);
            if (distSq > maxDistSq) continue;

            double interpX = MathHelper.lerp((double) tickDelta, living.lastRenderX, living.getX());
            double interpY = MathHelper.lerp((double) tickDelta, living.lastRenderY, living.getY());
            double interpZ = MathHelper.lerp((double) tickDelta, living.lastRenderZ, living.getZ());

            // Strict Wall Occlusion Check: Hide health bar if entity or head is blocked by blocks
            if (hideBehindWalls.isEnabled()) {
                if (!mc.player.canSee(living)) continue;
                Vec3d eyePos = mc.player.getCameraPosVec(tickDelta);
                Vec3d targetPos = new Vec3d(interpX, interpY + living.getHeight() + 0.35D, interpZ);
                RaycastContext rayCtx = new RaycastContext(
                    eyePos,
                    targetPos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
                );
                BlockHitResult hit = mc.world.raycast(rayCtx);
                if (hit.getType() != HitResult.Type.MISS && hit.getPos().squaredDistanceTo(eyePos) < targetPos.squaredDistanceTo(eyePos) - 0.4) {
                    continue;
                }
            }

            RenderSystem.pushMatrix();
            RenderSystem.translated(interpX - camPos.x, interpY - camPos.y + living.getHeight() + 0.55D, interpZ - camPos.z);
            RenderSystem.rotatef(-mc.gameRenderer.getCamera().getYaw() + 180.0F, 0.0F, 1.0F, 0.0F);
            RenderSystem.rotatef(mc.gameRenderer.getCamera().getPitch(), 1.0F, 0.0F, 0.0F);

            float scale = 0.020F;
            RenderSystem.scalef(scale, -scale, scale);

            renderGraphicalCard(living, now);

            RenderSystem.popMatrix();
        }
    }

    private void renderGraphicalCard(LivingEntity entity, long now) {
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
            t.lastSeenTime = now;
            return t;
        });

        tracker.lastSeenTime = now;

        if (currentHp < tracker.previousHealth) {
            tracker.damageGhostHealth = tracker.previousHealth;
            tracker.lastDamageTime = now;
            tracker.healGhostHealth = currentHp;
        } else if (currentHp > tracker.previousHealth) {
            tracker.healGhostHealth = currentHp;
            tracker.lastHealTime = now;
            tracker.damageGhostHealth = currentHp;
        }
        tracker.previousHealth = currentHp;

        tracker.animatedHealth = MathHelper.lerp(0.20f, tracker.animatedHealth, currentHp);
        tracker.animatedAbsorption = MathHelper.lerp(0.20f, tracker.animatedAbsorption, currentAbs);

        if (now - tracker.lastDamageTime > 280) {
            tracker.damageGhostHealth = MathHelper.lerp(0.08f, tracker.damageGhostHealth, currentHp);
        }
        if (currentHp > tracker.damageGhostHealth) {
            tracker.damageGhostHealth = currentHp;
        }
        if (now - tracker.lastHealTime > 300) {
            tracker.healGhostHealth = MathHelper.lerp(0.12f, tracker.healGhostHealth, tracker.animatedHealth);
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
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        float barX = cardX + 4;
        float barY = cardY + 13;
        float barW = cardW - 8;
        float barH = 5;

        // Draw ALL card rectangles in ONE single atomic Draw Call!
        RenderSystem.disableTexture();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);

        // 1. Blurple border
        addQuad(buffer, cardX - 1, cardY - 1, cardW + 2, cardH + 2, 0xEE5865F2);

        // 2. Dark Discord background
        addQuad(buffer, cardX, cardY, cardW, cardH, 0xFA1E1F22);

        // 3. Health bar track
        addQuad(buffer, barX, barY, barW, barH, 0xFF2B2D31);

        // 4. Ghost damage bar (behind active or trailing chunk)
        if (ghostDamage.isEnabled() && ghostPct > 0) {
            float ghostWidth = barW * ghostPct;
            addQuad(buffer, barX, barY, ghostWidth, barH, 0xFFF2F3F5);
        }

        // 4.5. Ghost Heal Bar (Bright Emerald Green)
        if (tracker.healGhostHealth > tracker.animatedHealth) {
            float healPct = MathHelper.clamp(tracker.healGhostHealth / maxHp, 0.0f, 1.0f);
            float healWidth = barW * healPct;
            addQuad(buffer, barX, barY, healWidth, barH, 0xFF57F287);
        }

        // 5. Active health bar (Green / Yellow / Red) from left to right
        if (healthPct > 0) {
            float fillW = barW * healthPct;
            addQuad(buffer, barX, barY, fillW, barH, hpColor);
        }

        // 6. Absorption bar
        if (tracker.animatedAbsorption > 0) {
            float absPct = MathHelper.clamp(tracker.animatedAbsorption / 20.0f, 0.0f, 1.0f);
            float absW = barW * absPct;
            addQuad(buffer, barX, barY + barH - 2, absW, 2, 0xFFFFD700);
        }

        Tessellator.getInstance().draw();
        RenderSystem.enableTexture();

        // Draw text
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

    private void addQuad(BufferBuilder buffer, float x, float y, float width, float height, int color) {
        float a = (color >> 24 & 0xFF) / 255.0f;
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        buffer.vertex(x, y + height, 0.0D).color(r, g, b, a).next();
        buffer.vertex(x + width, y + height, 0.0D).color(r, g, b, a).next();
        buffer.vertex(x + width, y, 0.0D).color(r, g, b, a).next();
        buffer.vertex(x, y, 0.0D).color(r, g, b, a).next();
    }
}
