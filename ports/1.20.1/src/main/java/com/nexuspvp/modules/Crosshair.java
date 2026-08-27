package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
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

    // Dynamic Attack Readiness Spread
    private final BooleanSetting dynamicSpread = addSetting(new BooleanSetting("DynamicSpread", true));
    private final NumberSetting minSpread = addSetting(new NumberSetting("MinSpread", 2.0, 0.0, 15.0, 0.5));
    private final NumberSetting maxSpread = addSetting(new NumberSetting("MaxSpread", 9.0, 2.0, 30.0, 0.5));

    // Target Acquisition & Highlighting
    private final BooleanSetting targetHighlight = addSetting(new BooleanSetting("TargetHighlight", true));
    private final BooleanSetting targetFrame = addSetting(new BooleanSetting("TargetFrame", true));
    private final ColorSetting targetColor = addSetting(new ColorSetting("TargetColor", new Color(255, 50, 60)));

    private static long staticHitTime = 0;

    public Crosshair() {
        super("Crosshair", "Custom PvP crosshair with dynamic attack spread and target lock", Category.RENDER);
    }

    public static void recordHit() {
        staticHitTime = System.currentTimeMillis();
    }

    public void renderCustomCrosshair(DrawContext context) {
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

        // Active color (highlighted if aiming at target)
        int c = (targetHighlight.isEnabled() && isTargeting) ?
                (targetColor.getColor().getRGB() | 0xFF000000) :
                (color.getColor().getRGB() | 0xFF000000);
        int tc = targetColor.getColor().getRGB() | 0xFF000000;

        int s = (int) Math.round(size.getValue());
        int t = (int) Math.max(1, Math.round(thickness.getValue()));

        // Dynamic attack spread calculation
        int g;
        if (dynamicSpread.isEnabled()) {
            float cd = mc.player.getAttackCooldownProgress(0.0f); // 0.0 (just attacked) -> 1.0 (fully charged)
            float minG = minSpread.getFloatValue();
            float maxG = maxSpread.getFloatValue();
            // When cd is 0 (not ready), gap is maxG. When cd is 1 (ready), gap is minG.
            g = (int) Math.round(MathHelper.lerp(cd, maxG, minG));
        } else {
            g = (int) Math.round(gap.getValue());
        }

        String mode = style.getValue();
        if (mode.equalsIgnoreCase("Dot")) {
            int d = Math.max(2, t * 2);
            context.fill(cx - d / 2, cy - d / 2, cx + (d + 1) / 2, cy + (d + 1) / 2, c);
        } else if (mode.equalsIgnoreCase("Circle")) {
            for (int a = 0; a < 360; a += 15) {
                double rad = Math.toRadians(a);
                int px = (int) Math.round(cx + Math.cos(rad) * (s + g));
                int py = (int) Math.round(cy + Math.sin(rad) * (s + g));
                context.fill(px, py, px + t, py + t, c);
            }
        } else { // Cross style
            // Top branch
            context.fill(cx, cy - g - s, cx + t, cy - g, c);
            // Bottom branch
            context.fill(cx, cy + g + 1, cx + t, cy + g + s + 1, c);
            // Left branch
            context.fill(cx - g - s, cy, cx - g, cy + t, c);
            // Right branch
            context.fill(cx + g + 1, cy, cx + g + s + 1, cy + t, c);

            if (dot.isEnabled()) {
                context.fill(cx, cy, cx + t, cy + t, c);
            }
        }

        // Target Acquisition Frames / Brackets along lines
        if (targetFrame.isEnabled() && isTargeting) {
            int fDist = g + s + 3;
            int arm = 4;

            // 1. Sleek tactical corner brackets [ ┌ ┐ └ ┘ ]
            // Top-Left corner
            context.fill(cx - fDist, cy - fDist, cx - fDist + arm, cy - fDist + 1, tc);
            context.fill(cx - fDist, cy - fDist, cx - fDist + 1, cy - fDist + arm, tc);

            // Top-Right corner
            context.fill(cx + fDist - arm + 1, cy - fDist, cx + fDist + 1, cy - fDist + 1, tc);
            context.fill(cx + fDist, cy - fDist, cx + fDist + 1, cy - fDist + arm, tc);

            // Bottom-Left corner
            context.fill(cx - fDist, cy + fDist, cx - fDist + arm, cy + fDist + 1, tc);
            context.fill(cx - fDist, cy + fDist - arm + 1, cx - fDist + 1, cy + fDist + 1, tc);

            // Bottom-Right corner
            context.fill(cx + fDist - arm + 1, cy + fDist, cx + fDist + 1, cy + fDist + 1, tc);
            context.fill(cx + fDist, cy + fDist - arm + 1, cx + fDist + 1, cy + fDist + 1, tc);

            // 2. Line end-accent frames
            context.fill(cx - 1, cy - g - s - 2, cx + t + 1, cy - g - s - 1, tc);
            context.fill(cx - 1, cy + g + s + 1, cx + t + 1, cy + g + s + 2, tc);
            context.fill(cx - g - s - 2, cy - 1, cx - g - s - 1, cy + t + 1, tc);
            context.fill(cx + g + s + 1, cy - 1, cx + g + s + 2, cy + t + 1, tc);
        }

        // Hitmarker animation on hit
        if (hitmarker.isEnabled()) {
            long elapsed = System.currentTimeMillis() - staticHitTime;
            if (elapsed < 300) {
                float alpha = 1.0f - (elapsed / 300.0f);
                int hmColor = new Color(255, 50, 50, (int) (alpha * 255)).getRGB();
                int hms = 5;
                for (int i = 2; i <= hms; i++) {
                    context.fill(cx - i, cy - i, cx - i + 1, cy - i + 1, hmColor);
                    context.fill(cx + i + 1, cy - i, cx + i + 2, cy - i + 1, hmColor);
                    context.fill(cx - i, cy + i + 1, cx - i + 1, cy + i + 2, hmColor);
                    context.fill(cx + i + 1, cy + i + 1, cx + i + 2, cy + i + 2, hmColor);
                }
            }
        }
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {}
}
