package com.nexuspvp.gui.components;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.gui.ModuleButton;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.Color;

public class ColorComponent extends SettingComponent {

    private final ColorSetting colorSetting;
    private boolean expanded = false;
    private int draggingMode = 0; // 0=none, 1=hue, 2=saturation, 3=brightness

    public ColorComponent(ColorSetting setting, ModuleButton parent) {
        super(setting, parent, 14);
        this.colorSetting = setting;
    }

    @Override
    public int getHeight() {
        return expanded ? 50 : 14;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        String translatedName = com.nexuspvp.gui.LanguageManager.getInstance().get(colorSetting.getName());
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, translatedName, x + 4, y + 3, 0xFFDBDEE1);
        
        int boxW = 12;
        int boxH = 10;
        int boxX = x + width - boxW - 14;
        int boxY = y + 2;
        
        Color c = colorSetting.getColor();
        // Outer border for swatch
        RenderUtils.drawRoundedRect(matrices, boxX - 1, boxY - 1, boxW + 2, boxH + 2, 3, 0xFF1E1F22);
        // Swatch
        RenderUtils.drawRoundedRect(matrices, boxX, boxY, boxW, boxH, 2, c.getRGB());
        
        if (expanded) {
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            int sliderW = width - 18;
            
            // Hue slider
            RenderUtils.drawRoundedRect(matrices, x + 4, y + 16, sliderW, 5, 2, 0xFF1E1F22);
            RenderUtils.drawRoundedRect(matrices, x + 4, y + 16, (int)(sliderW * hsb[0]), 5, 2, Color.HSBtoRGB(hsb[0], 1.0f, 1.0f));
            
            // Saturation slider
            RenderUtils.drawRoundedRect(matrices, x + 4, y + 26, sliderW, 5, 2, 0xFF1E1F22);
            RenderUtils.drawRoundedRect(matrices, x + 4, y + 26, (int)(sliderW * hsb[1]), 5, 2, Color.HSBtoRGB(hsb[0], hsb[1], 1.0f));
            
            // Brightness slider
            RenderUtils.drawRoundedRect(matrices, x + 4, y + 36, sliderW, 5, 2, 0xFF1E1F22);
            RenderUtils.drawRoundedRect(matrices, x + 4, y + 36, (int)(sliderW * hsb[2]), 5, 2, Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));

            if (draggingMode > 0) {
                updateValue(mouseX);
            }
        }
    }

    private void updateValue(double mouseX) {
        double diff = Math.min(width - 8, Math.max(0, mouseX - (x + 4)));
        float pct = (float) (diff / (width - 8));
        
        Color c = colorSetting.getColor();
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        
        if (draggingMode == 1) hsb[0] = pct;
        else if (draggingMode == 2) hsb[1] = pct;
        else if (draggingMode == 3) hsb[2] = pct;
        
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        Color newColor = new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, c.getAlpha());
        colorSetting.setValue(newColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= y && mouseY <= y + 14 && mouseX >= x && mouseX <= x + width) {
            if (button == 1) { 
                colorSetting.setRainbow(!colorSetting.isRainbow());
                return true;
            } else if (button == 0) {
                expanded = !expanded;
                return true;
            }
        }
        
        if (expanded && button == 0) {
            if (mouseY >= y + 16 && mouseY <= y + 22) { draggingMode = 1; return true; }
            if (mouseY >= y + 26 && mouseY <= y + 32) { draggingMode = 2; return true; }
            if (mouseY >= y + 36 && mouseY <= y + 42) { draggingMode = 3; return true; }
        }
        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) draggingMode = 0;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {}
}
