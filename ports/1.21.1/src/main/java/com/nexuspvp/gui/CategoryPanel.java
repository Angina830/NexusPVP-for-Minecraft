package com.nexuspvp.gui;
import com.nexuspvp.util.Compat;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {

    private int x, y;
    private final int width = 120;
    private final int headerHeight = 25;
    private final Category category;
    private final List<ModuleButton> buttons = new ArrayList<>();
    
    private boolean expanded = true;
    private boolean dragging = false;
    private double dragOffsetX, dragOffsetY;
    
    private int scrollOffset = 0;

    public CategoryPanel(Category category, int x, int y) {
        this.category = category;
        this.x = (x);
        this.y = (y);

        List<Module> modules = NexusPVP.getInstance().getModuleManager().getModulesByCategory(category);
        for (Module m : modules) {
            buttons.add(new ModuleButton(m, this));
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        ThemeManager theme = NexusPVP.getInstance().getThemeManager();
        Color accent = theme.getAccentColor(); // #5865F2 Blurple
        
        // Discord Dark Colors
        int headerBg = 0xFF1E1F22; // Discord header dark
        int bodyBg = 0xFF2B2D31;   // Discord channel list dark
        int borderColor = 0xFF18191C;
        
        int totalH = headerHeight;
        if (expanded) {
            for (ModuleButton mb : buttons) totalH += mb.getHeight();
        }
        
        // Outer border / shadow
        RenderUtils.drawRoundedRect(matrices, x - 1, y - 1, width + 2, totalH + 2, 6, borderColor);
        // Body background
        RenderUtils.drawRoundedRect(matrices, x, y, width, totalH, 5, bodyBg);
        
        // Header background (top rounded)
        RenderUtils.drawRoundedRect(matrices, x, y, width, headerHeight, 5, headerBg);
        if (expanded) {
            RenderUtils.drawRect(matrices, x, y + headerHeight - 4, width, 4, headerBg);
            // Subtle Discord separator line
            RenderUtils.drawRect(matrices, x + 4, y + headerHeight - 1, width - 8, 1, 0xFF35373C);
        }
        
        // Discord hashtag prefix '#' in muted gray, then Category Name in white
        String hashTag = "# ";
        String translatedName = LanguageManager.getInstance().get(category.name()).toLowerCase();
        int hashWidth = MinecraftClient.getInstance().textRenderer.getWidth(hashTag);
        
        int startX = x + 8;
        int textY = y + (headerHeight - 8) / 2;
        
        Compat.drawWithShadow(null, matrices, hashTag, startX, textY, 0x80949BA4);
        Compat.drawWithShadow(null, matrices, translatedName, startX + hashWidth, textY, 0xFFF2F3F5);
        
        // Discord expand arrow icon (v / >)
        String expandIcon = expanded ? "v" : ">";
        int iconWidth = MinecraftClient.getInstance().textRenderer.getWidth(expandIcon);
        Compat.drawWithShadow(null, matrices, expandIcon, x + width - iconWidth - 8, textY, 0xFF949BA4);

        if (expanded) {
            int currentY = y + headerHeight + scrollOffset;
            for (ModuleButton button : buttons) {
                button.setX(x);
                button.setY(currentY);
                button.render(matrices, mouseX, mouseY, delta);
                currentY += button.getHeight();
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHoveredHeader(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
                return true;
            } else if (button == 1) {
                expanded = !expanded;
                return true;
            }
        }
        
        if (expanded && isHovered(mouseX, mouseY)) {
            for (ModuleButton mb : buttons) {
                if (mb.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        if (expanded) {
            for (ModuleButton mb : buttons) {
                mb.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            x = (int) (mouseX - dragOffsetX);
            y = (int) (mouseY - dragOffsetY);
        } else if (expanded) {
            for (ModuleButton mb : buttons) {
                mb.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double amount) {
        if (expanded && isHovered(mouseX, mouseY)) {
            scrollOffset += (int)(amount * 10);
            // simple clamp could go here
        }
    }
    
    private boolean isHoveredHeader(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight;
    }
    
    private boolean isHovered(double mouseX, double mouseY) {
        int totalH = headerHeight;
        if (expanded) {
            for (ModuleButton mb : buttons) totalH += mb.getHeight();
        }
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + totalH;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
}
