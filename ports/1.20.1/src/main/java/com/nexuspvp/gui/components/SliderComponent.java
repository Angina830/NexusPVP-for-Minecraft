package com.nexuspvp.gui.components;
import com.nexuspvp.util.Compat;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.gui.ModuleButton;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SliderComponent extends SettingComponent {

    public boolean isDragging() {
        return dragging;
    }


    private final NumberSetting numSetting;
    private boolean dragging = false;

    public SliderComponent(NumberSetting setting, ModuleButton parent) {
        super(setting, parent, 24);
        this.numSetting = setting;
    }

        @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        float mAlpha = com.nexuspvp.gui.ClickGui.getMenuAlpha();
        String translatedName = com.nexuspvp.gui.LanguageManager.getInstance().get(numSetting.getName());
        String valStr = String.valueOf(numSetting.getFloatValue());
        
        Compat.drawWithShadow(null, matrices, translatedName, x + 4, y + 2, com.nexuspvp.gui.ClickGui.applyAlpha(0xFFDBDEE1, Math.max(0.5f, mAlpha)));
        int valW = MinecraftClient.getInstance().textRenderer.getWidth(valStr);
        Compat.drawWithShadow(null, matrices, valStr, x + width - valW - 14, y + 2, com.nexuspvp.gui.ClickGui.applyAlpha(0xFF949BA4, Math.max(0.5f, mAlpha)));
        
        int sliderX = x + 4;
        int sliderY = y + 14;
        int sliderW = width - 18;
        
        RenderUtils.drawRoundedRect(matrices, sliderX, sliderY + 1, sliderW, 4, 2, com.nexuspvp.gui.ClickGui.applyAlpha(0xFF1E1F22, mAlpha));
        
        double range = numSetting.getMax() - numSetting.getMin();
        double val = numSetting.getFloatValue() - numSetting.getMin();
        double pct = Math.max(0.0, Math.min(1.0, val / range));
        
        int fillW = (int) (sliderW * pct);
        if (fillW > 0) {
            int accentColor = ThemeManager.getInstance().getAccentColor().getRGB();
            RenderUtils.drawRoundedRect(matrices, sliderX, sliderY + 1, fillW, 4, 2, com.nexuspvp.gui.ClickGui.applyAlpha(accentColor, Math.max(0.7f, mAlpha)));
        }
        
        int thumbRadius = 3;
        int thumbX = sliderX + fillW - thumbRadius;
        int thumbY = sliderY + 3 - thumbRadius;
        RenderUtils.drawRoundedRect(matrices, thumbX, thumbY, thumbRadius * 2, thumbRadius * 2, thumbRadius, com.nexuspvp.gui.ClickGui.applyAlpha(0xFFFFFFFF, Math.max(0.8f, mAlpha)));
        
        if (dragging) {
            updateValue(mouseX);
        }
    }

    private void updateValue(double mouseX) {
        double diff = Math.min(width - 8, Math.max(0, mouseX - (x + 4)));
        double min = numSetting.getMin();
        double max = numSetting.getMax();
        double inc = numSetting.getIncrement();
        
        if (diff == 0) {
            numSetting.setValue(min);
        } else {
            double newValue = roundToPlace((diff / (width - 8)) * (max - min) + min, 2);
            double precision = 1.0 / inc;
            newValue = Math.round(newValue * precision) / precision;
            numSetting.setValue(newValue);
        }
    }
    
    private double roundToPlace(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    }
}
