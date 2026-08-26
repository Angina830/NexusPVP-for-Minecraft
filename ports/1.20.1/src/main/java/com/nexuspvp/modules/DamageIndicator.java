package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.gui.ClickGui;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DamageIndicator extends Module {

    private final BooleanSetting onlyMyDamage = addSetting(new BooleanSetting("OnlyMyDamage", true));
    private final BooleanSetting screenDisplay = addSetting(new BooleanSetting("ScreenDisplay", true));
    private final NumberSetting screenNumbersOpacity = addSetting(new NumberSetting("ScreenNumbersOpacity", 100, 10, 100, 5));
    private final NumberSetting screenNumbersScale = addSetting(new NumberSetting("ScreenNumbersScale", 1.0, 0.5, 3.0, 0.1));
    private final BooleanSetting showCombo = addSetting(new BooleanSetting("ShowCombo", true));
    private final NumberSetting comboOpacity = addSetting(new NumberSetting("ComboOpacity", 100, 10, 100, 5));
    private final NumberSetting comboOffsetX = addSetting(new NumberSetting("ComboOffsetX", 0, -300, 300, 5));
    private final NumberSetting comboOffsetY = addSetting(new NumberSetting("ComboOffsetY", -34, -250, 250, 5));
    private final BooleanSetting previewCombo = addSetting(new BooleanSetting("PreviewCombo", false));
    private final BooleanSetting worldNumbers = addSetting(new BooleanSetting("WorldNumbers", true));
    private final BooleanSetting hearts = addSetting(new BooleanSetting("Hearts", true));
    private final BooleanSetting critIndicator = addSetting(new BooleanSetting("CritIndicator", true));
    private final BooleanSetting bloom = addSetting(new BooleanSetting("Bloom", true));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 60, 60)));
    private final NumberSetting size = addSetting(new NumberSetting("Size", 1.2, 0.5, 3.0, 0.1));

    private final Map<Integer, Float> trackedHealth = new HashMap<>();
    private final List<WorldDamagePopup> worldPopups = new ArrayList<>();
    private final List<ScreenDamagePopup> screenPopups = new ArrayList<>();
    private static final Map<Integer, Long> myAttacks = new ConcurrentHashMap<>();

    private int comboHits = 0;
    private float comboDamage = 0;
    private long lastHitTime = 0;
    private float comboScaleAnim = 1.0f;

    public DamageIndicator() {
        super("DamageIndicator", "Displays dealt damage on screen and world", Category.PVP);
    }

    public NumberSetting getComboOffsetX() { return comboOffsetX; }
    public NumberSetting getComboOffsetY() { return comboOffsetY; }

    public static void recordAttack(int entityId) {
        myAttacks.put(entityId, System.currentTimeMillis());
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (now - lastHitTime > 2600) {
            comboHits = 0;
            comboDamage = 0;
        }

        myAttacks.entrySet().removeIf(entry -> now - entry.getValue() > 1500);

        for (WorldDamagePopup wp : worldPopups) {
            wp.update();
        }
        worldPopups.removeIf(WorldDamagePopup::isDead);

        for (Entity e : mc.world.getEntities()) {
            if (e instanceof LivingEntity && e != mc.player) {
                LivingEntity living = (LivingEntity) e;
                float currentHealth = living.getHealth() + living.getAbsorptionAmount();
                Float prev = trackedHealth.get(e.getEntityId());

                if (prev != null && currentHealth < prev) {
                    float damage = prev - currentHealth;
                    
                    Long attackTime = myAttacks.get(e.getEntityId());
                    boolean wasHitByMe = attackTime != null && (now - attackTime <= 800);

                    if (onlyMyDamage.isEnabled() && !wasHitByMe) {
                        trackedHealth.put(e.getEntityId(), currentHealth);
                        continue;
                    }

                    boolean isCrit = wasHitByMe && mc.player.fallDistance > 0.0f && !mc.player.isOnGround() && !mc.player.isClimbing() && !mc.player.isTouchingWater();

                    if (wasHitByMe) {
                        comboHits++;
                        comboDamage += damage;
                        lastHitTime = now;
                        comboScaleAnim = 1.45f;
                    }

                    if (worldNumbers.isEnabled()) {
                        double posX = living.getX();
                        double posY = living.getY() + living.getHeight() / 2.0;
                        double posZ = living.getZ();
                        worldPopups.add(new WorldDamagePopup(posX, posY, posZ, damage, isCrit));
                    }

                    if (screenDisplay.isEnabled()) {
                        screenPopups.add(new ScreenDamagePopup(damage, isCrit));
                    }
                }

                trackedHealth.put(e.getEntityId(), currentHealth);
            }
        }

        comboScaleAnim += (1.0f - comboScaleAnim) * 0.15f;
        screenPopups.removeIf(ScreenDamagePopup::isDead);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (!worldNumbers.isEnabled() || worldPopups.isEmpty() || mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        float baseScale = size.getFloatValue() * 0.022f;

        for (WorldDamagePopup popup : worldPopups) {
            float alpha = popup.getAlpha();
            if (alpha <= 0.02f) continue;

            matrices.push();
            matrices.translate(popup.x - camPos.x, popup.y - camPos.y, popup.z - camPos.z);
            matrices.multiply(camera.getRotation());

            float springScale = baseScale * popup.getSpringScale();
            matrices.scale(-springScale, -springScale, springScale);

            String text = String.format("-%.1f", popup.damage);
            if (popup.isCrit) text = "CRIT " + text;
            if (hearts.isEnabled()) text += " ❤";

            int textColor = popup.isCrit ? 0xFFFF3333 : color.getColor().getRGB();
            int alphaInt = (int) (alpha * 255);
            int finalColor = (alphaInt << 24) | (textColor & 0x00FFFFFF);

            int tw = mc.textRenderer.getWidth(text);
            RenderUtils.drawRoundedRect(matrices, -tw / 2 - 3, -5, tw + 6, 12, 3, (alphaInt / 2 << 24) | 0x00111214);
            mc.textRenderer.drawWithShadow(matrices, text, -tw / 2, -3, finalColor);

            matrices.pop();
        }
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null) return;

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        if (screenDisplay.isEnabled() && !screenPopups.isEmpty()) {
            int cx = screenW / 2;
            int cy = screenH / 2 - 50;
            float sc = screenNumbersScale.getFloatValue();
            float baseA = screenNumbersOpacity.getFloatValue() / 100.0f;

            for (ScreenDamagePopup sp : screenPopups) {
                float a = sp.getAlpha() * baseA;
                if (a <= 0.02f) continue;

                matrices.push();
                matrices.translate(cx + sp.xOff, cy + sp.yOff, 0);
                matrices.scale(sc, sc, 1.0f);

                String text = String.format("-%.1f", sp.damage);
                if (sp.isCrit) text = "CRIT " + text;

                int col = sp.isCrit ? 0xFFFF3333 : color.getColor().getRGB();
                int finalColor = ((int) (a * 255) << 24) | (col & 0x00FFFFFF);

                int tw = mc.textRenderer.getWidth(text);
                mc.textRenderer.drawWithShadow(matrices, text, -tw / 2, 0, finalColor);
                matrices.pop();
            }
        }

        boolean showPreview = previewCombo.isEnabled() && mc.currentScreen instanceof ClickGui;
        if ((showCombo.isEnabled() && comboHits > 0) || showPreview) {
            int hits = showPreview ? 5 : comboHits;
            float dmg = showPreview ? 42.5f : comboDamage;

            int cx = screenW / 2 + comboOffsetX.getIntValue();
            int cy = screenH / 2 + comboOffsetY.getIntValue();
            float a = comboOpacity.getFloatValue() / 100.0f;

            matrices.push();
            matrices.translate(cx, cy, 0);
            matrices.scale(comboScaleAnim, comboScaleAnim, 1.0f);

            String comboText = hits + " HITS";
            String dmgText = String.format("%.1f DMG", dmg);

            int w1 = mc.textRenderer.getWidth(comboText);
            int w2 = mc.textRenderer.getWidth(dmgText);
            int boxW = Math.max(w1, w2) + 16;
            int boxH = 24;

            RenderUtils.drawRoundedRect(matrices, -boxW / 2, -boxH / 2, boxW, boxH, 4, ((int) (a * 220) << 24) | 0x001E1F22);
            mc.textRenderer.drawWithShadow(matrices, comboText, -w1 / 2, -boxH / 2 + 4, ((int) (a * 255) << 24) | 0x00FFDD55);
            mc.textRenderer.drawWithShadow(matrices, dmgText, -w2 / 2, -boxH / 2 + 13, ((int) (a * 255) << 24) | 0x00FF4444);

            matrices.pop();
        }
    }

    private static class WorldDamagePopup {
        double x, y, z;
        double vx, vy, vz;
        final float damage;
        final boolean isCrit;
        final long spawnTime;
        final long maxLifetime = 900;

        WorldDamagePopup(double x, double y, double z, float damage, boolean isCrit) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.damage = damage;
            this.isCrit = isCrit;
            this.spawnTime = System.currentTimeMillis();

            Random r = new Random();
            this.vx = (r.nextDouble() - 0.5) * 0.035;
            this.vy = 0.065 + r.nextDouble() * 0.025;
            this.vz = (r.nextDouble() - 0.5) * 0.035;
        }

        void update() {
            x += vx;
            y += vy;
            z += vz;
            vy -= 0.0032;
            vx *= 0.95;
            vz *= 0.95;
        }

        float getSpringScale() {
            long elapsed = System.currentTimeMillis() - spawnTime;
            float progress = Math.min(1.0f, (float) elapsed / (float) maxLifetime);
            return 1.0f + 0.35f * (float) Math.sin(progress * Math.PI);
        }

        float getAlpha() {
            long elapsed = System.currentTimeMillis() - spawnTime;
            if (elapsed > maxLifetime) return 0.0f;
            return 1.0f - ((float) elapsed / (float) maxLifetime);
        }

        boolean isDead() {
            return System.currentTimeMillis() - spawnTime >= maxLifetime;
        }
    }

    private static class ScreenDamagePopup {
        final float damage;
        final boolean isCrit;
        final long spawnTime;
        final float xOff;
        float yOff = 0;
        final long maxLifetime = 700;

        ScreenDamagePopup(float damage, boolean isCrit) {
            this.damage = damage;
            this.isCrit = isCrit;
            this.spawnTime = System.currentTimeMillis();
            Random r = new Random();
            this.xOff = (r.nextFloat() - 0.5f) * 40.0f;
        }

        float getAlpha() {
            long elapsed = System.currentTimeMillis() - spawnTime;
            if (elapsed > maxLifetime) return 0.0f;
            yOff -= 0.6f;
            return 1.0f - ((float) elapsed / (float) maxLifetime);
        }

        boolean isDead() {
            return System.currentTimeMillis() - spawnTime >= maxLifetime;
        }
    }
}
