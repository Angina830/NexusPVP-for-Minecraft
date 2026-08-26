package com.nexuspvp.setting;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public boolean isEnabled() {
        return getValue();
    }

    public void toggle() {
        setValue(!getValue());
    }
}
