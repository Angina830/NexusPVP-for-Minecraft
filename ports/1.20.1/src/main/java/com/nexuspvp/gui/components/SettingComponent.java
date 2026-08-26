package com.nexuspvp.gui.components;

import com.nexuspvp.gui.ModuleButton;
import com.nexuspvp.setting.Setting;
import net.minecraft.client.util.math.MatrixStack;

public abstract class SettingComponent {
    
    protected Setting<?> setting;
    protected ModuleButton parent;
    protected int x, y, width, height;

    public SettingComponent(Setting<?> setting, ModuleButton parent, int height) {
        this.setting = setting;
        this.parent = parent;
        this.width = parent.getWidth();
        this.height = height;
    }

    public abstract void render(MatrixStack matrices, int mouseX, int mouseY, float delta);
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);
    public abstract void mouseReleased(double mouseX, double mouseY, int button);
    public abstract void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setWidth(int width) { this.width = width; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    protected boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
