package com.nexuspvp.gui.components;
import com.nexuspvp.util.Compat;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.gui.ModuleButton;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.Color;

public class BooleanComponent extends SettingComponent {

    private final BooleanSetting boolSetting;
    private float toggleAnim = 0.0f;

    public BooleanComponent(BooleanSetting setting, ModuleButton parent) {
        super(setting, parent, 14);
        this.boolSetting = setting;
        this.toggleAnim = setting.isEnabled() ? 1.0f : 0.0f;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        String translatedName = com.nexuspvp.gui.LanguageManager.getInstance().get(boolSetting.getName());
        Compat.drawWithShadow(null, matrices, translatedName, x + 4, y + 3, 0xFFDBDEE1);
        
        int toggleW = 18;
        int toggleH = 10;
        int toggleX = x + width - toggleW - 14;
        int toggleY = y + 2;
        
        boolean enabled = boolSetting.isEnabled();
        int accent = ThemeManager.getInstance().getAccentColor().getRGB();
        
        float target = enabled ? 1.0f : 0.0f;
        toggleAnim += (target - toggleAnim) * 0.28f;
        if (Math.abs(target - toggleAnim) < 0.005f) toggleAnim = target;

        int swR = (int) (78 + (((accent >> 16) & 0xFF) - 78) * toggleAnim);
        int swG = (int) (80 + (((accent >> 8) & 0xFF) - 80) * toggleAnim);
        int swB = (int) (88 + ((accent & 0xFF) - 88) * toggleAnim);
        int switchBg = (0xFF << 24) | (swR << 16) | (swG << 8) | swB;
        
        RenderUtils.drawRoundedRect(matrices, toggleX, toggleY, toggleW, toggleH, 5, switchBg);
        
        int thumbRadius = 3;
        int startX = toggleX + 2;
        int endX = toggleX + toggleW - thumbRadius * 2 - 2;
        int thumbX = (int) (startX + (endX - startX) * toggleAnim);
        int thumbY = toggleY + 2;
        
        RenderUtils.drawRoundedRect(matrices, thumbX, thumbY, thumbRadius * 2, thumbRadius * 2, thumbRadius, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            boolSetting.toggle();
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {}
}
