package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class Crosshair extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "Cross", "Cross", "Dot", "Circle"));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 255, 230)));
    private final NumberSetting size = addSetting(new NumberSetting("Size", 4.0, 1.0, 20.0, 0.5));
    private final NumberSetting gap = addSetting(new NumberSetting("Gap", 2.0, 0.0, 15.0, 0.5));
    private final NumberSetting thickness = addSetting(new NumberSetting("Thickness", 1.0, 1.0, 5.0, 0.5));
    private final BooleanSetting dot = addSetting(new BooleanSetting("Dot", false));
    private final BooleanSetting hitmarker = addSetting(new BooleanSetting("Hitmarker", true));

    // Dynamic Attack Readiness Spread (60+ FPS Smooth)
    private final BooleanSetting dynamicSpread = addSetting(new BooleanSetting("DynamicSpread", true));
    private final NumberSetting minSpread = addSetting(new NumberSetting("MinSpread", 2.0, 0.0, 15.0, 0.5));
    private final NumberSetting maxSpread = addSetting(new NumberSetting("MaxSpread", 9.0, 2.0, 30.0, 0.5));

    // Target Acquisition & Highlighting
    private final BooleanSetting targetHighlight = addSetting(new BooleanSetting("TargetHighlight", true));
    private final BooleanSetting targetFrame = addSetting(new BooleanSetting("TargetFrame", true));
    private final ColorSetting targetColor = addSetting(new ColorSetting("TargetColor", new Color(255, 50, 60)));

    private static long staticHitTime = 0;
    private float smoothGap = 2.0f;

    public Crosshair() {
        super("Crosshair", "Custom PvP crosshair with 60+ FPS dynamic spread and target lock", Category.RENDER);
    }

    public static void recordHit() {
        staticHitTime = System.currentTimeMillis();
    }

    public void renderCustomCrosshair(DrawContext context, float tickDelta) {
        if (mc.player == null || mc.options.hudHidden) return;
        if (mc.options.getPerspective() != Perspective.FIRST_PERSON) return;

        float screenW = mc.getWindow().getScaledWidth();
        float screenH = mc.getWindow().getScaledHeight();
        float cx = screenW / 2.0f;
        float cy = screenH / 2.0f;

        // Check if player is aiming at an attackable living entity
        boolean isTargeting = false;
        if (mc.targetedEntity instanceof LivingEntity && mc.targetedEntity.isAlive() && mc.targetedEntity != mc.player) {
            isTargeting = true;
        } else if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity ent = ((EntityHitResult) mc.crosshairTarget).getEntity();
            if (ent instanceof LivingEntity && ent.isAlive() && ent != mc.player) {
                isTargeting = true;
            }
        }

        // Colors
        int c = (targetHighlight.isEnabled() && isTargeting) ?
                (targetColor.getColor().getRGB() | 0xFF000000) :
                (color.getColor().getRGB() | 0xFF000000);
        int tc = targetColor.getColor().getRGB() | 0xFF000000;

        float s = size.getFloatValue();
        float t = Math.max(1.0f, thickness.getFloatValue());
        float halfT = t / 2.0f;

        // 60+ FPS continuous tickDelta sub-tick interpolation for butter-smooth animation
        float g;
        if (dynamicSpread.isEnabled()) {
            float cd = mc.player.getAttackCooldownProgress(tickDelta); // Exact frame delta!
            float minG = minSpread.getFloatValue();
            float maxG = maxSpread.getFloatValue();
            float targetG = MathHelper.lerp(cd, maxG, minG);
            smoothGap = MathHelper.lerp(0.35f, smoothGap, targetG);
            g = smoothGap;
        } else {
            g = gap.getFloatValue();
        }

        MatrixStack matrices = context.getMatrices();
        String mode = style.getValue();

        if (mode.equalsIgnoreCase("Dot")) {
            float d = Math.max(2.0f, t * 2.0f);
            RenderUtils.drawQuad(matrices, cx - d / 2.0f, cy - d / 2.0f, cx + d / 2.0f, cy + d / 2.0f, c);
        } else if (mode.equalsIgnoreCase("Circle")) {
            for (int a = 0; a < 360; a += 15) {
                double rad = Math.toRadians(a);
                float px = (float) (cx + Math.cos(rad) * (s + g));
                float py = (float) (cy + Math.sin(rad) * (s + g));
                RenderUtils.drawQuad(matrices, px, py, px + t, py + t, c);
            }
        } else { // Cross style with sub-pixel float hardware quads
            // Top branch
            RenderUtils.drawQuad(matrices, cx - halfT, cy - g - s, cx + halfT, cy - g, c);
            // Bottom branch
            RenderUtils.drawQuad(matrices, cx - halfT, cy + g, cx + halfT, cy + g + s, c);
            // Left branch
            RenderUtils.drawQuad(matrices, cx - g - s, cy - halfT, cx - g, cy + halfT, c);
            // Right branch
            RenderUtils.drawQuad(matrices, cx + g, cy - halfT, cx + g + s, cy + halfT, c);

            if (dot.isEnabled()) {
                RenderUtils.drawQuad(matrices, cx - halfT, cy - halfT, cx + halfT, cy + halfT, c);
            }
        }

        // Target Acquisition Tactical Frames
        if (targetFrame.isEnabled() && isTargeting) {
            float fDist = g + s + 3.0f;
            float arm = 4.0f;

            // Corner brackets
            RenderUtils.drawQuad(matrices, cx - fDist, cy - fDist, cx - fDist + arm, cy - fDist + 1.0f, tc);
            RenderUtils.drawQuad(matrices, cx - fDist, cy - fDist, cx - fDist + 1.0f, cy - fDist + arm, tc);

            RenderUtils.drawQuad(matrices, cx + fDist - arm, cy - fDist, cx + fDist, cy - fDist + 1.0f, tc);
            RenderUtils.drawQuad(matrices, cx + fDist - 1.0f, cy - fDist, cx + fDist, cy - fDist + arm, tc);

            RenderUtils.drawQuad(matrices, cx - fDist, cy + fDist - 1.0f, cx - fDist + arm, cy + fDist, tc);
            RenderUtils.drawQuad(matrices, cx - fDist, cy + fDist - arm, cx - fDist + 1.0f, cy + fDist, tc);

            RenderUtils.drawQuad(matrices, cx + fDist - arm, cy + fDist - 1.0f, cx + fDist, cy + fDist, tc);
            RenderUtils.drawQuad(matrices, cx + fDist - 1.0f, cy + fDist - arm, cx + fDist, cy + fDist, tc);

            // Line accent markers
            RenderUtils.drawQuad(matrices, cx - halfT - 1.0f, cy - g - s - 2.0f, cx + halfT + 1.0f, cy - g - s - 1.0f, tc);
            RenderUtils.drawQuad(matrices, cx - halfT - 1.0f, cy + g + s + 1.0f, cx + halfT + 1.0f, cy + g + s + 2.0f, tc);
            RenderUtils.drawQuad(matrices, cx - g - s - 2.0f, cy - halfT - 1.0f, cx - g - s - 1.0f, cy + halfT + 1.0f, tc);
            RenderUtils.drawQuad(matrices, cx + g + s + 1.0f, cy - halfT - 1.0f, cx + g + s + 2.0f, cy + halfT + 1.0f, tc);
        }

        // Hitmarker animation
        if (hitmarker.isEnabled()) {
            long elapsed = System.currentTimeMillis() - staticHitTime;
            if (elapsed < 300) {
                float alpha = 1.0f - (elapsed / 300.0f);
                int hmColor = new Color(255, 50, 50, (int) (alpha * 255)).getRGB();
                float hms = 5.0f;
                for (float i = 2.0f; i <= hms; i += 1.0f) {
                    RenderUtils.drawQuad(matrices, cx - i, cy - i, cx - i + 1.0f, cy - i + 1.0f, hmColor);
                    RenderUtils.drawQuad(matrices, cx + i, cy - i, cx + i + 1.0f, cy - i + 1.0f, hmColor);
                    RenderUtils.drawQuad(matrices, cx - i, cy + i, cx - i + 1.0f, cy + i + 1.0f, hmColor);
                    RenderUtils.drawQuad(matrices, cx + i, cy + i, cx + i + 1.0f, cy + i + 1.0f, hmColor);
                }
            }
        }
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {}
}
