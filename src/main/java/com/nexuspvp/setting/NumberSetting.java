package com.nexuspvp.setting;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double increment;

    public NumberSetting(String name, double defaultValue, double min, double max, double increment) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }

    public float getFloatValue() {
        return getValue().floatValue();
    }

    public int getIntValue() {
        return getValue().intValue();
    }

    @Override
    public void setValue(Double value) {
        double rounded = Math.round(value / increment) * increment;
        super.setValue(Math.max(min, Math.min(max, rounded)));
    }
}
