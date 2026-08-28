package com.nexuspvp.setting;

public abstract class Setting<T> {
    private final String name;
    private T value;
    private boolean visible = true;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isVisible() {
        return visible;
    }

    public Setting<T> setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }
}
