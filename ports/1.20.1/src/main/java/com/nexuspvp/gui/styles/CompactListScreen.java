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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CompactListScreen extends Screen {

    private String filterCategory = "ALL";
    private int scrollY = 0;
    private final List<Module> allModules = new ArrayList<>();
    private Module bindingModule = null;

    // Settings Drawer
    private Module selectedModule = null;
    private NumberSetting draggingSlider = null;
    private int draggingSliderX = 0;
    private int draggingSliderW = 0;
    private int drawerScrollY = 0;

    public CompactListScreen() {
        super(Text.literal("NexusPVP - Compact"));
        for (Module m : NexusPVP.getInstance().getModuleManager().getModules()) {
            if (m.getName().equalsIgnoreCase("ClickGuiModule") || m.getName().equalsIgnoreCase("Radio") || m.getName().equalsIgnoreCase("DebugLogger")) {
                continue;
            }
            allModules.add(m);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        int accent = ThemeManager.getInstance().getAccentColor().getRGB();

        RenderUtils.drawRect(matrices, 0, 0, width, height, 0x88111214);

        int winW = (selectedModule != null) ? 460 : 380;
        int winH = 290;
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;

        // Window Frame
        RenderUtils.drawRoundedRect(matrices, winX - 1, winY - 1, winW + 2, winH + 2, 4, accent);
        RenderUtils.drawRoundedRect(matrices, winX, winY, winW, winH, 3, 0xFF18191C);

        // Header (Logo + Tabs)
        int headerH = 26;
        RenderUtils.drawRect(matrices, winX, winY, winW, headerH, 0xFF121315);
        Compat.drawWithShadow(null, matrices, "GLEBKA", winX + 10, winY + 9, accent);
        Compat.drawWithShadow(null, matrices, "LIST", winX + 54, winY + 9, 0xFFFFFFFF);

        // Category Filter Pills
        String[] cats = new String[]{"ALL", "PVP", "HUD", "PLAYER", "RENDER", "THEMES"};
        int startX = winX + 90;
        int tabW = 40;
        for (int i = 0; i < cats.length; i++) {
            String c = cats[i];
            int tx = startX + i * (tabW + 4);
            int ty = winY + 5;
            boolean active = filterCategory.equalsIgnoreCase(c);
            boolean hovered = mouseX >= tx && mouseX <= tx + tabW && mouseY >= ty && mouseY <= ty + 16;

            int bg = active ? accent : (hovered ? 0xFF2B2D31 : 0xFF1E1F22);
            RenderUtils.drawRoundedRect(matrices, tx, ty, tabW, 16, 2, bg);
            int tw = textRenderer.getWidth(c);
            Compat.drawWithShadow(null, matrices, c, tx + (tabW - tw) / 2, ty + 4, active ? 0xFFFFFFFF : 0xFF949BA4);
        }

        // Module Rows Area
        int listW = (selectedModule != null) ? 260 : winW - 16;
        int contentY = winY + headerH + 6;
        int contentH = winH - headerH - 12;

        RenderUtils.startScissor(winX + 4, contentY, listW, contentH);

        if (filterCategory.equalsIgnoreCase("THEMES")) {
            renderStyleSelector(matrices, winX + 10, contentY + scrollY, listW - 12, mouseX, mouseY);
        } else {
            List<Module> filtered = new ArrayList<>();
            for (Module m : allModules) {
                if (filterCategory.equalsIgnoreCase("ALL") || m.getCategory().name().equalsIgnoreCase(filterCategory)) {
                    filtered.add(m);
                }
            }

            int rowH = 22;
            for (int i = 0; i < filtered.size(); i++) {
                Module m = filtered.get(i);
                int ry = contentY + scrollY + i * (rowH + 2);
                boolean hovered = mouseX >= winX + 8 && mouseX <= winX + listW && mouseY >= ry && mouseY <= ry + rowH;
                boolean enabled = m.isEnabled();
                boolean isSelected = (m == selectedModule);

                int rowBg = isSelected ? 0xFF353C4D : (enabled ? (accent & 0x33FFFFFF) | 0xDD202225 : (hovered ? 0xDD2B2D31 : 0xBB1E1F22));
                RenderUtils.drawRoundedRect(matrices, winX + 8, ry, listW - 8, rowH, 3, rowBg);

                if (enabled) {
                    RenderUtils.drawRect(matrices, winX + 8, ry, 2, rowH, accent);
                }

                // Checkbox status indicator
                int chkX = winX + 14;
                int chkY = ry + 6;
                RenderUtils.drawRoundedRect(matrices, chkX, chkY, 10, 10, 2, enabled ? accent : 0xFF35373C);
                if (enabled) {
                    Compat.drawWithShadow(null, matrices, "\u2714", chkX + 2, chkY + 1, 0xFFFFFFFF);
                }

                // Module name
                Compat.drawWithShadow(null, matrices, m.getName(), winX + 28, ry + 7, enabled ? 0xFFFFFFFF : 0xFFDBDEE1);

                // Settings gear
                int gearX = winX + listW - 40;
                Compat.drawWithShadow(null, matrices, "\u2699", gearX, ry + 7, isSelected ? accent : 0xFF8A93A4);

                // Keybind badge
                boolean isBindingThis = (bindingModule == m);
                String keyText = isBindingThis ? "..." : (m.getKeyBind() > 0 ? "[" + GLFW.glfwGetKeyName(m.getKeyBind(), 0) + "]" : "[-]");
                int kw = textRenderer.getWidth(keyText);
                int kx = winX + listW - kw - 12;
                Compat.drawWithShadow(null, matrices, keyText, kx, ry + 7, isBindingThis ? accent : 0xFF949BA4);
            }
        }

        RenderUtils.endScissor();

        // Settings Slide-out Drawer on the right!
        if (selectedModule != null) {
            int drawX = winX + 266;
            int drawY = winY + headerH + 6;
            int drawW = winW - 274;
            int drawH = winH - headerH - 12;

            RenderUtils.drawRoundedRect(matrices, drawX, drawY, drawW, drawH, 4, 0xFF14161C);
            RenderUtils.drawRect(matrices, drawX, drawY, 1, drawH, 0x33FFFFFF);

            // Drawer Header
            Compat.drawWithShadow(null, matrices, selectedModule.getName(), drawX + 8, drawY + 8, accent);
            Compat.drawWithShadow(null, matrices, "X", drawX + drawW - 14, drawY + 8, 0xFFED4245);

            RenderUtils.startScissor(drawX, drawY + 22, drawW, drawH - 24);
            int curY = drawY + 24 + drawerScrollY;

            List<Setting<?>> settings = selectedModule.getSettings();
            if (settings.isEmpty()) {
                Compat.drawWithShadow(null, matrices, "No settings", drawX + 10, drawY + 30, 0xFF8A93A4);
            } else {
                for (Setting<?> s : settings) {
                    if (s instanceof BooleanSetting) {
                        BooleanSetting bs = (BooleanSetting) s;
                        RenderUtils.drawRoundedRect(matrices, drawX + 6, curY, drawW - 12, 20, 3, 0xFF1E212A);
                        Compat.drawWithShadow(null, matrices, s.getName(), drawX + 10, curY + 6, 0xFFDBDEE1);

                        int chkX = drawX + drawW - 22;
                        RenderUtils.drawRoundedRect(matrices, chkX, curY + 5, 10, 10, 2, bs.isEnabled() ? accent : 0xFF35373C);
                        if (bs.isEnabled()) {
                            Compat.drawWithShadow(null, matrices, "\u2714", chkX + 2, curY + 5, 0xFFFFFFFF);
                        }
                        curY += 24;
                    } else if (s instanceof NumberSetting) {
                        NumberSetting ns = (NumberSetting) s;
                        RenderUtils.drawRoundedRect(matrices, drawX + 6, curY, drawW - 12, 30, 3, 0xFF1E212A);
                        Compat.drawWithShadow(null, matrices, s.getName(), drawX + 10, curY + 4, 0xFFDBDEE1);

                        String valStr = String.format("%.1f", ns.getValue());
                        int vw = textRenderer.getWidth(valStr);
                        Compat.drawWithShadow(null, matrices, valStr, drawX + drawW - vw - 12, curY + 4, accent);

                        int sliderX = drawX + 10;
                        int sliderY = curY + 18;
                        int sliderW = drawW - 20;
                        RenderUtils.drawRoundedRect(matrices, sliderX, sliderY, sliderW, 4, 2, 0xFF2B2D31);

                        float pct = (float) ((ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin()));
                        int fillW = (int) (sliderW * Math.max(0, Math.min(1, pct)));
                        if (fillW > 0) {
                            RenderUtils.drawRoundedRect(matrices, sliderX, sliderY, fillW, 4, 2, accent);
                        }
                        RenderUtils.drawRoundedRect(matrices, sliderX + Math.max(0, fillW - 3), sliderY - 1, 6, 6, 3, 0xFFFFFFFF);
                        curY += 34;
                    } else if (s instanceof ModeSetting) {
                        ModeSetting ms = (ModeSetting) s;
                        RenderUtils.drawRoundedRect(matrices, drawX + 6, curY, drawW - 12, 22, 3, 0xFF1E212A);
                        Compat.drawWithShadow(null, matrices, s.getName() + ": " + ms.getValue(), drawX + 10, curY + 7, accent);
                        curY += 26;
                    } else if (s instanceof ColorSetting) {
                        ColorSetting cs = (ColorSetting) s;
                        RenderUtils.drawRoundedRect(matrices, drawX + 6, curY, drawW - 12, 20, 3, 0xFF1E212A);
                        Compat.drawWithShadow(null, matrices, s.getName(), drawX + 10, curY + 6, 0xFFDBDEE1);
                        RenderUtils.drawRoundedRect(matrices, drawX + drawW - 24, curY + 5, 14, 10, 2, cs.getColor().getRGB());
                        curY += 24;
                    }
                }
            }
            RenderUtils.endScissor();
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderStyleSelector(MatrixStack matrices, int x, int y, int w, int mouseX, int mouseY) {
        Compat.drawWithShadow(null, matrices, "SELECT GUI STYLE:", x + 4, y + 4, 0xFFFFFFFF);

        com.nexuspvp.gui.GuiStyle[] styles = com.nexuspvp.gui.GuiStyle.values();
        int btnH = 34;

        for (int i = 0; i < styles.length; i++) {
            com.nexuspvp.gui.GuiStyle s = styles[i];
            int by = y + 20 + i * (btnH + 6);
            boolean active = ThemeManager.getInstance().getCurrentStyle() == s;
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= by && mouseY <= by + btnH;
            int accent = ThemeManager.getInstance().getAccentColor().getRGB();

            RenderUtils.drawRoundedRect(matrices, x, by, w, btnH, 3, active ? accent : (hovered ? 0xFF2B2D31 : 0xFF1E1F22));
            Compat.drawWithShadow(null, matrices, s.getIcon() + " " + s.getDisplayName() + " - " + s.getDescription(), x + 8, by + 12, active ? 0xFFFFFFFF : 0xFFD8DEE9);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int winW = (selectedModule != null) ? 460 : 380;
        int winH = 290;
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;
        int headerH = 26;

        // Category tabs click
        String[] cats = new String[]{"ALL", "PVP", "HUD", "PLAYER", "RENDER", "THEMES"};
        int startX = winX + 90;
        int tabW = 40;
        for (int i = 0; i < cats.length; i++) {
            String c = cats[i];
            int tx = startX + i * (tabW + 4);
            int ty = winY + 5;
            if (mouseX >= tx && mouseX <= tx + tabW && mouseY >= ty && mouseY <= ty + 16) {
                filterCategory = c;
                scrollY = 0;
                bindingModule = null;
                return true;
            }
        }

        int contentY = winY + headerH + 6;

        // Drawer click
        if (selectedModule != null) {
            int drawX = winX + 266;
            int drawY = winY + headerH + 6;
            int drawW = winW - 274;
            int drawH = winH - headerH - 12;

            // Close button
            if (mouseX >= drawX + drawW - 16 && mouseX <= drawX + drawW && mouseY >= drawY && mouseY <= drawY + 18) {
                selectedModule = null;
                return true;
            }

            int curY = drawY + 24 + drawerScrollY;
            for (Setting<?> s : selectedModule.getSettings()) {
                if (s instanceof BooleanSetting) {
                    if (mouseX >= drawX + 6 && mouseX <= drawX + drawW - 6 && mouseY >= curY && mouseY <= curY + 20) {
                        ((BooleanSetting) s).toggle();
                        return true;
                    }
                    curY += 24;
                } else if (s instanceof NumberSetting) {
                    NumberSetting ns = (NumberSetting) s;
                    int sliderX = drawX + 10;
                    int sliderY = curY + 16;
                    int sliderW = drawW - 20;
                    if (mouseX >= sliderX && mouseX <= sliderX + sliderW && mouseY >= sliderY - 4 && mouseY <= sliderY + 10) {
                        draggingSlider = ns;
                        draggingSliderX = sliderX;
                        draggingSliderW = sliderW;
                        double pct = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / (double) sliderW));
                        double val = ns.getMin() + pct * (ns.getMax() - ns.getMin());
                        ns.setValue(val);
                        return true;
                    }
                    curY += 34;
                } else if (s instanceof ModeSetting) {
                    if (mouseX >= drawX + 6 && mouseX <= drawX + drawW - 6 && mouseY >= curY && mouseY <= curY + 22) {
                        ((ModeSetting) s).cycle();
                        return true;
                    }
                    curY += 26;
                } else if (s instanceof ColorSetting) {
                    if (mouseX >= drawX + 6 && mouseX <= drawX + drawW - 6 && mouseY >= curY && mouseY <= curY + 20) {
                        ((ColorSetting) s).cycle();
                        return true;
                    }
                    curY += 24;
                }
            }
        }

        if (filterCategory.equalsIgnoreCase("THEMES")) {
            com.nexuspvp.gui.GuiStyle[] styles = com.nexuspvp.gui.GuiStyle.values();
            int btnH = 34;
            int x = winX + 10;
            int w = winW - 20;

            for (int i = 0; i < styles.length; i++) {
                com.nexuspvp.gui.GuiStyle s = styles[i];
                int by = contentY + scrollY + 20 + i * (btnH + 6);
                if (mouseX >= x && mouseX <= x + w && mouseY >= by && mouseY <= by + btnH) {
                    ThemeManager.getInstance().setStyle(s);
                    if (client != null) {
                        ClickGui.openCurrentStyleScreen();
                    }
                    return true;
                }
            }
        } else {
            int listW = (selectedModule != null) ? 260 : winW - 16;
            List<Module> filtered = new ArrayList<>();
            for (Module m : allModules) {
                if (filterCategory.equalsIgnoreCase("ALL") || m.getCategory().name().equalsIgnoreCase(filterCategory)) {
                    filtered.add(m);
                }
            }

            int rowH = 22;
            for (int i = 0; i < filtered.size(); i++) {
                Module m = filtered.get(i);
                int ry = contentY + scrollY + i * (rowH + 2);
                if (mouseX >= winX + 8 && mouseX <= winX + listW && mouseY >= ry && mouseY <= ry + rowH) {
                    if (button == 1 || (mouseX >= winX + listW - 40 && mouseX <= winX + listW - 20)) {
                        selectedModule = (selectedModule == m) ? null : m;
                        drawerScrollY = 0;
                        return true;
                    } else if (button == 0) {
                        m.toggle();
                        return true;
                    } else if (button == 2) {
                        bindingModule = (bindingModule == m) ? null : m;
                        return true;
                    }
                }
            }
        }

        bindingModule = null;
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
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (selectedModule != null && mouseX >= (width - 460) / 2 + 266) {
            drawerScrollY += (int) (amount * 16);
            if (drawerScrollY > 0) drawerScrollY = 0;
            return true;
        }
        scrollY += (int) (amount * 20);
        if (scrollY > 0) scrollY = 0;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                bindingModule.setKeyBind(0);
            } else {
                bindingModule.setKeyBind(keyCode);
            }
            bindingModule = null;
            return true;
        }

        if (selectedModule != null && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            selectedModule = null;
            return true;
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

    @Override
    public void close() {
        if (this.client != null) {
            Compat.setScreen(client, null);
        }
        if (NexusPVP.getInstance().getConfigManager() != null) {
            NexusPVP.getInstance().getConfigManager().saveConfig();
        }
}
