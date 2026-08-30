package com.nexuspvp.module;

import com.nexuspvp.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public enum PerformanceImpact {
        NEGLIGIBLE("0.01ms (Zero Impact)", 0xFF23A55A),
        VERY_LOW("0.03ms (Very Fast)", 0xFF57F287),
        LOW("0.07ms (Fast)", 0xFF5865F2),
        MEDIUM("0.15ms (Normal)", 0xFFFEE75C);

        private final String label;
        private final int color;

        PerformanceImpact(String label, int color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public int getColor() { return color; }
    }

    public enum Legitimacy {
        LEGIT("100% Legit (Visual)", 0xFF23A55A),
        SAFE("Safe (Client-Side)", 0xFF57F287),
        SERVER_PROOF("Undetectable (No Packets)", 0xFF5865F2);

        private final String label;
        private final int color;

        Legitimacy(String label, int color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public int getColor() { return color; }
    }

    private final String name;
    private final String description;
    private final Category category;
    private int keyBind;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    private PerformanceImpact impact = PerformanceImpact.VERY_LOW;
    private Legitimacy legitimacy = Legitimacy.LEGIT;

    // Realtime nanosecond profiling metrics
    private long lastRender2DTimeNanos = 0;
    private long lastRender3DTimeNanos = 0;
    private long lastTickTimeNanos = 0;
    private float avgRenderMicros = 0;
    private float peakRenderMicros = 0;

    public Module(String name, String description, Category category, int keyBind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyBind = keyBind;
        this.enabled = false;
        initDefaultRatings();
    }

    public Module(String name, String description, Category category) {
        this(name, description, category, -1);
    }

    private void initDefaultRatings() {
        String n = this.name.toLowerCase();
        if (n.contains("stun") || n.contains("trajectory") || n.contains("blockoutline")) {
            this.impact = PerformanceImpact.MEDIUM;
        } else if (n.contains("jump") || n.contains("particles") || n.contains("trails") || n.contains("chinahat") || n.contains("nimb")) {
            this.impact = PerformanceImpact.LOW;
        } else if (n.contains("health") || n.contains("targethud") || n.contains("viewmodel") || n.contains("babymode") || n.contains("hud")) {
            this.impact = PerformanceImpact.VERY_LOW;
        } else {
            this.impact = PerformanceImpact.NEGLIGIBLE;
        }
        this.legitimacy = Legitimacy.LEGIT;
    }

    public PerformanceImpact getImpact() { return impact; }
    public void setImpact(PerformanceImpact impact) { this.impact = impact; }

    public Legitimacy getLegitimacy() { return legitimacy; }
    public void setLegitimacy(Legitimacy legitimacy) { this.legitimacy = legitimacy; }

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

    public void onRender3D(MatrixStack matrices, float tickDelta) {}
    public void onRender2D(MatrixStack matrices, float tickDelta) {}

    protected <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    // Profiling methods
    public void recordRender2DTime(long nanos) {
        this.lastRender2DTimeNanos = nanos;
        updateRenderMetrics(nanos);
    }

    public void recordRender3DTime(long nanos) {
        this.lastRender3DTimeNanos = nanos;
        updateRenderMetrics(nanos);
    }

    public void recordTickTime(long nanos) {
        this.lastTickTimeNanos = nanos;
    }

    private void updateRenderMetrics(long nanos) {
        float micros = nanos / 1000.0f;
        this.avgRenderMicros = this.avgRenderMicros * 0.90f + micros * 0.10f;
        if (micros > this.peakRenderMicros) {
            this.peakRenderMicros = micros;
        } else {
            this.peakRenderMicros = Math.max(0, this.peakRenderMicros * 0.992f);
        }
    }

    public float getAvgRenderMicros() { return avgRenderMicros; }
    public float getPeakRenderMicros() { return peakRenderMicros; }
    public long getLastTotalRenderNanos() { return lastRender2DTimeNanos + lastRender3DTimeNanos; }
    public long getLastTickTimeNanos() { return lastTickTimeNanos; }

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
