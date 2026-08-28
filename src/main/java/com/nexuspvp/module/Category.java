package com.nexuspvp.module;

public enum Category {
    PVP("PvP"),
    HUD("HUD"),
    PLAYER("Player"),
    VISUAL("Visuals"),
    RENDER("Render"),
    GUI("GUI"),
    MISC("Misc");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}