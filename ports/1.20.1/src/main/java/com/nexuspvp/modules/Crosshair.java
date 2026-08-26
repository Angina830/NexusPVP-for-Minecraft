package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class Crosshair extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "Cross", "Cross", "Dot", "Circle", "None"));
    private final BooleanSetting onlyHitmarker = addSetting(new BooleanSetting("OnlyHitmarker", false));
    private final NumberSetting size = addSetting(new NumberSetting("Size", 4.0, 1.0, 15.0, 0.5));
    private final NumberSetting gap = addSetting(new NumberSetting("Gap", 3.0, 0.0, 10.0, 0.5));
    private final NumberSetting thickness = addSetting(new NumberSetting("Thickness", 1.5, 0.5, 5.0, 0.5));
    private final BooleanSetting dot = addSetting(new BooleanSetting("CenterDot", false));
    private final BooleanSetting hitmarker = addSetting(new BooleanSetting("Hitmarker", true));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 255, 200)));

    private static long lastHitTime = 0;

    public Crosshair() {
        super("Crosshair", "Custom PvP crosshair with hitmarkers", Category.PVP);
    }

    public static void recordHit() {
        lastHitTime = System.currentTimeMillis();
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null) return;

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int cx = screenW / 2;
        int cy = screenH / 2;

        int c = color.getColor().getRGB();
        float sz = size.getFloatValue();
        float gp = gap.getFloatValue();
        float th = thickness.getFloatValue();
        int halfTh = (int) Math.max(1, th / 2);

        String currentStyle = style.getValue();

        // If not set to OnlyHitmarker and not None style, draw custom crosshair
        if (!onlyHitmarker.isEnabled() && !currentStyle.equals("None")) {
            if (currentStyle.equals("Dot") || dot.isEnabled()) {
                RenderUtils.drawRect(matrices, cx - 1, cy - 1, 2, 2, c);
            }

            if (currentStyle.equals("Cross")) {
                // Top
                RenderUtils.drawRect(matrices, cx - halfTh, (int)(cy - gp - sz), (int)th, (int)sz, c);
                // Bottom
                RenderUtils.drawRect(matrices, cx - halfTh, (int)(cy + gp), (int)th, (int)sz, c);
                // Left
                RenderUtils.drawRect(matrices, (int)(cx - gp - sz), cy - halfTh, (int)sz, (int)th, c);
                // Right
                RenderUtils.drawRect(matrices, (int)(cx + gp), cy - halfTh, (int)sz, (int)th, c);
            } else if (currentStyle.equals("Circle")) {
                RenderUtils.drawRect(matrices, cx - halfTh, (int)(cy - sz), (int)th, 2, c);
                RenderUtils.drawRect(matrices, cx - halfTh, (int)(cy + sz - 2), (int)th, 2, c);
                RenderUtils.drawRect(matrices, (int)(cx - sz), cy - halfTh, 2, (int)th, c);
                RenderUtils.drawRect(matrices, (int)(cx + sz - 2), cy - halfTh, 2, (int)th, c);
            }
        }

        // Render Call of Duty / Apex Hitmarker X on hit!
        if (hitmarker.isEnabled() && (System.currentTimeMillis() - lastHitTime < 300)) {
            float fade = 1.0f - ((System.currentTimeMillis() - lastHitTime) / 300.0f);
            int alpha = (int) (fade * 255);
            int hmColor = (alpha << 24) | 0xFF3333;
            int hmSize = 6;
            int hmGap = 4;

            // 4 diagonals (Top-Left, Top-Right, Bottom-Left, Bottom-Right)
            RenderUtils.drawRect(matrices, cx - hmGap - hmSize, cy - hmGap - hmSize, hmSize, 1, hmColor);
            RenderUtils.drawRect(matrices, cx - hmGap - 1, cy - hmGap - hmSize, 1, hmSize, hmColor);

            RenderUtils.drawRect(matrices, cx + hmGap, cy - hmGap - hmSize, hmSize, 1, hmColor);
            RenderUtils.drawRect(matrices, cx + hmGap, cy - hmGap - hmSize, 1, hmSize, hmColor);

            RenderUtils.drawRect(matrices, cx - hmGap - hmSize, cy + hmGap + hmSize - 1, hmSize, 1, hmColor);
            RenderUtils.drawRect(matrices, cx - hmGap - 1, cy + hmGap, 1, hmSize, hmColor);

            RenderUtils.drawRect(matrices, cx + hmGap, cy + hmGap + hmSize - 1, hmSize, 1, hmColor);
            RenderUtils.drawRect(matrices, cx + hmGap, cy + hmGap, 1, hmSize, hmColor);
        }
    }
}