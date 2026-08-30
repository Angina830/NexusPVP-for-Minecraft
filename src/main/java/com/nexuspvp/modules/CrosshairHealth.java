package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.gui.ClickGui;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class CrosshairHealth extends Module {

    private final NumberSetting posX = addSetting(new NumberSetting("PosX", 0, -500, 500, 2));
    private final NumberSetting posY = addSetting(new NumberSetting("PosY", 26, -400, 400, 2));
    private final NumberSetting width = addSetting(new NumberSetting("Width", 72, 30, 160, 2));
    private final NumberSetting height = addSetting(new NumberSetting("Height", 4, 2, 10, 1));
    private final BooleanSetting showNumbers = addSetting(new BooleanSetting("ShowNumbers", true));
    private final BooleanSetting ghostDamage = addSetting(new BooleanSetting("GhostDamage", true));
    private final BooleanSetting preview = addSetting(new BooleanSetting("Preview", false));

    private LivingEntity target = null;
    private long lastTargetTime = 0;
    private float fadeAlpha = 0.0f;

    // Dota 2 style health bar animation
    private float animatedHealth = 20.0f;
    private float damageGhostHealth = 20.0f;
    private float previousTargetHealth = 20.0f;
    private long lastDamageTime = 0;
    private float animatedAbsorption = 0.0f;

    public CrosshairHealth() {
        super("CrosshairHealth", "Minimalistic under-crosshair health bar with Dota ghost damage & golden absorption", Category.PVP);
        setEnabled(true);
    }

    public void setTarget(LivingEntity entity) {
        if (entity != null && entity != mc.player) {
            if (this.target != entity) {
                this.target = entity;
                this.animatedHealth = entity.getHealth();
                this.damageGhostHealth = entity.getHealth();
                this.previousTargetHealth = entity.getHealth();
                this.animatedAbsorption = entity.getAbsorptionAmount();
            }
            this.lastTargetTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        if (mc.targetedEntity instanceof LivingEntity && mc.targetedEntity != mc.player) {
            setTarget((LivingEntity) mc.targetedEntity);
        }

        if (target != null) {
            if (!target.isAlive() || System.currentTimeMillis() - lastTargetTime > 3800) {
                target = null;
            }
        }
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null) return;

        boolean isPreview = preview.isEnabled() || (mc.currentScreen instanceof ClickGui && preview.isEnabled());
        boolean hasTarget = (target != null && target.isAlive());

        float targetFade = (hasTarget || isPreview) ? 1.0f : 0.0f;
        fadeAlpha += (targetFade - fadeAlpha) * 0.20f;
        if (fadeAlpha <= 0.01f) return;

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int barW = width.getIntValue();
        int barH = height.getIntValue();

        int centerX = screenW / 2 + posX.getIntValue();
        int centerY = screenH / 2 + posY.getIntValue();

        int barX = centerX - barW / 2;
        int barY = centerY;

        float maxHp = isPreview ? 20.0f : (target != null ? target.getMaxHealth() : 20.0f);
        float currentHp;
        if (isPreview) {
            long cycle = (System.currentTimeMillis() / 1600) % 3;
            currentHp = cycle == 0 ? 20.0f : (cycle == 1 ? 13.5f : 6.0f);
        } else {
            currentHp = target != null ? target.getHealth() : 20.0f;
        }

        float currentAbs = isPreview ? 4.0f : (target != null ? target.getAbsorptionAmount() : 0.0f);

        // Dota 2 damage tracking
        if (currentHp < previousTargetHealth) {
            damageGhostHealth = previousTargetHealth;
            lastDamageTime = System.currentTimeMillis();
        }
        previousTargetHealth = currentHp;

        animatedHealth = MathHelper.lerp(0.20f, animatedHealth, currentHp);
        animatedAbsorption = MathHelper.lerp(0.20f, animatedAbsorption, currentAbs);

        if (System.currentTimeMillis() - lastDamageTime > 260) {
            damageGhostHealth = MathHelper.lerp(0.08f, damageGhostHealth, currentHp);
        }
        if (currentHp > damageGhostHealth) {
            damageGhostHealth = currentHp;
        }

        int globalAlpha = (int) (255 * fadeAlpha);

        // 1. Sleek Background Track
        int pad = 2;
        int totalCardH = barH + (animatedAbsorption > 0.1f ? 5 : 0) + (showNumbers.isEnabled() ? 10 : 0);
        int bgY = showNumbers.isEnabled() ? barY - 9 : barY - pad;
        
        RenderUtils.drawRoundedRect(matrices, barX - pad - 1, bgY - 1, barW + (pad * 2) + 2, totalCardH + (pad * 2) + 2, 4, (0xAA << 24) | 0x1E1F22);
        RenderUtils.drawRoundedRect(matrices, barX - pad, bgY, barW + (pad * 2), totalCardH + (pad * 2), 3, (0xDD << 24) | 0x16171A);

        // 2. Optional Compact Numeric Text
        if (showNumbers.isEnabled()) {
            String hpText = String.format("%.1f", currentHp) + " \u2764";
            if (currentAbs > 0.1f) {
                hpText += " §6(+" + String.format("%.1f", currentAbs) + ")";
            }
            int textW = mc.textRenderer.getWidth(hpText);
            int textX = centerX - textW / 2;
            int textY = barY - 8;
            mc.textRenderer.drawWithShadow(matrices, hpText, textX, textY, (globalAlpha << 24) | (currentAbs > 0.1f ? 0xFFD700 : 0xF2F3F5));
        }

        // 3. Main Health Bar Groove
        RenderUtils.drawRoundedRect(matrices, barX, barY, barW, barH, 2, (globalAlpha << 24) | 0x2B2D31);

        // 4. Dota 2 Ghost Bar (Lost health highlighted in white/light grey)
        if (ghostDamage.isEnabled()) {
            float ghostPct = MathHelper.clamp(damageGhostHealth / maxHp, 0.0f, 1.0f);
            float ghostW = barW * ghostPct;
            if (ghostW > 0) {
                RenderUtils.drawRoundedRect(matrices, barX, barY, (int) ghostW, barH, 2, (globalAlpha << 24) | 0xF2F3F5);
            }
        }

        // 5. Active Health Bar Fill (Green / Yellow / Red)
        float healthPct = MathHelper.clamp(animatedHealth / maxHp, 0.0f, 1.0f);
        float fillW = barW * healthPct;
        if (fillW > 0) {
            int hpColor = healthPct > 0.6f ? 0x23A55A : (healthPct > 0.3f ? 0xFEE75C : 0xED4245);
            RenderUtils.drawRoundedRect(matrices, barX, barY, (int) fillW, barH, 2, (globalAlpha << 24) | hpColor);
        }

        // 6. SEPARATE GOLDEN ABSORPTION BAR (Slightly below the main bar)
        if (animatedAbsorption > 0.1f) {
            int absY = barY + barH + 2;
            int absH = Math.max(2, barH - 2);

            // Absorption track
            RenderUtils.drawRoundedRect(matrices, barX, absY, barW, absH, 1, (globalAlpha << 24) | 0x2B2D31);

            // Gold Absorption fill
            float absPct = MathHelper.clamp(animatedAbsorption / 20.0f, 0.0f, 1.0f);
            float absW = barW * absPct;
            if (absW > 0) {
                RenderUtils.drawRoundedRect(matrices, barX, absY, (int) absW, absH, 1, (globalAlpha << 24) | 0xFFD700);
            }
        }
    }
}
