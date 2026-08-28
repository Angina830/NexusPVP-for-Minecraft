package com.nexuspvp.gui.components;
import com.nexuspvp.util.Compat;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.gui.ModuleButton;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.Color;
import java.util.List;

public class ModeComponent extends SettingComponent {

    private final ModeSetting modeSetting;

    public ModeComponent(ModeSetting setting, ModuleButton parent) {
        super(setting, parent, 14);
        this.modeSetting = setting;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        String translatedName = com.nexuspvp.gui.LanguageManager.getInstance().get(modeSetting.getName());
        String translatedValue = com.nexuspvp.gui.LanguageManager.getInstance().get(modeSetting.getValue());
        
        Compat.drawWithShadow(null, matrices, translatedName, x + 4, y + 3, 0xFFDBDEE1);
        
        int badgeW = MinecraftClient.getInstance().textRenderer.getWidth(translatedValue) + 8;
        int badgeX = x + width - badgeW - 14;
        RenderUtils.drawRoundedRect(matrices, badgeX, y + 1, badgeW, 12, 3, 0xFF1E1F22);
        int accentColor = ThemeManager.getInstance().getAccentColor().getRGB();
        Compat.drawWithShadow(null, matrices, translatedValue, badgeX + 4, y + 3, accentColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                modeSetting.cycle();
                return true;
            } else if (button == 1) {
                List<String> modes = modeSetting.getModes();
                int idx = modes.indexOf(modeSetting.getValue());
                idx--;
                if (idx < 0) idx = modes.size() - 1;
                modeSetting.setValue(modes.get(idx));
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {}
}
