package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class Crosshair extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "Cross", "Cross", "Dot", "Circle", "None"));
    private final BooleanSetting onlyHitmarker = addSetting(new BooleanSetting("OnlyHitmarker", false));
    private final NumberSetting size = addSetting(new NumberSetting("Size", 4.0, 1.0, 20.0, 0.5));
    private final NumberSetting gap = addSetting(new NumberSetting("Gap", 2.0, 0.0, 15.0, 0.5));
    private final NumberSetting thickness = addSetting(new NumberSetting("Thickness", 1.0, 1.0, 5.0, 0.5));
    private final BooleanSetting dot = addSetting(new BooleanSetting("CenterDot", false));
    private final BooleanSetting hitmarker = addSetting(new BooleanSetting("Hitmarker", true));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 255, 230)));

    // Dynamic Attack Readiness Spread (60+ FPS)
    private final BooleanSetting dynamicSpread = addSetting(new BooleanSetting("DynamicSpread", true));
    private final NumberSetting minSpread = addSetting(new NumberSetting("MinSpread", 2.0, 0.0, 15.0, 0.5));
    private final NumberSetting maxSpread = addSetting(new NumberSetting("MaxSpread", 9.0, 2.0, 30.0, 0.5));

    // Target Acquisition & Highlighting
    private final BooleanSetting targetHighlight = addSetting(new BooleanSetting("TargetHighlight", true));
    private final BooleanSetting targetFrame = addSetting(new BooleanSetting("TargetFrame", true));
    private final ColorSetting targetColor = addSetting(new ColorSetting("TargetColor", new Color(255, 50, 60)));

    private static long lastHitTime = 0;
    private float smoothGap = 2.0f;

    public Crosshair() {
        super("Crosshair", "Custom PvP crosshair with 60+ FPS dynamic spread and target lock", Category.PVP);
    }

    public static void recordHit() {
        lastHitTime = System.currentTimeMillis();
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.options.hudHidden) return;
        if (mc.options.getPerspective() != Perspective.FIRST_PERSON) return;

        float screenW = (float) mc.getWindow().getScaledWidth();
        float screenH = (float) mc.getWindow().getScaledHeight();
        float cx = screenW / 2.0f;
        float cy = screenH / 2.0f;

        boolean isTargeting = false;
        if (mc.targetedEntity instanceof LivingEntity && mc.targetedEntity.isAlive() && mc.targetedEntity != mc.player) {
            isTargeting = true;
        } else if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity ent = ((EntityHitResult) mc.crosshairTarget).getEntity();
            if (ent instanceof LivingEntity && ent.isAlive() && ent != mc.player) {
                isTargeting = true;
            }
        }

        int c = (targetHighlight.isEnabled() && isTargeting) ?
                (targetColor.getColor().getRGB() | 0xFF000000) :
                (color.getColor().getRGB() | 0xFF000000);
        int tc = targetColor.getColor().getRGB() | 0xFF000000;

        float s = size.getFloatValue();
        float t = Math.max(1.0f, thickness.getFloatValue());
        float halfT = t / 2.0f;

        // 60+ FPS continuous frame tickDelta interpolation
        float g;
        if (dynamicSpread.isEnabled()) {
            float cd = mc.player.getAttackCooldownProgress(tickDelta);
            float minG = minSpread.getFloatValue();
            float maxG = maxSpread.getFloatValue();
            float targetG = MathHelper.lerp(cd, maxG, minG);
            smoothGap = MathHelper.lerp(0.35f, smoothGap, targetG);
            g = smoothGap;
        } else {
            g = gap.getFloatValue();
        }

        String currentStyle = style.getValue();

        if (!onlyHitmarker.isEnabled() && !currentStyle.equals("None")) {
            if (currentStyle.equals("Dot") || dot.isEnabled()) {
                RenderUtils.drawRect(matrices, cx - halfT, cy - halfT, t, t, c);
            }

            if (currentStyle.equals("Cross")) {
                // Top
                RenderUtils.drawRect(matrices, cx - halfT, cy - g - s, t, s, c);
                // Bottom
                RenderUtils.drawRect(matrices, cx - halfT, cy + g, t, s, c);
                // Left
                RenderUtils.drawRect(matrices, cx - g - s, cy - halfT, s, t, c);
                // Right
                RenderUtils.drawRect(matrices, cx + g, cy - halfT, s, t, c);
            } else if (currentStyle.equals("Circle")) {
                for (int a = 0; a < 360; a += 15) {
                    double rad = Math.toRadians(a);
                    float px = (float) (cx + Math.cos(rad) * (s + g));
                    float py = (float) (cy + Math.sin(rad) * (s + g));
                    RenderUtils.drawRect(matrices, px, py, t, t, c);
                }
            }

            // Target Acquisition Frames
            if (targetFrame.isEnabled() && isTargeting) {
                float fDist = g + s + 3.0f;
                float arm = 4.0f;

                // Corner brackets
                RenderUtils.drawRect(matrices, cx - fDist, cy - fDist, arm, 1.0f, tc);
                RenderUtils.drawRect(matrices, cx - fDist, cy - fDist, 1.0f, arm, tc);

                RenderUtils.drawRect(matrices, cx + fDist - arm, cy - fDist, arm, 1.0f, tc);
                RenderUtils.drawRect(matrices, cx + fDist - 1.0f, cy - fDist, 1.0f, arm, tc);

                RenderUtils.drawRect(matrices, cx - fDist, cy + fDist - 1.0f, arm, 1.0f, tc);
                RenderUtils.drawRect(matrices, cx - fDist, cy + fDist - arm, 1.0f, arm, tc);

                RenderUtils.drawRect(matrices, cx + fDist - arm, cy + fDist - 1.0f, arm, 1.0f, tc);
                RenderUtils.drawRect(matrices, cx + fDist - 1.0f, cy + fDist - arm, 1.0f, arm, tc);

                // Line accent ticks
                RenderUtils.drawRect(matrices, cx - halfT - 1.0f, cy - g - s - 2.0f, t + 2.0f, 1.0f, tc);
                RenderUtils.drawRect(matrices, cx - halfT - 1.0f, cy + g + s + 1.0f, t + 2.0f, 1.0f, tc);
                RenderUtils.drawRect(matrices, cx - g - s - 2.0f, cy - halfT - 1.0f, 1.0f, t + 2.0f, tc);
                RenderUtils.drawRect(matrices, cx + g + s + 1.0f, cy - halfT - 1.0f, 1.0f, t + 2.0f, tc);
            }
        }

        // Hitmarker animation
        if (hitmarker.isEnabled() && (System.currentTimeMillis() - lastHitTime < 300)) {
            float fade = 1.0f - ((System.currentTimeMillis() - lastHitTime) / 300.0f);
            int alpha = (int) (fade * 255);
            int hmColor = (alpha << 24) | 0xFF3333;
            float hmSize = 6.0f;
            float hmGap = 4.0f;

            RenderUtils.drawRect(matrices, cx - hmGap - hmSize, cy - hmGap - hmSize, hmSize, 1.0f, hmColor);
            RenderUtils.drawRect(matrices, cx - hmGap - 1.0f, cy - hmGap - hmSize, 1.0f, hmSize, hmColor);

            RenderUtils.drawRect(matrices, cx + hmGap, cy - hmGap - hmSize, hmSize, 1.0f, hmColor);
            RenderUtils.drawRect(matrices, cx + hmGap + hmSize - 1.0f, cy - hmGap - hmSize, 1.0f, hmSize, hmColor);

            RenderUtils.drawRect(matrices, cx - hmGap - hmSize, cy + hmGap + hmSize - 1.0f, hmSize, 1.0f, hmColor);
            RenderUtils.drawRect(matrices, cx - hmGap - 1.0f, cy + hmGap, 1.0f, hmSize, hmColor);

            RenderUtils.drawRect(matrices, cx + hmGap, cy + hmGap + hmSize - 1.0f, hmSize, 1.0f, hmColor);
            RenderUtils.drawRect(matrices, cx + hmGap + hmSize - 1.0f, cy + hmGap, 1.0f, hmSize, hmColor);
        }
    }
}
