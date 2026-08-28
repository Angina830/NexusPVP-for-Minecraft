package com.nexuspvp.util;

import java.awt.*;

public class ColorUtils {

    /**
     * Get a rainbow color cycling over time.
     */
    public static Color rainbow(long offset) {
        float hue = ((System.currentTimeMillis() + offset) % 3000) / 3000.0f;
        return Color.getHSBColor(hue, 0.7f, 1.0f);
    }

    /**
     * Get rainbow color with custom speed.
     */
    public static Color rainbow(long offset, float speed) {
        float hue = ((System.currentTimeMillis() * speed + offset) % 3000) / 3000.0f;
        return Color.getHSBColor(hue, 0.7f, 1.0f);
    }

    /**
     * Interpolate between two colors.
     */
    public static Color interpolate(Color c1, Color c2, float factor) {
        factor = Math.max(0, Math.min(1, factor));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * factor);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * factor);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * factor);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * factor);
        return new Color(r, g, b, a);
    }

    /**
     * Create color from RGBA integers.
     */
    public static Color fromRGBA(int r, int g, int b, int a) {
        return new Color(r, g, b, a);
    }

    /**
     * Create color with modified alpha.
     */
    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    /**
     * Convert Color to ARGB int for Minecraft rendering.
     */
    public static int toARGB(Color color) {
        return (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }

    /**
     * Convert Color to ABGR int (for some MC rendering methods).
     */
    public static int toABGR(Color color) {
        return (color.getAlpha() << 24) | (color.getBlue() << 16) | (color.getGreen() << 8) | color.getRed();
    }

    /**
     * Pulse alpha for breathing effect.
     */
    public static Color pulse(Color base, float speed) {
        float factor = (float) (Math.sin(System.currentTimeMillis() * speed / 1000.0) * 0.5 + 0.5);
        int alpha = (int) (50 + factor * 200);
        return withAlpha(base, alpha);
    }

    /**
     * Get a gradient color between two colors based on position (0.0 to 1.0).
     */
    public static int gradient(Color start, Color end, float position) {
        return toARGB(interpolate(start, end, position));
    }
}
