package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.gui.ClickGui;
import com.nexuspvp.gui.styles.ClassicGuiScreen;
import com.nexuspvp.gui.styles.CompactListScreen;
import com.nexuspvp.gui.styles.GlassDashboardScreen;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

public class TargetHUD extends Module {

    private final NumberSetting posX = addSetting(new NumberSetting("PosX", 0, -500, 500, 5));
    private final NumberSetting posY = addSetting(new NumberSetting("PosY", 60, -400, 400, 5));
    private final NumberSetting scale = addSetting(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.05));
    private final BooleanSetting preview = addSetting(new BooleanSetting("Preview", true));

    private LivingEntity target = null;
    private long lastTargetTime = 0;

    private float animatedHealth = 20.0f;
    private float damageGhostHealth = 20.0f;
    private float previousTargetHealth = 20.0f;
    private long lastDamageTime = 0;
    private float animatedAbsorption = 0.0f;

    // Zero-allocation armor cache
    private final ItemStack[] armorCache = new ItemStack[4];

    public TargetHUD() {
        super("TargetHUD", "Discord-styled target health and armor info", Category.PVP);
        setEnabled(true);
    }

    public NumberSetting getPosX() { return posX; }
    public NumberSetting getPosY() { return posY; }
    public NumberSetting getScale() { return scale; }

    public void setTarget(LivingEntity entity) {
        if (entity != null && entity != mc.player) {
            if (this.target != entity) {
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

        if (mc.targetedEntity instanceof LivingEntity && mc.targetedEntity != mc.player) {
            setTarget((LivingEntity) mc.targetedEntity);
        } else if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity ent = ((EntityHitResult) mc.crosshairTarget).getEntity();
            if (ent instanceof LivingEntity && ent != mc.player) {
                setTarget((LivingEntity) ent);
            }
        }

        if (target != null) {
            if (!target.isAlive() || System.currentTimeMillis() - lastTargetTime > 5000) {
                target = null;
            }
        }
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null) return;

        boolean isGuiOpen = mc.currentScreen instanceof ClickGui ||
                            mc.currentScreen instanceof ClassicGuiScreen ||
                            mc.currentScreen instanceof GlassDashboardScreen ||
                            mc.currentScreen instanceof CompactListScreen;

        boolean isPreview = (preview.isEnabled() && isGuiOpen) || (target == null && isGuiOpen);
        if (target == null && !isPreview) return;

        int screenW = Compat.getScaledWidth();
        int screenH = Compat.getScaledHeight();
        int centerX = screenW / 2 + posX.getIntValue();
        int centerY = screenH / 2 + posY.getIntValue();

        float sc = scale.getFloatValue();

        int cardW = 150;
        int cardH = 46;
        int cardX = centerX - cardW / 2;
        int cardY = centerY;

        matrices.push();
        if (sc != 1.0f) {
            matrices.translate(centerX, centerY, 0);
            matrices.scale(sc, sc, 1.0f);
            matrices.translate(-centerX, -centerY, 0);
        }

        RenderUtils.drawRoundedRect(matrices, cardX - 1, cardY - 1, cardW + 2, cardH + 2, 6, 0xEE5865F2);
        RenderUtils.drawRoundedRect(matrices, cardX, cardY, cardW, cardH, 5, 0xFA1E1F22);

        String name = isPreview ? (mc.player != null ? mc.player.getName().getString() : "PlayerTarget") : (target != null ? target.getName().getString() : "Target");
        float maxHp = isPreview ? 20.0f : (target != null ? target.getMaxHealth() : 20.0f);
        
        float currentHp;
        if (isPreview) {
            long cycle = (System.currentTimeMillis() / 1500) % 3;
            currentHp = cycle == 0 ? 20.0f : (cycle == 1 ? 13.5f : 7.0f);
        } else {
            currentHp = target != null ? target.getHealth() : 20.0f;
        }

        float currentAbs = isPreview ? 4.0f : (target != null ? target.getAbsorptionAmount() : 0.0f);

        if (currentHp < previousTargetHealth) {
            damageGhostHealth = previousTargetHealth;
            lastDamageTime = System.currentTimeMillis();
        }
        previousTargetHealth = currentHp;

        animatedHealth = MathHelper.lerp(0.18f, animatedHealth, currentHp);
        animatedAbsorption = MathHelper.lerp(0.18f, animatedAbsorption, currentAbs);

        if (System.currentTimeMillis() - lastDamageTime > 280) {
            damageGhostHealth = MathHelper.lerp(0.08f, damageGhostHealth, currentHp);
        }
        if (currentHp > damageGhostHealth) {
            damageGhostHealth = currentHp;
        }

        int headX = cardX + 6;
        int headY = cardY + 6;
        int headSize = 22;

        RenderUtils.drawRoundedRect(matrices, headX, headY, headSize, headSize, 3, 0xFF2B2D31);
        renderTargetFace(matrices, headX + 1, headY + 1, headSize - 2);

        int textX = headX + headSize + 6;
        if (name.length() > 14) {
            name = name.substring(0, 12) + "..";
        }
        Compat.drawText(matrices, name, textX, headY, 0xFFF2F3F5);

        String hpText = String.format("%.1f", currentHp) + " / " + String.format("%.0f", maxHp) + " \u2764";
        if (currentAbs > 0) {
            hpText += " (+" + String.format("%.1f", currentAbs) + ")";
        }
        Compat.drawText(matrices, hpText, textX, headY + 11, currentAbs > 0 ? 0xFFFFD700 : 0xFF23A55A);

        int barX = cardX + 6;
        int barY = cardY + cardH - 12;
        int barW = cardW - 12;
        int barH = 6;

        RenderUtils.drawRoundedRect(matrices, barX, barY, barW, barH, 2, 0xFF2B2D31);

        float ghostPct = MathHelper.clamp(damageGhostHealth / maxHp, 0.0f, 1.0f);
        float ghostW = barW * ghostPct;
        if (ghostW > 0) {
            RenderUtils.drawRoundedRect(matrices, barX, barY, ghostW, barH, 2, 0xFFF2F3F5);
        }

        float healthPct = MathHelper.clamp(animatedHealth / maxHp, 0.0f, 1.0f);
        float fillW = barW * healthPct;
        if (fillW > 0) {
            int hpColor = healthPct > 0.6f ? 0xFF23A55A : (healthPct > 0.3f ? 0xFFFEE75C : 0xFFED4245);
            RenderUtils.drawRoundedRect(matrices, barX, barY, fillW, barH, 2, hpColor);
        }

        if (animatedAbsorption > 0) {
            float absPct = MathHelper.clamp(animatedAbsorption / 20.0f, 0.0f, 1.0f);
            float absW = barW * absPct;
            RenderUtils.drawRoundedRect(matrices, barX, barY + barH - 2, absW, 2, 1, 0xFFFFD700);
        }

        // Equipped Armor Icons Row (Zero-allocation reverse array: Helmet -> Chest -> Legs -> Boots)
        if (target != null || isPreview) {
            LivingEntity entity = isPreview ? mc.player : target;
            if (entity != null) {
                int itemX = cardX + cardW - 55;
                int itemY = cardY + 5;
                int slotIdx = 0;
                for (ItemStack stack : entity.getArmorItems()) {
                    if (slotIdx < 4) {
                        armorCache[slotIdx] = stack;
                        slotIdx++;
                    }
                }
                for (int i = slotIdx - 1; i >= 0; i--) {
                    ItemStack st = armorCache[i];
                    if (st != null && !st.isEmpty()) {
                        Compat.drawItem(matrices, st, itemX, itemY);
                        itemX += 13;
                    }
                }
            }
        }

        matrices.pop();
    }

    private void renderTargetFace(MatrixStack matrices, int x, int y, int size) {
        Identifier skin = null;
        if (target instanceof AbstractClientPlayerEntity) {
            skin = ((AbstractClientPlayerEntity) target).getSkinTextures().texture();
        } else if (mc.player != null) {
            skin = mc.player.getSkinTextures().texture();
        }

        if (skin != null) {
            Compat.drawSkinHead(matrices, skin, x, y, size);
        }
    }
}
