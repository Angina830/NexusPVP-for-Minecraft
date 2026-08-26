package com.nexuspvp.setting;
import com.nexuspvp.util.Compat;


import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name, defaultValue);
        this.modes = Arrays.asList(modes);
    }

    public List<String> getModes() {
        return modes;
    }

    public int getIndex() {
        return modes.indexOf(getValue());
    }

    public void cycle() {
        int next = (getIndex() + 1) % modes.size();
        setValue(modes.get(next));
    }

    public boolean is(String mode) {
        return getValue().equalsIgnoreCase(mode);
    }
}
