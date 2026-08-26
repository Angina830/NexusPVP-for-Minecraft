package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;


import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class OverheadHealth extends Module {

    private final BooleanSetting playersOnly = addSetting(new BooleanSetting("PlayersOnly", false));
    private final BooleanSetting showAbsorption = addSetting(new BooleanSetting("Absorption", true));
    private final BooleanSetting showDotaGhost = addSetting(new BooleanSetting("DotaHealth", true));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 35.0, 5.0, 64.0, 1.0));
    private final NumberSetting scale = addSetting(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.05));
    private final NumberSetting yOffset = addSetting(new NumberSetting("YOffset", 0.0, -1.0, 2.0, 0.05));

    // Per-entity Dota 2 damage ghost health tracking
    private final Map<Integer, EntityHpData> hpMap = new HashMap<>();

    public OverheadHealth() {
        super("OverheadHealth", "TargetHUD-style Dota 2 health bar floating above entities", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;

        long now = System.currentTimeMillis();
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof LivingEntity && e != mc.player) {
                LivingEntity living = (LivingEntity) e;
                float currentHealth = living.getHealth();
                float currentAbs = living.getAbsorptionAmount();

                EntityHpData data = hpMap.computeIfAbsent(e.getId(), id -> new EntityHpData(currentHealth, currentAbs));

                if (currentHealth < data.prevHealth) {
                    data.lastDamageTime = now;
                }
                data.prevHealth = currentHealth;

                // Smooth animated health
                data.animatedHealth += (currentHealth - data.animatedHealth) * 0.25f;

                // Dota 2 ghost catchup after 350ms
                if (now - data.lastDamageTime > 350) {
                    data.ghostHealth += (currentHealth - data.ghostHealth) * 0.15f;
                }
                if (data.ghostHealth < currentHealth) {
                    data.ghostHealth = currentHealth;
                }

                data.animatedAbs += (currentAbs - data.animatedAbs) * 0.20f;
            }
        }

        // Clean up dead/despawned entities
        hpMap.entrySet().removeIf(entry -> mc.world.getEntityById(entry.getKey()) == null);
    }

    public void renderOverhead(LivingEntity entity, MatrixStack matrices, float tickDelta) {
        if (mc.player == null || entity == mc.player || !entity.isAlive() || entity.isInvisible()) return;
        if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) return;

        double distSq = entity.squaredDistanceTo(mc.player);
        if (distSq > range.getValue() * range.getValue()) return;

        EntityHpData data = hpMap.computeIfAbsent(entity.getId(), id -> new EntityHpData(entity.getHealth(), entity.getAbsorptionAmount()));

        float userScale = scale.getFloatValue();
        float yOff = yOffset.getFloatValue();

        matrices.push();
        // Translate right above entity model head
        matrices.translate(0.0D, (double) (entity.getHeight() + 0.5F + yOff), 0.0D);
        // Face camera smoothly
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());

        float sc = 0.025F * userScale;
        matrices.scale(-sc, -sc, sc);

        renderOverheadCard(matrices, entity, data);

        matrices.pop();
    }

    private void renderOverheadCard(MatrixStack matrices, LivingEntity entity, EntityHpData data) {
        int cardW = 76;
        int cardH = 17;
        int halfW = cardW / 2;

        // Dark Discord container
        RenderUtils.drawRoundedRect(matrices, -halfW - 1, -cardH / 2 - 1, cardW + 2, cardH + 2, 4, 0xAA111214);
        RenderUtils.drawRoundedRect(matrices, -halfW, -cardH / 2, cardW, cardH, 3, 0xEE1E1F22);

        // Entity Name & HP Number
        String name = entity.getName().getString();
        if (mc.textRenderer.getWidth(name) > 42) {
            name = name.substring(0, Math.min(name.length(), 6)) + "..";
        }
        Compat.drawText(matrices, name, -halfW + 4, -cardH / 2 + 2, 0xFFF2F3F5);

        float currentHp = entity.getHealth();
        float maxHp = Math.max(1.0f, entity.getMaxHealth());
        String hpText = String.format("%.1f", currentHp);
        int hpW = mc.textRenderer.getWidth(hpText);
        Compat.drawText(matrices, hpText, halfW - hpW - 4, -cardH / 2 + 2, 0xFF22C55E);

        // Dota 2 Animated Health Bar
        int barX = -halfW + 3;
        int barY = 1;
        int barW = cardW - 6;
        int barH = 4;

        // Bar background
        RenderUtils.drawRoundedRect(matrices, barX, barY, barW, barH, 2, 0xFF351C1C);

        // 1. Dota 2 White Damage Ghost Bar
        if (showDotaGhost.isEnabled() && data.ghostHealth > currentHp) {
            float ghostPct = MathHelper.clamp(data.ghostHealth / maxHp, 0.0f, 1.0f);
            int ghostW = (int) (barW * ghostPct);
            if (ghostW > 0) {
                RenderUtils.drawRoundedRect(matrices, barX, barY, ghostW, barH, 2, 0xFFFFFFFF);
            }
        }

        // 2. Main Animated Health Bar (Green / Yellow / Red based on HP%)
        float hpPct = MathHelper.clamp(data.animatedHealth / maxHp, 0.0f, 1.0f);
        int mainW = (int) (barW * hpPct);
        if (mainW > 0) {
            int hpColor;
            if (hpPct > 0.5f) {
                hpColor = 0xFF22C55E; // Emerald Green
            } else if (hpPct > 0.25f) {
                hpColor = 0xFFEAB308; // Yellow
            } else {
                hpColor = 0xFFEF4444; // Red
            }
            RenderUtils.drawRoundedRect(matrices, barX, barY, mainW, barH, 2, hpColor);
        }

        // 3. Absorption Golden Bar Overlay
        if (showAbsorption.isEnabled() && data.animatedAbs > 0.01f) {
            float absPct = MathHelper.clamp(data.animatedAbs / maxHp, 0.0f, 1.0f);
            int absW = (int) (barW * absPct);
            if (absW > 0) {
                RenderUtils.drawRoundedRect(matrices, barX, barY + 2, absW, 2, 1, 0xFFF59E0B);
            }
        }
    }

    private static class EntityHpData {
        float prevHealth;
        float animatedHealth;
        float ghostHealth;
        float animatedAbs;
        long lastDamageTime = 0;

        EntityHpData(float health, float abs) {
            this.prevHealth = health;
            this.animatedHealth = health;
            this.ghostHealth = health;
            this.animatedAbs = abs;
        }
    }
}