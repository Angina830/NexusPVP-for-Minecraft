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

import java.awt.Color;

public class Crosshair extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "Cross", "Cross", "Dot", "Circle"));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 255, 230)));
    private final NumberSetting size = addSetting(new NumberSetting("Size", 4, 1, 15, 1));
    private final NumberSetting gap = addSetting(new NumberSetting("Gap", 2, 0, 10, 1));
    private final NumberSetting thickness = addSetting(new NumberSetting("Thickness", 1, 1, 4, 1));
    private final BooleanSetting dot = addSetting(new BooleanSetting("Dot", false));
    private final BooleanSetting hitmarker = addSetting(new BooleanSetting("Hitmarker", true));

    private static long staticHitTime = 0;

    public Crosshair() {
        super("Crosshair", "Custom PvP crosshair with styles and hitmarkers", Category.RENDER);
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

        int c = color.getColor().getRGB() | 0xFF000000;
        int s = size.getIntValue();
        int g = gap.getIntValue();
        int t = Math.max(1, thickness.getIntValue());
        int halfT = t / 2;

        String mode = style.getValue();
        if (mode.equalsIgnoreCase("Dot")) {
            context.fill(cx - 1, cy - 1, cx + 2, cy + 2, c);
        } else if (mode.equalsIgnoreCase("Circle")) {
            context.fill(cx - s, cy - halfT, cx - s + t, cy + halfT + 1, c);
            context.fill(cx + s - t, cy - halfT, cx + s, cy + halfT + 1, c);
            context.fill(cx - halfT, cy - s, cx + halfT + 1, cy - s + t, c);
            context.fill(cx - halfT, cy + s - t, cx + halfT + 1, cy + s, c);
        } else { // Cross
            context.fill(cx - halfT, cy - g - s, cx + halfT + (t % 2 == 0 ? 0 : 1), cy - g, c);
            context.fill(cx - halfT, cy + g, cx + halfT + (t % 2 == 0 ? 0 : 1), cy + g + s, c);
            context.fill(cx - g - s, cy - halfT, cx - g, cy + halfT + (t % 2 == 0 ? 0 : 1), c);
            context.fill(cx + g, cy - halfT, cx + g + s, cy + halfT + (t % 2 == 0 ? 0 : 1), c);
            if (dot.isEnabled()) {
                context.fill(cx - 1, cy - 1, cx + 1, cy + 1, c);
            }
        }

        if (hitmarker.isEnabled()) {
            long elapsed = System.currentTimeMillis() - staticHitTime;
            if (elapsed < 300) {
                float alpha = 1.0f - (elapsed / 300.0f);
                int hmColor = new Color(255, 50, 50, (int) (alpha * 255)).getRGB();
                int hms = 5;
                for (int i = 2; i <= hms; i++) {
                    context.fill(cx - i, cy - i, cx - i + 1, cy - i + 1, hmColor);
                    context.fill(cx + i, cy - i, cx + i + 1, cy - i + 1, hmColor);
                    context.fill(cx - i, cy + i, cx - i + 1, cy + i + 1, hmColor);
                    context.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, hmColor);
                }
            }
        }
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {}
}
