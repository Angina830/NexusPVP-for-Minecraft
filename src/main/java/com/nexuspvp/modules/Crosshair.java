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

    // Dynamic Attack Readiness Spread
    private final BooleanSetting dynamicSpread = addSetting(new BooleanSetting("DynamicSpread", true));
    private final NumberSetting minSpread = addSetting(new NumberSetting("MinSpread", 2.0, 0.0, 15.0, 0.5));
    private final NumberSetting maxSpread = addSetting(new NumberSetting("MaxSpread", 9.0, 2.0, 30.0, 0.5));

    // Target Acquisition & Highlighting
    private final BooleanSetting targetHighlight = addSetting(new BooleanSetting("TargetHighlight", true));
    private final BooleanSetting targetFrame = addSetting(new BooleanSetting("TargetFrame", true));
    private final ColorSetting targetColor = addSetting(new ColorSetting("TargetColor", new Color(255, 50, 60)));

    private static long lastHitTime = 0;

    public Crosshair() {
        super("Crosshair", "Custom PvP crosshair with dynamic attack spread and target lock", Category.PVP);
    }

    public static void recordHit() {
        lastHitTime = System.currentTimeMillis();
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.options.hudHidden) return;
        if (mc.options.getPerspective() != Perspective.FIRST_PERSON) return;

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int cx = screenW / 2;
        int cy = screenH / 2;

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

        // Active color
        int c = (targetHighlight.isEnabled() && isTargeting) ?
                (targetColor.getColor().getRGB() | 0xFF000000) :
                (color.getColor().getRGB() | 0xFF000000);
        int tc = targetColor.getColor().getRGB() | 0xFF000000;

        int s = (int) Math.round(size.getValue());
        int t = (int) Math.max(1, Math.round(thickness.getValue()));

        // Dynamic spread calculation
        int g;
        if (dynamicSpread.isEnabled()) {
            float cd = mc.player.getAttackCooldownProgress(0.0f);
            float minG = minSpread.getFloatValue();
            float maxG = maxSpread.getFloatValue();
            g = (int) Math.round(MathHelper.lerp(cd, maxG, minG));
        } else {
            g = (int) Math.round(gap.getValue());
        }

        String currentStyle = style.getValue();

        if (!onlyHitmarker.isEnabled() && !currentStyle.equals("None")) {
            if (currentStyle.equals("Dot") || dot.isEnabled()) {
                RenderUtils.drawRect(matrices, cx, cy, t, t, c);
            }

            if (currentStyle.equals("Cross")) {
                // Top
                RenderUtils.drawRect(matrices, cx, cy - g - s, t, s, c);
                // Bottom
                RenderUtils.drawRect(matrices, cx, cy + g + 1, t, s, c);
                // Left
                RenderUtils.drawRect(matrices, cx - g - s, cy, s, t, c);
                // Right
                RenderUtils.drawRect(matrices, cx + g + 1, cy, s, t, c);
            } else if (currentStyle.equals("Circle")) {
                for (int a = 0; a < 360; a += 15) {
                    double rad = Math.toRadians(a);
                    int px = (int) Math.round(cx + Math.cos(rad) * (s + g));
                    int py = (int) Math.round(cy + Math.sin(rad) * (s + g));
                    RenderUtils.drawRect(matrices, px, py, t, t, c);
                }
            }

            // Target Acquisition Frames / Brackets
            if (targetFrame.isEnabled() && isTargeting) {
                int fDist = g + s + 3;
                int arm = 4;

                // Corner brackets
                RenderUtils.drawRect(matrices, cx - fDist, cy - fDist, arm, 1, tc);
                RenderUtils.drawRect(matrices, cx - fDist, cy - fDist, 1, arm, tc);

                RenderUtils.drawRect(matrices, cx + fDist - arm + 1, cy - fDist, arm, 1, tc);
                RenderUtils.drawRect(matrices, cx + fDist, cy - fDist, 1, arm, tc);

                RenderUtils.drawRect(matrices, cx - fDist, cy + fDist, arm, 1, tc);
                RenderUtils.drawRect(matrices, cx - fDist, cy + fDist - arm + 1, 1, arm, tc);

                RenderUtils.drawRect(matrices, cx + fDist - arm + 1, cy + fDist, arm, 1, tc);
                RenderUtils.drawRect(matrices, cx + fDist, cy + fDist - arm + 1, 1, arm, tc);

                // Line accent ticks
                RenderUtils.drawRect(matrices, cx - 1, cy - g - s - 2, t + 2, 1, tc);
                RenderUtils.drawRect(matrices, cx - 1, cy + g + s + 1, t + 2, 1, tc);
                RenderUtils.drawRect(matrices, cx - g - s - 2, cy - 1, 1, t + 2, tc);
                RenderUtils.drawRect(matrices, cx + g + s + 1, cy - 1, 1, t + 2, tc);
            }
        }

        // Hitmarker animation
        if (hitmarker.isEnabled() && (System.currentTimeMillis() - lastHitTime < 300)) {
            float fade = 1.0f - ((System.currentTimeMillis() - lastHitTime) / 300.0f);
            int alpha = (int) (fade * 255);
            int hmColor = (alpha << 24) | 0xFF3333;
            int hmSize = 6;
            int hmGap = 4;

            RenderUtils.drawRect(matrices, cx - hmGap - hmSize, cy - hmGap - hmSize, hmSize, 1, hmColor);
            RenderUtils.drawRect(matrices, cx - hmGap - 1, cy - hmGap - hmSize, 1, hmSize, hmColor);

            RenderUtils.drawRect(matrices, cx + hmGap, cy - hmGap - hmSize, hmSize, 1, hmColor);
            RenderUtils.drawRect(matrices, cx + hmGap + hmSize - 1, cy - hmGap - hmSize, 1, hmSize, hmColor);

            RenderUtils.drawRect(matrices, cx - hmGap - hmSize, cy + hmGap + hmSize - 1, hmSize, 1, hmColor);
            RenderUtils.drawRect(matrices, cx - hmGap - 1, cy + hmGap, 1, hmSize, hmColor);

            RenderUtils.drawRect(matrices, cx + hmGap, cy + hmGap + hmSize - 1, hmSize, 1, hmColor);
            RenderUtils.drawRect(matrices, cx + hmGap + hmSize - 1, cy + hmGap, 1, hmSize, hmColor);
        }
    }
}
