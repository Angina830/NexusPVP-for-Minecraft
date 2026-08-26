package com.nexuspvp.gui;
import com.nexuspvp.util.Compat;


import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ThemeManager {

    private static ThemeManager instance;
    private String currentTheme = "Discord";
    private GuiStyle currentStyle = GuiStyle.DISCORD;
    private final Map<String, Theme> themes = new LinkedHashMap<>();

    public ThemeManager() {
        instance = this;
        themes.put("Discord", new Theme(new Color(88, 101, 242), new Color(49, 51, 56), new Color(43, 45, 49)));
        themes.put("Purple", new Theme(new Color(168, 85, 247), new Color(28, 20, 42), new Color(38, 26, 56)));
        themes.put("Blue", new Theme(new Color(14, 165, 233), new Color(15, 23, 42), new Color(24, 34, 58)));
        themes.put("Red", new Theme(new Color(239, 68, 68), new Color(38, 18, 20), new Color(52, 22, 26)));
        themes.put("Green", new Theme(new Color(34, 197, 94), new Color(18, 38, 24), new Color(24, 52, 32)));
        themes.put("Pink", new Theme(new Color(236, 72, 153), new Color(38, 18, 30), new Color(52, 24, 42)));
        themes.put("Orange", new Theme(new Color(249, 115, 22), new Color(38, 26, 16), new Color(52, 34, 20)));
        themes.put("Cyan", new Theme(new Color(6, 182, 212), new Color(16, 32, 38), new Color(22, 46, 54)));
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public Color getAccentColor() {
        Theme t = themes.get(currentTheme);
        return t != null ? t.accent : new Color(88, 101, 242);
    }

    public Color getThemeAccent(String themeName) {
        Theme t = themes.get(themeName);
        return t != null ? t.accent : Color.WHITE;
    }

    public Color getBackgroundColor() {
        Theme t = themes.get(currentTheme);
        return t != null ? t.background : new Color(49, 51, 56);
    }

    public Color getPanelColor() {
        Theme t = themes.get(currentTheme);
        return t != null ? t.panel : new Color(43, 45, 49);
    }
    
    public Color getTextColor() {
        return Color.WHITE;
    }

    public void setTheme(String theme) {
        if (themes.containsKey(theme)) {
            this.currentTheme = theme;
        }
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public Set<String> getThemeNames() {
        return themes.keySet();
    }

    public GuiStyle getCurrentStyle() {
        return currentStyle;
    }

    public void setStyle(GuiStyle style) {
        if (style != null) {
            this.currentStyle = style;
        }
    }

    private static class Theme {
        Color accent;
        Color background;
        Color panel;

        Theme(Color accent, Color background, Color panel) {
            this.accent = accent;
            this.background = background;
            this.panel = panel;
        }
    }
}