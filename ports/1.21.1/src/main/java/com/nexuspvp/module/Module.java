package com.nexuspvp.module;
import com.nexuspvp.util.Compat;


import com.nexuspvp.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private int keyBind;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category, int keyBind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyBind = keyBind;
        this.enabled = false;
    }

    public Module(String name, String description, Category category) {
        this(name, description, category, -1);
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    /**
     * Called during world rendering (3D context).
     */
    public void onRender3D(MatrixStack matrices, float tickDelta) {}

    /**
     * Called during HUD rendering (2D context).
     */
    public void onRender2D(MatrixStack matrices, float tickDelta) {}

    protected <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    // Getters/setters

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public int getKeyBind() {
        return keyBind;
    }

    public void setKeyBind(int keyBind) {
        this.keyBind = keyBind;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            toggle();
        }
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }
}
