package com.nexuspvp.gui.styles;
import com.nexuspvp.util.Compat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.gui.ClickGui;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.*;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClassicGuiScreen extends Screen {

    private static final List<ClassicPanel> panels = new ArrayList<>();
    private static boolean initializedPositions = false;
    private static NumberSetting draggingSlider = null;
    private static int draggingSliderX = 0;
    private static int draggingSliderW = 0;

    public ClassicGuiScreen() {
        super(Text.literal("NexusPVP - Classic"));
        if (!initializedPositions || panels.isEmpty()) {
            initDefaultPanels();
            initializedPositions = true;
        }
    }

    private void initDefaultPanels() {
        panels.clear();
        Category[] cats = new Category[]{Category.PVP, Category.HUD, Category.PLAYER, Category.RENDER};
        int startX = 25;
        int spacing = 125;
        int y = 30;

        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            List<Module> modules = NexusPVP.getInstance().getModuleManager().getModulesByCategory(cat);
            List<Module> validMods = new ArrayList<>();
            for (Module m : modules) {
                if (m.getName().equalsIgnoreCase("ClickGuiModule") || m.getName().equalsIgnoreCase("Radio") || m.getName().equalsIgnoreCase("DebugLogger")) {
                    continue;
                }
                validMods.add(m);
            }
            panels.add(new ClassicPanel(cat.name(), startX + i * spacing, y, 115, validMods));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Compat.setContext(context);
        MatrixStack matrices = context.getMatrices();
        RenderUtils.drawRect(matrices, 0, 0, width, height, 0x77000000);

        int btnW = 120;
        int btnH = 20;
        int btnX = width - btnW - 15;
        int btnY = 10;
        boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        int accent = ThemeManager.getInstance().getAccentColor().getRGB();

        RenderUtils.drawRoundedRect(matrices, btnX, btnY, btnW, btnH, 4, btnHover ? accent : 0xEE1E1F22);
        String switchText = "Themes / Styles";
        int stw = textRenderer.getWidth(switchText);
        Compat.drawWithShadow(null, matrices, switchText, btnX + (btnW - stw) / 2, btnY + 6, 0xFFFFFFFF);

        for (ClassicPanel panel : panels) {
            panel.render(matrices, mouseX, mouseY, delta);
        }

        super.render(context, mouseX, mouseY, delta);
        Compat.setContext(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int btnW = 120;
        int btnH = 20;
        int btnX = width - btnW - 15;
        int btnY = 10;
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            if (client != null) {
                ClickGui gui = new ClickGui();
                Compat.setScreen(client, gui);
                gui.switchTab(ClickGui.Tab.THEMES);
            }
            return true;
        }

        for (int i = panels.size() - 1; i >= 0; i--) {
            ClassicPanel panel = panels.get(i);
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                panels.remove(i);
                panels.add(panel);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider != null) {
            double pct = Math.max(0.0, Math.min(1.0, (mouseX - draggingSliderX) / (double) draggingSliderW));
            double val = draggingSlider.getMin() + pct * (draggingSlider.getMax() - draggingSlider.getMin());
            draggingSlider.setValue(val);
            return true;
        }

        for (ClassicPanel panel : panels) {
            if (panel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = null;
        for (ClassicPanel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ClassicPanel panel : panels) {
            if (panel.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean shouldPause() {
        return false;
    }

    private static class ClassicPanel {
        String title;
        int x, y, width;
        int headerHeight = 22;
        boolean collapsed = false;
        boolean dragging = false;
        int dragOffsetX, dragOffsetY;
        List<ClassicModuleEntry> entries = new ArrayList<>();

        ClassicPanel(String title, int x, int y, int width, List<Module> modules) {
            this.title = title;
            this.x = (x);
            this.y = (y);
            this.width = width;
            for (Module m : modules) {
                entries.add(new ClassicModuleEntry(m));
            }
        }

        void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            if (dragging) {
                this.x = (mouseX - dragOffsetX);
                this.y = (mouseY - dragOffsetY);
            }

            int accent = ThemeManager.getInstance().getAccentColor().getRGB();

            RenderUtils.drawRoundedRect(matrices, x, y, width, headerHeight, 3, 0xFF18191C);
            RenderUtils.drawRect(matrices, x, y + headerHeight - 2, width, 2, accent);

            Compat.drawWithShadow(null, matrices, title, x + 8, y + 6, 0xFFF2F3F5);
            String icon = collapsed ? "+" : "-";
            Compat.drawWithShadow(null, matrices, icon, x + width - 14, y + 6, accent);

            if (!collapsed) {
                int curY = y + headerHeight;
                for (ClassicModuleEntry entry : entries) {
                    entry.render(matrices, x, curY, width, mouseX, mouseY);
                    curY += entry.getHeight();
                }
            }
        }

        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight) {
                if (button == 0) {
                    dragging = true;
                    dragOffsetX = (int) mouseX - x;
                    dragOffsetY = (int) mouseY - y;
                    return true;
                } else if (button == 1) {
                    collapsed = !collapsed;
                    return true;
                }
            }

            if (!collapsed && mouseX >= x && mouseX <= x + width) {
                int curY = y + headerHeight;
                for (ClassicModuleEntry entry : entries) {
                    int h = entry.getHeight();
                    if (mouseY >= curY && mouseY <= curY + h) {
                        return entry.mouseClicked(mouseX, mouseY, curY, width, button);
                    }
                    curY += h;
                }
            }

            return false;
        }

        void mouseReleased(double mouseX, double mouseY, int button) {
            dragging = false;
        }

        boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return dragging;
        }

        boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            for (ClassicModuleEntry entry : entries) {
                if (entry.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class ClassicModuleEntry {
        Module module;
        boolean expanded = false;
        boolean binding = false;

        ClassicModuleEntry(Module module) {
            this.module = module;
        }

        int getHeight() {
            int base = 18;
            if (expanded) {
                for (Setting<?> s : module.getSettings()) {
                    if (s instanceof NumberSetting) base += 24;
                    else base += 18;
                }
            }
            return base;
        }

        void render(MatrixStack matrices, int x, int y, int width, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 18;
            int accent = ThemeManager.getInstance().getAccentColor().getRGB();
            boolean enabled = module.isEnabled();

            int bg = enabled ? (accent & 0x55FFFFFF) | 0x88000000 : (hovered ? 0xEE2B2D31 : 0xDD1E1F22);
            RenderUtils.drawRect(matrices, x, y, width, 18, bg);

            if (enabled) {
                RenderUtils.drawRect(matrices, x, y, 2, 18, accent);
            }

            String name = module.getName();
            int nameCol = enabled ? accent : (hovered ? 0xFFFFFFFF : 0xFFDBDEE1);
            Compat.drawWithShadow(null, matrices, name, x + 6, y + 5, nameCol);

            String rightIcon = binding ? "..." : (module.getKeyBind() > 0 ? "[" + GLFW.glfwGetKeyName(module.getKeyBind(), 0) + "]" : (!module.getSettings().isEmpty() ? (expanded ? "v" : ">") : ""));
            if (rightIcon != null && !rightIcon.isEmpty()) {
                int rw = MinecraftClient.getInstance().textRenderer.getWidth(rightIcon);
                Compat.drawWithShadow(null, matrices, rightIcon, x + width - rw - 5, y + 5, binding ? accent : 0xFF949BA4);
            }

            // Render rich interactive inline settings if expanded!
            if (expanded) {
                int sY = y + 18;
                for (Setting<?> s : module.getSettings()) {
                    if (s instanceof BooleanSetting) {
                        BooleanSetting bs = (BooleanSetting) s;
                        RenderUtils.drawRect(matrices, x, sY, width, 18, 0xEE141518);
                        
                        // Checkbox
                        RenderUtils.drawRoundedRect(matrices, x + 6, sY + 4, 10, 10, 2, bs.isEnabled() ? accent : 0xFF35373C);
                        if (bs.isEnabled()) {
                            Compat.drawWithShadow(null, matrices, "\u2714", x + 8, sY + 5, 0xFFFFFFFF);
                        }

                        String sName = s.getName();
                        if (sName.length() > 14) sName = sName.substring(0, 12) + "..";
                        Compat.drawWithShadow(null, matrices, sName, x + 20, sY + 5, bs.isEnabled() ? 0xFFFFFFFF : 0xFF949BA4);
                        sY += 18;
                    } else if (s instanceof NumberSetting) {
                        NumberSetting ns = (NumberSetting) s;
                        RenderUtils.drawRect(matrices, x, sY, width, 24, 0xEE141518);

                        String sName = s.getName() + ": " + String.format("%.1f", ns.getValue());
                        if (sName.length() > 18) sName = sName.substring(0, 16) + "..";
                        Compat.drawWithShadow(null, matrices, sName, x + 6, sY + 3, accent);

                        int sliderX = x + 6;
                        int sliderY = sY + 14;
                        int sliderW = width - 12;
                        RenderUtils.drawRoundedRect(matrices, sliderX, sliderY, sliderW, 4, 2, 0xFF2B2D31);

                        float pct = (float) ((ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin()));
                        int fillW = (int) (sliderW * Math.max(0, Math.min(1, pct)));
                        if (fillW > 0) {
                            RenderUtils.drawRoundedRect(matrices, sliderX, sliderY, fillW, 4, 2, accent);
                        }
                        RenderUtils.drawRoundedRect(matrices, sliderX + Math.max(0, fillW - 3), sliderY - 1, 6, 6, 3, 0xFFFFFFFF);

                        sY += 24;
                    } else if (s instanceof ModeSetting) {
                        ModeSetting ms = (ModeSetting) s;
                        RenderUtils.drawRect(matrices, x, sY, width, 18, 0xEE141518);

                        String sText = s.getName() + ": " + ms.getValue();
                        if (sText.length() > 16) sText = sText.substring(0, 14) + "..";
                        Compat.drawWithShadow(null, matrices, sText, x + 6, sY + 5, accent);
                        sY += 18;
                    } else if (s instanceof ColorSetting) {
                        ColorSetting cs = (ColorSetting) s;
                        RenderUtils.drawRect(matrices, x, sY, width, 18, 0xEE141518);

                        Compat.drawWithShadow(null, matrices, s.getName(), x + 6, sY + 5, 0xFFDBDEE1);
                        RenderUtils.drawRoundedRect(matrices, x + width - 20, sY + 4, 14, 10, 2, cs.getColor().getRGB());
                        sY += 18;
                    }
                }
            }
        }

        boolean mouseClicked(double mouseX, double mouseY, int entryY, int width, int button) {
            if (mouseY >= entryY && mouseY <= entryY + 18) {
                if (button == 0) {
                    module.toggle();
                    return true;
                } else if (button == 1) {
                    if (!module.getSettings().isEmpty()) {
                        expanded = !expanded;
                    }
                    return true;
                } else if (button == 2) {
                    binding = !binding;
                    return true;
                }
            }

            if (expanded && mouseY > entryY + 18) {
                int sY = entryY + 18;
                for (Setting<?> s : module.getSettings()) {
                    if (s instanceof BooleanSetting) {
                        if (mouseY >= sY && mouseY <= sY + 18) {
                            ((BooleanSetting) s).toggle();
                            return true;
                        }
                        sY += 18;
                    } else if (s instanceof NumberSetting) {
                        NumberSetting ns = (NumberSetting) s;
                        if (mouseY >= sY && mouseY <= sY + 24) {
                            int sliderX = (int) (mouseX - 10);
                            draggingSlider = ns;
                            draggingSliderX = (int) (mouseX - 10);
                            draggingSliderW = width - 12;
                            double pct = Math.max(0.0, Math.min(1.0, (mouseX - (mouseX - 10)) / (double) (width - 12)));
                            double val = ns.getMin() + pct * (ns.getMax() - ns.getMin());
                            ns.setValue(val);
                            return true;
                        }
                        sY += 24;
                    } else if (s instanceof ModeSetting) {
                        if (mouseY >= sY && mouseY <= sY + 18) {
                            ((ModeSetting) s).cycle();
                            return true;
                        }
                        sY += 18;
                    } else if (s instanceof ColorSetting) {
                        if (mouseY >= sY && mouseY <= sY + 18) {
                            ((ColorSetting) s).cycle();
                            return true;
                        }
                        sY += 18;
                    }
                }
            }

            return false;
        }

        boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
    }

    @Override
    public void close() {
        if (this.client != null) {
            Compat.setScreen(client, null);
        }
        if (NexusPVP.getInstance().getConfigManager() != null) {
            NexusPVP.getInstance().getConfigManager().saveConfig();
        }
        NexusPVP.getInstance().getModuleManager().getModuleByName("ClickGui").ifPresent(m -> {
            if (m.isEnabled()) {
                m.toggle();
            }
        });
    }

    

}
