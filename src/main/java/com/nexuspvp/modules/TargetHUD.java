package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.gui.ClickGui;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TargetHUD extends Module {

    private final NumberSetting posX = addSetting(new NumberSetting("PosX", 0, -500, 500, 5));
    private final NumberSetting posY = addSetting(new NumberSetting("PosY", 60, -400, 400, 5));
    private final NumberSetting scale = addSetting(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.05));
    private final NumberSetting opacity = addSetting(new NumberSetting("Opacity", 1.0, 0.1, 1.0, 0.05));
    private final BooleanSetting preview = addSetting(new BooleanSetting("Preview", false));

    private LivingEntity target = null;
    private long lastTargetTime = 0;

    // Dota 2 style health bar animation variables
    private float animatedHealth = 20.0f;
    private float damageGhostHealth = 20.0f;
    private float healTargetHp = 20.0f;
    private float previousTargetHealth = 20.0f;
    private long lastDamageTime = 0;
    private long lastHealTime = 0;
    private float animatedAbsorption = 0.0f;

    public TargetHUD() {
        super("TargetHUD", "Discord-styled target health and armor info", Category.PVP);
    }

    public NumberSetting getPosX() { return posX; }
    public NumberSetting getPosY() { return posY; }
    public NumberSetting getScale() { return scale; }

    public void setTarget(LivingEntity entity) {
        if (entity != null && entity != mc.player) {
            if (this.target != entity) {
                // New target acquired
                this.target = entity;
                this.animatedHealth = entity.getHealth();
                this.damageGhostHealth = entity.getHealth();
                this.previousTargetHealth = entity.getHealth();
            }
            this.lastTargetTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        // If aiming at living entity, update target
        if (mc.targetedEntity instanceof LivingEntity && mc.targetedEntity != mc.player) {
            setTarget((LivingEntity) mc.targetedEntity);
        }

        // Clean up target if dead or timed out (4s)
        if (target != null) {
            if (!target.isAlive() || System.currentTimeMillis() - lastTargetTime > 4000) {
                target = null;
            }
        }
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null) return;

        boolean isPreview = preview.isEnabled() || (mc.currentScreen instanceof ClickGui && preview.isEnabled());
        if (target == null && !isPreview) return;

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int centerX = screenW / 2 + posX.getIntValue();
        int centerY = screenH / 2 + posY.getIntValue();

        float sc = scale.getFloatValue();

        int cardW = 150;
        int cardH = 46;
        int cardX = centerX - cardW / 2;
        int cardY = centerY;

        // Card background (Discord dark theme)
        float op = opacity.getFloatValue();
        int borderCol = ((int)(0xEE * op) << 24) | 0x5865F2;
        int bgCol = ((int)(0xFA * op) << 24) | 0x1E1F22;
        RenderUtils.drawRoundedRect(matrices, cardX - 1, cardY - 1, cardW + 2, cardH + 2, 6, borderCol);
        RenderUtils.drawRoundedRect(matrices, cardX, cardY, cardW, cardH, 5, bgCol);

        String name = isPreview ? (mc.player != null ? mc.player.getName().getString() : "PlayerTarget") : (target != null ? target.getName().getString() : "Target");
        float maxHp = isPreview ? 20.0f : (target != null ? target.getMaxHealth() : 20.0f);
        
        // In preview mode simulate a periodic Dota 2 damage cycle
        float currentHp;
        if (isPreview) {
            long cycle = (System.currentTimeMillis() / 1500) % 3;
            currentHp = cycle == 0 ? 20.0f : (cycle == 1 ? 13.5f : 7.0f);
        } else {
            currentHp = target != null ? target.getHealth() : 20.0f;
        }

        float currentAbs = isPreview ? 4.0f : (target != null ? target.getAbsorptionAmount() : 0.0f);

        long now = System.currentTimeMillis();
        if (currentHp < previousTargetHealth - 0.1f) {
            damageGhostHealth = previousTargetHealth;
            lastDamageTime = now;
            healTargetHp = currentHp;
        } else if (currentHp > previousTargetHealth + 0.1f) {
            healTargetHp = currentHp;
            lastHealTime = now;
            damageGhostHealth = currentHp;
        }
        previousTargetHealth = currentHp;

        // Smooth liquid interpolation
        if (currentHp > animatedHealth) {
            animatedHealth = MathHelper.lerp(0.065f, animatedHealth, currentHp);
            if (Math.abs(currentHp - animatedHealth) < 0.05f) animatedHealth = currentHp;
        } else {
            animatedHealth = MathHelper.lerp(0.20f, animatedHealth, currentHp);
            if (Math.abs(currentHp - animatedHealth) < 0.05f) animatedHealth = currentHp;
        }
        animatedAbsorption = MathHelper.lerp(0.18f, animatedAbsorption, currentAbs);

        if (now - lastDamageTime > 280) {
            damageGhostHealth = MathHelper.lerp(0.07f, damageGhostHealth, currentHp);
        }
        if (currentHp > damageGhostHealth) {
            damageGhostHealth = currentHp;
        }

        // Draw Player Head / Avatar
        int headX = cardX + 6;
        int headY = cardY + 6;
        int headSize = 22;

        RenderUtils.drawRoundedRect(matrices, headX, headY, headSize, headSize, 3, 0xFF2B2D31);
        renderTargetFace(matrices, headX + 1, headY + 1, headSize - 2);

        // Target Name & Status
        int textX = headX + headSize + 6;
        if (mc.textRenderer.getWidth(name) > cardW - headSize - 18) {
            name = name.substring(0, Math.min(name.length(), 14)) + "...";
        }
        mc.textRenderer.drawWithShadow(matrices, name, textX, headY, 0xFFF2F3F5);

        // Health text (e.g. "14.5 / 20.0")
        String hpText = String.format("%.1f", currentHp) + " / " + String.format("%.0f", maxHp) + " \u2764";
        if (currentAbs > 0) {
            hpText += " (+" + String.format("%.1f", currentAbs) + ")";
        }
        mc.textRenderer.drawWithShadow(matrices, hpText, textX, headY + 11, currentAbs > 0 ? 0xFFFFD700 : 0xFF23A55A);

        // ==========================================
        // DOTA 2 HEALTH BAR RENDERING
        // ==========================================
        int barX = cardX + 6;
        int barY = cardY + cardH - 12;
        int barW = cardW - 12;
        int barH = 6;

        // 1. Background groove
        RenderUtils.drawRoundedRect(matrices, barX, barY, barW, barH, 2, 0xFF2B2D31);

        // 2. Dota 2 White Damage Ghost Bar (Lost HP highlighted in White!)
        float ghostPct = MathHelper.clamp(damageGhostHealth / maxHp, 0.0f, 1.0f);
        float ghostW = barW * ghostPct;
        if (ghostW > 0) {
            RenderUtils.drawRoundedRect(matrices, barX, barY, ghostW, barH, 2, 0xFFF2F3F5); // White highlight!
        }

        // 2.5. Liquid Ghost Heal Bar (Target zone being filled)
        if (healTargetHp > animatedHealth + 0.1f && now - lastHealTime < 1500) {
            float healPct = MathHelper.clamp(healTargetHp / maxHp, 0.0f, 1.0f);
            float healW = barW * healPct;
            if (healW > 0) {
                RenderUtils.drawRoundedRect(matrices, barX, barY, (int) healW, barH, 2, 0xFF57F287);
            }
        }

        // 3. Smoothly Rising Main Health Bar
        float healthPct = MathHelper.clamp(animatedHealth / maxHp, 0.0f, 1.0f);
        float fillW = barW * healthPct;
        if (fillW > 0) {
            float actualPct = MathHelper.clamp(currentHp / maxHp, 0.0f, 1.0f);
            int hpColor = actualPct > 0.6f ? 0xFF23A55A : (actualPct > 0.3f ? 0xFFFEE75C : 0xFFED4245);
            RenderUtils.drawRoundedRect(matrices, barX, barY, fillW, barH, 2, hpColor);
        }

        // 4. Golden Absorption overlay
        if (animatedAbsorption > 0) {
            float absPct = MathHelper.clamp(animatedAbsorption / 20.0f, 0.0f, 1.0f);
            float absW = barW * absPct;
            RenderUtils.drawRoundedRect(matrices, barX, barY + barH - 2, absW, 2, 1, 0xFFFFD700);
        }

        // Equipped Armor Icons Row on the right (Absolute coordinates)
        if (target != null || isPreview) {
            LivingEntity entity = isPreview ? mc.player : target;
            if (entity != null) {
                int itemX = cardX + cardW - 60;
                int itemY = cardY + 5;
                java.util.List<ItemStack> armorList = new java.util.ArrayList<>();
                for (ItemStack stack : entity.getArmorItems()) {
                    armorList.add(stack);
                }
                java.util.Collections.reverse(armorList);
                for (ItemStack stack : armorList) {
                    if (!stack.isEmpty()) {
                        mc.getItemRenderer().renderInGui(stack, itemX, itemY);
                        itemX += 13;
                    }
                }
            }
        }
    }

    private void renderTargetFace(MatrixStack matrices, int x, int y, int size) {
        Identifier skin = null;
        if (target instanceof AbstractClientPlayerEntity) {
            skin = ((AbstractClientPlayerEntity) target).getSkinTexture();
        } else if (mc.player != null) {
            skin = mc.player.getSkinTexture();
        }

        if (skin != null) {
            RenderSystem.enableBlend();
            mc.getTextureManager().bindTexture(skin);
            // Draw head (8, 8 to 16, 16 from 64x64 texture)
            DrawableHelper.drawTexture(matrices, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            // Draw hat / outer layer (40, 8 to 48, 16)
            DrawableHelper.drawTexture(matrices, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
            RenderSystem.disableBlend();
        }
    }
}