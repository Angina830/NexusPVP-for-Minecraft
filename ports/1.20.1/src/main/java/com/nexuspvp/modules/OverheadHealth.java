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
import net.minecraft.text.Text;
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

        float healthPct = MathHelper.clamp(tracker.animatedHealth / maxHp, 0.0f, 1.0f);
        float ghostPct = MathHelper.clamp(tracker.damageGhostHealth / maxHp, 0.0f, 1.0f);

        String hpColorCode = healthPct > 0.6f ? "§a" : (healthPct > 0.3f ? "§e" : "§c");
        String hpText = String.format("%s%.1f §7/ %s%.0f §c❤", hpColorCode, currentHp, hpColorCode, maxHp);
        if (currentAbs > 0) {
            hpText += String.format(" §6(+%.1f)", currentAbs);
        }

        int totalBars = 20;
        int filledBars = Math.round(healthPct * totalBars);
        int ghostBars = Math.round(ghostPct * totalBars);

        StringBuilder barBuilder = new StringBuilder();
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                barBuilder.append(hpColorCode).append("■");
            } else if (i < ghostBars && ghostDamage.isEnabled()) {
                barBuilder.append("§f■");
            } else {
                barBuilder.append("§8■");
            }
        }
        String barStr = barBuilder.toString();

        String line1 = "§f§l" + name + "  " + hpText;
        String line2 = barStr;

        int w1 = mc.textRenderer.getWidth(line1);
        int w2 = mc.textRenderer.getWidth(line2);

        matrices.push();
        matrices.translate(x, y + entity.getHeight() + 0.55D, z);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());

        float scale = 0.022F;
        matrices.scale(-scale, -scale, scale);

        Matrix4f mat = matrices.peek().getPositionMatrix();
        int fullLight = 0xF000F0;
        int bg = 0xB0111214;

        float x1 = -w1 / 2.0f;
        mc.textRenderer.draw(
            Text.literal(line1),
            x1,
            -10.0f,
            0xFFFFFFFF,
            false,
            mat,
            vertexConsumers,
            TextRenderer.TextLayerType.NORMAL,
            bg,
            fullLight
        );

        float x2 = -w2 / 2.0f;
        mc.textRenderer.draw(
            Text.literal(line2),
            x2,
            2.0f,
            0xFFFFFFFF,
            false,
            mat,
            vertexConsumers,
            TextRenderer.TextLayerType.NORMAL,
            bg,
            fullLight
        );

        matrices.pop();
    }
}
