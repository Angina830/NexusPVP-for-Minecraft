package com.nexuspvp.gui;

import com.nexuspvp.gui.components.*;
import com.nexuspvp.module.Module;
import com.nexuspvp.modules.CommandKeybinds;
import com.nexuspvp.modules.ViewModel;
import com.nexuspvp.setting.*;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton {

    private final Module module;
    private final CategoryPanel parent;
    private int x, y;
    private int width = 340;
    private final int baseHeight = 28;
    private boolean expanded = false;
    private boolean binding = false;
    
    private float expandAnim = 0.0f;
    private float toggleAnim = 0.0f;
    private float clickScale = 1.0f;
    private float hoverAnim = 0.0f;
    
    private final List<SettingComponent> settingComponents = new ArrayList<>();

    public ModuleButton(Module module, CategoryPanel parent) {
        this.module = module;
        this.parent = parent;
        this.toggleAnim = module.isEnabled() ? 1.0f : 0.0f;
        
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting) {
                settingComponents.add(new BooleanComponent((BooleanSetting) setting, this));
            } else if (setting instanceof NumberSetting) {
                settingComponents.add(new SliderComponent((NumberSetting) setting, this));
            } else if (setting instanceof ModeSetting) {
                settingComponents.add(new ModeComponent((ModeSetting) setting, this));
            } else if (setting instanceof ColorSetting) {
                settingComponents.add(new ColorComponent((ColorSetting) setting, this));
            }
        }
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY);
        boolean enabled = module.isEnabled();
        int accent = ThemeManager.getInstance().getAccentColor().getRGB();
        
        float targetExpand = expanded ? 1.0f : 0.0f;
        expandAnim += (targetExpand - expandAnim) * 0.24f;
        if (Math.abs(targetExpand - expandAnim) < 0.005f) expandAnim = targetExpand;

        float targetToggle = enabled ? 1.0f : 0.0f;
        toggleAnim += (targetToggle - toggleAnim) * 0.28f;
        if (Math.abs(targetToggle - toggleAnim) < 0.005f) toggleAnim = targetToggle;

        float targetHover = hovered ? 1.0f : 0.0f;
        hoverAnim += (targetHover - hoverAnim) * 0.22f;

        clickScale += (1.0f - clickScale) * 0.24f;
        
        int totalH = getHeight();

        int r = (int) (43 + (53 - 43) * hoverAnim);
        int g = (int) (45 + (55 - 45) * hoverAnim);
        int b = (int) (49 + (60 - 49) * hoverAnim);
        int bgColor = (0xFF << 24) | (r << 16) | (g << 8) | b;

        int borderColor = (toggleAnim > 0.05f) ? accent : 0xFF1E1F22;
        
        matrices.push();
        if (clickScale < 0.99f) {
            float cx = x + width / 2.0f;
            float cy = y + baseHeight / 2.0f;
            matrices.translate(cx, cy, 0);
            matrices.scale(clickScale, clickScale, 1.0f);
            matrices.translate(-cx, -cy, 0);
        }

        RenderUtils.drawRoundedRect(matrices, x - 1, y - 1, width + 2, totalH + 2, 5, borderColor);
        RenderUtils.drawRoundedRect(matrices, x, y, width, totalH, 4, bgColor);
        
        if (toggleAnim > 0.01f) {
            int pillH = (int) ((baseHeight - 8) * toggleAnim);
            int pillY = y + 4 + (baseHeight - 8 - pillH) / 2;
            RenderUtils.drawRoundedRect(matrices, x + 2, pillY, 3, pillH, 2, accent);
        }
        
        String translatedName = LanguageManager.getInstance().get(module.getName());
        int textX = x + (int) (8 + 3 * toggleAnim);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, translatedName, textX, y + 4, 0xFFF2F3F5);
        
        String desc = LanguageManager.getInstance().get(module.getDescription());
        if (desc != null && !desc.isEmpty()) {
            if (MinecraftClient.getInstance().textRenderer.getWidth(desc) > width - 110) {
                desc = desc.substring(0, Math.min(desc.length(), 28)) + "...";
            }
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, desc, textX, y + 15, 0xFF949BA4);
        }

        // 1. In-Card Interactive Keybind Button
        int bindW = 44;
        int bindH = 14;
        int bindX = x + width - 78;
        int bindY = y + 7;
        boolean bindHover = mouseX >= bindX && mouseX <= bindX + bindW && mouseY >= bindY && mouseY <= bindY + bindH;
        int bindBg = binding ? accent : (bindHover ? 0xFF35373C : 0xFF2B2D31);
        RenderUtils.drawRoundedRect(matrices, bindX, bindY, bindW, bindH, 3, bindBg);

        String keyText = binding ? "..." : getKeyName(module.getKeyBind());
        int ktw = MinecraftClient.getInstance().textRenderer.getWidth(keyText);
        int keyCol = binding ? 0xFFFFFFFF : (module.getKeyBind() > 0 ? accent : 0xFF949BA4);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, keyText, bindX + (bindW - ktw) / 2, bindY + 3, keyCol);
        
        // 2. Toggle Switch
        int toggleW = 20;
        int toggleH = 12;
        int toggleX = x + width - toggleW - 22;
        int toggleY = y + 8;

        int swR = (int) (78 + (((accent >> 16) & 0xFF) - 78) * toggleAnim);
        int swG = (int) (80 + (((accent >> 8) & 0xFF) - 80) * toggleAnim);
        int swB = (int) (88 + ((accent & 0xFF) - 88) * toggleAnim);
        int switchBg = (0xFF << 24) | (swR << 16) | (swG << 8) | swB;
        RenderUtils.drawRoundedRect(matrices, toggleX, toggleY, toggleW, toggleH, 6, switchBg);

        int thumbRadius = 4;
        int thumbStartX = toggleX + 2;
        int thumbEndX = toggleX + toggleW - thumbRadius * 2 - 2;
        int thumbX = (int) (thumbStartX + (thumbEndX - thumbStartX) * toggleAnim);
        int thumbY = toggleY + 2;
        RenderUtils.drawRoundedRect(matrices, thumbX, thumbY, thumbRadius * 2, thumbRadius * 2, thumbRadius, 0xFFFFFFFF);
        
        // 3. Settings dropdown chevron
        if (!settingComponents.isEmpty()) {
            int chevronX = x + width - 12;
            int chevronY = y + 9;
            String chevron = expanded ? "v" : ">";
            int chCol = expanded ? accent : 0xFF949BA4;
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, chevron, chevronX, chevronY, chCol);
        }
        
        // 4. Render expanded settings
        if (expandAnim > 0.01f && !settingComponents.isEmpty()) {
            int settingsTargetH = 0;
            for (SettingComponent comp : settingComponents) {
                settingsTargetH += comp.getHeight();
            }
            int currentSettingsH = (int) (settingsTargetH * expandAnim);

            RenderUtils.startScissor(x + 2, y + baseHeight, width - 4, currentSettingsH);

            int compY = y + baseHeight + 2;
            for (SettingComponent comp : settingComponents) {
                comp.setX(x + 10);
                comp.setY(compY);
                comp.setWidth(width - 20);
                comp.render(matrices, mouseX, mouseY, delta);
                compY += comp.getHeight();
            }

            RenderUtils.endScissor();
        }

        matrices.pop();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) {
            binding = false;
            return false;
        }

        clickScale = 0.96f;

        int bindW = 44;
        int bindH = 14;
        int bindX = x + width - 78;
        int bindY = y + 7;
        if (mouseX >= bindX && mouseX <= bindX + bindW && mouseY >= bindY && mouseY <= bindY + bindH) {
            if (button == 0) {
                binding = !binding;
                return true;
            } else if (button == 1) {
                module.setKeyBind(0);
                binding = false;
                return true;
            }
        }
        binding = false;

        if (mouseY >= y && mouseY <= y + baseHeight) {
            if (button == 0) {
                module.toggle();
                return true;
            } else if (button == 1) {
                if (!settingComponents.isEmpty()) {
                    expanded = !expanded;
                }
                return true;
            }
        }

        if (expanded && mouseY > y + baseHeight) {
            for (SettingComponent comp : settingComponents) {
                if (comp.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                module.setKeyBind(0);
            } else {
                module.setKeyBind(keyCode);
            }
            binding = false;
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (expanded) {
            for (SettingComponent comp : settingComponents) {
                comp.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (expanded) {
            for (SettingComponent comp : settingComponents) {
                comp.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }
    }

    public int getHeight() {
        if (!expanded && expandAnim <= 0.005f) {
            return baseHeight;
        }
        int settingsH = 0;
        for (SettingComponent comp : settingComponents) {
            settingsH += comp.getHeight();
        }
        return baseHeight + (int) (settingsH * expandAnim) + (int) (4 * expandAnim);
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + getHeight();
    }

    public Module getModule() { return module; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }

    private static String getKeyName(int key) {
        if (key <= 0) return "NONE";
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) return "RSHIFT";
        if (key == GLFW.GLFW_KEY_LEFT_SHIFT) return "LSHIFT";
        if (key == GLFW.GLFW_KEY_RIGHT_CONTROL) return "RCTRL";
        if (key == GLFW.GLFW_KEY_LEFT_CONTROL) return "LCTRL";
        if (key == GLFW.GLFW_KEY_RIGHT_ALT) return "RALT";
        if (key == GLFW.GLFW_KEY_LEFT_ALT) return "LALT";
        if (key == GLFW.GLFW_KEY_SPACE) return "SPACE";
        if (key == GLFW.GLFW_KEY_TAB) return "TAB";
        if (key == GLFW.GLFW_KEY_CAPS_LOCK) return "CAPS";
        String name = GLFW.glfwGetKeyName(key, 0);
        return name != null ? name.toUpperCase() : "KEY " + key;
    }
}
