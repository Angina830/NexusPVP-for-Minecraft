package com.nexuspvp.setting;

import java.awt.*;

public class ColorSetting extends Setting<Color> {
    private boolean rainbow;

    private static final Color[] PRESETS = new Color[]{
        new Color(255, 60, 60),
        new Color(255, 140, 0),
        new Color(255, 215, 0),
        new Color(50, 205, 50),
        new Color(0, 220, 255),
        new Color(138, 43, 226),
        new Color(255, 105, 180),
        Color.WHITE
    };

    public ColorSetting(String name, Color defaultValue) {
        super(name, defaultValue);
        this.rainbow = false;
    }

    public void cycle() {
        Color cur = getValue();
        for (int i = 0; i < PRESETS.length; i++) {
            if (PRESETS[i].getRGB() == cur.getRGB()) {
                setValue(PRESETS[(i + 1) % PRESETS.length]);
                return;
            }
        }
        setValue(PRESETS[0]);
    }

    public int getRGB() {
        if (rainbow) {
            return getRainbowColor().getRGB();
        }
        return getValue().getRGB();
    }

    public int getRed() {
        return getValue().getRed();
    }

    public int getGreen() {
        return getValue().getGreen();
    }

    public int getBlue() {
        return getValue().getBlue();
    }

    public int getAlpha() {
        return getValue().getAlpha();
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public Color getColor() {
        if (rainbow) {
            return getRainbowColor();
        }
        return getValue();
    }

    private Color getRainbowColor() {
        float hue = (System.currentTimeMillis() % 3000) / 3000.0f;
        return Color.getHSBColor(hue, 0.8f, 1.0f);
    }

    public Color getColorWithAlpha(int alpha) {
        Color c = getColor();
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}