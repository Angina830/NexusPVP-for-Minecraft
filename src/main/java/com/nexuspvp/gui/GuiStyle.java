package com.nexuspvp.gui;

public enum GuiStyle {
    DISCORD("Discord Modern", "Sidebar + expandable cards layout", "\uD83D\uDCAC"),
    CLASSIC_WINDOWS("Classic Windows", "Draggable category window panels", "\uD83E\uDE9F"),
    GLASS_DASHBOARD("Glass Dashboard", "Top navbar + 2-column glass grid", "\uD83D\uDC8E"),
    COMPACT_LIST("Compact List", "Minimalist high-speed flat list", "\u26A1");

    private final String displayName;
    private final String description;
    private final String icon;

    GuiStyle(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}