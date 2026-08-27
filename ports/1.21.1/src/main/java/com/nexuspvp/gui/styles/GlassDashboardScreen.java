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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlassDashboardScreen extends Screen {

    public enum DashTab {
        PVP("PVP & COMBAT", Category.PVP),
        HUD("HUD & SCREEN", Category.HUD),
        PLAYER("PLAYER & HANDS", Category.PLAYER),
        RENDER("WORLD & RENDER", Category.RENDER),
        THEMES("THEMES / STYLES", null);

        private final String title;
        private final Category category;

        DashTab(String title, Category category) {
            this.title = title;
            this.category = category;
        }
    }

    private static DashTab currentTab = DashTab.PVP;
    private int scrollY = 0;
    private final Map<Category, List<Module>> categoryModules = new HashMap<>();

    // Settings Modal State
    private Module selectedModule = null;
    private Setting<?> draggingSlider = null;
    private boolean bindingKey = false;
    private int modalScrollY = 0;

    public GlassDashboardScreen() {
        super(Text.literal("NexusPVP - Glass Dashboard"));
        for (Category cat : Category.values()) {
            List<Module> mods = NexusPVP.getInstance().getModuleManager().getModulesByCategory(cat);
            List<Module> valid = new ArrayList<>();
            for (Module m : mods) {
                if (m.getName().equalsIgnoreCase("ClickGuiModule") || m.getName().equalsIgnoreCase("Radio") || m.getName().equalsIgnoreCase("DebugLogger")) {
                    continue;
                }
                valid.add(m);
            }
            categoryModules.put(cat, valid);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Compat.setContext(context);
        MatrixStack matrices = context.getMatrices();
        int accent = ThemeManager.getInstance().getAccentColor().getRGB();

        // 1. Dark translucent backdrop
        RenderUtils.drawRect(matrices, 0, 0, width, height, 0x880B0E14);

        // 2. Central Glass Container (Width: 540, Height: 340)
        int panelW = 540;
        int panelH = 340;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        RenderUtils.drawRoundedRect(matrices, panelX - 1, panelY - 1, panelW + 2, panelH + 2, 8, 0x33FFFFFF);
        RenderUtils.drawRoundedRect(matrices, panelX, panelY, panelW, panelH, 7, 0xDD141820);

        // 3. Top Navigation Bar
        int navH = 36;
        RenderUtils.drawRoundedRect(matrices, panelX, panelY, panelW, navH, 7, 0xEE0D1017);
        RenderUtils.drawRect(matrices, panelX, panelY + navH - 1, panelW, 1, 0x22FFFFFF);

        // Logo
        Compat.drawWithShadow(null, matrices, "GLEBKA", panelX + 16, panelY + 12, accent);
        Compat.drawWithShadow(null, matrices, "VISUALS", panelX + 62, panelY + 12, 0xFFFFFFFF);

        // Nav Tabs
        DashTab[] tabs = DashTab.values();
        int tabStartX = panelX + 130;
        int tabW = 75;

        for (int i = 0; i < tabs.length; i++) {
            DashTab t = tabs[i];
            int tx = tabStartX + i * (tabW + 6);
            int ty = panelY + 7;
            boolean active = (t == currentTab);
            boolean hovered = mouseX >= tx && mouseX <= tx + tabW && mouseY >= ty && mouseY <= ty + 22;

            if (active) {
                RenderUtils.drawRoundedRect(matrices, tx, ty, tabW, 22, 4, accent);
            } else if (hovered) {
                RenderUtils.drawRoundedRect(matrices, tx, ty, tabW, 22, 4, 0x33FFFFFF);
            }

            int tw = textRenderer.getWidth(t.title);
            Compat.drawWithShadow(null, matrices, t.title, tx + (tabW - tw) / 2, ty + 6, active ? 0xFFFFFFFF : 0xFF949BA4);
        }

        // 4. Content Area (2-Column Grid)
        int contentX = panelX + 16;
        int contentY = panelY + navH + 12;
        int contentW = panelW - 32;
        int contentH = panelH - navH - 24;

        RenderUtils.startScissor(contentX - 2, contentY - 2, contentW + 4, contentH + 4);

        if (currentTab == DashTab.THEMES) {
            renderThemesTab(matrices, contentX, contentY + scrollY, contentW, mouseX, mouseY);
        } else {
            List<Module> mods = categoryModules.get(currentTab.category);
            if (mods != null) {
                int colW = (contentW - 12) / 2;
                int cardH = 48;

                for (int i = 0; i < mods.size(); i++) {
                    Module m = mods.get(i);
                    int col = i % 2;
                    int row = i / 2;

                    int cardX = contentX + col * (colW + 12);
                    int cardY = contentY + scrollY + row * (cardH + 8);

                    boolean hovered = mouseX >= cardX && mouseX <= cardX + colW && mouseY >= cardY && mouseY <= cardY + cardH;
                    boolean enabled = m.isEnabled();

                    int cardBg = enabled ? (accent & 0x22FFFFFF) | 0xDD1E232E : (hovered ? 0xDD252C3A : 0xBB191E28);
                    int borderCol = enabled ? accent : (hovered ? 0x44FFFFFF : 0x22FFFFFF);

                    RenderUtils.drawRoundedRect(matrices, cardX - 1, cardY - 1, colW + 2, cardH + 2, 5, borderCol);
                    RenderUtils.drawRoundedRect(matrices, cardX, cardY, colW, cardH, 4, cardBg);

                    // Module Title & Desc
                    Compat.drawWithShadow(null, matrices, m.getName(), cardX + 10, cardY + 8, enabled ? 0xFFFFFFFF : 0xFFDBDEE1);
                    String desc = m.getDescription();
                    if (desc != null && textRenderer.getWidth(desc) > colW - 70) {
                        desc = desc.substring(0, Math.min(desc.length(), 22)) + "..";
                    }
                    Compat.drawWithShadow(null, matrices, desc, cardX + 10, cardY + 22, 0xFF8A93A4);

                    // Settings Gear Button
                    int gearW = 16;
                    int gearH = 16;
                    int gearX = cardX + colW - 56;
                    int gearY = cardY + 16;
                    boolean gearHover = mouseX >= gearX && mouseX <= gearX + gearW && mouseY >= gearY && mouseY <= gearY + gearH;
                    RenderUtils.drawRoundedRect(matrices, gearX, gearY, gearW, gearH, 3, gearHover ? 0xFF404654 : 0xFF2A2F3D);
                    Compat.drawWithShadow(null, matrices, "\u2699", gearX + 4, gearY + 3, gearHover ? accent : 0xFF949BA4);

                    // iOS-style Toggle Switch
                    int swW = 28;
                    int swH = 16;
                    int swX = cardX + colW - swW - 8;
                    int swY = cardY + (cardH - swH) / 2;

                    RenderUtils.drawRoundedRect(matrices, swX, swY, swW, swH, 8, enabled ? accent : 0xFF353C4D);
                    int knobX = enabled ? swX + swW - 14 : swX + 2;
                    RenderUtils.drawRoundedRect(matrices, knobX, swY + 2, 12, 12, 6, 0xFFFFFFFF);
                }
            }
        }

        RenderUtils.endScissor();

        // 5. Render Glass Settings Modal if a module is selected!
        if (selectedModule != null) {
            renderSettingsModal(matrices, panelX, panelY, panelW, panelH, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
        Compat.setContext(null);
    }

    private void renderSettingsModal(MatrixStack matrices, int px, int py, int pw, int ph, int mouseX, int mouseY) {
        int accent = ThemeManager.getInstance().getAccentColor().getRGB();

        // Dark dim backdrop over main panel
        RenderUtils.drawRoundedRect(matrices, px, py, pw, ph, 7, 0xAA0A0D14);

        // Centered Modal Window (Width: 360, Height: 260)
        int mw = 360;
        int mh = 260;
        int mx = px + (pw - mw) / 2;
        int my = py + (ph - mh) / 2;

        RenderUtils.drawRoundedRect(matrices, mx - 1, my - 1, mw + 2, mh + 2, 7, accent);
        RenderUtils.drawRoundedRect(matrices, mx, my, mw, mh, 6, 0xFF161A24);

        // Modal Header
        int mHeadH = 32;
        RenderUtils.drawRoundedRect(matrices, mx, my, mw, mHeadH, 6, 0xFF10141C);
        RenderUtils.drawRect(matrices, mx, my + mHeadH - 1, mw, 1, 0x22FFFFFF);

        Compat.drawWithShadow(null, matrices, selectedModule.getName() + " Settings", mx + 12, my + 10, 0xFFFFFFFF);

        // Keybind button in header
        String keyText = bindingKey ? "..." : (selectedModule.getKeyBind() > 0 ? "[" + GLFW.glfwGetKeyName(selectedModule.getKeyBind(), 0) + "]" : "[BIND]");
        int kw = textRenderer.getWidth(keyText);
        int kx = mx + mw - kw - 38;
        int ky = my + 8;
        RenderUtils.drawRoundedRect(matrices, kx - 3, ky - 1, kw + 6, 16, 3, bindingKey ? accent : 0xFF2A2E3B);
        Compat.drawWithShadow(null, matrices, keyText, kx, ky + 3, bindingKey ? 0xFFFFFFFF : accent);

        // Close Button 'X'
        int closeX = mx + mw - 24;
        int closeY = my + 8;
        boolean closeHover = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
        Compat.drawWithShadow(null, matrices, "X", closeX + 4, closeY + 3, closeHover ? 0xFFED4245 : 0xFF949BA4);

        // Modal Settings List
        int sListX = mx + 12;
        int sListY = my + mHeadH + 8;
        int sListW = mw - 24;
        int sListH = mh - mHeadH - 16;

        RenderUtils.startScissor(sListX - 2, sListY - 2, sListW + 4, sListH + 4);

        int curY = sListY + modalScrollY;
        List<Setting<?>> settings = selectedModule.getSettings();

        if (settings.isEmpty()) {
            Compat.drawWithShadow(null, matrices, "No configurable settings for this module.", sListX + 10, sListY + 20, 0xFF8A93A4);
        } else {
            for (Setting<?> s : settings) {
                if (s instanceof BooleanSetting) {
                    BooleanSetting bs = (BooleanSetting) s;
                    boolean val = bs.isEnabled();
                    int rowH = 24;

                    RenderUtils.drawRoundedRect(matrices, sListX, curY, sListW, rowH, 4, 0xFF1E2330);
                    Compat.drawWithShadow(null, matrices, s.getName(), sListX + 10, curY + 7, 0xFFDBDEE1);

                    int swW = 24;
                    int swH = 12;
                    int swX = sListX + sListW - swW - 8;
                    int swY = curY + 6;
                    RenderUtils.drawRoundedRect(matrices, swX, swY, swW, swH, 6, val ? accent : 0xFF353C4D);
                    int knobX = val ? swX + swW - 10 : swX + 2;
                    RenderUtils.drawRoundedRect(matrices, knobX, swY + 1, 8, 10, 4, 0xFFFFFFFF);

                    curY += rowH + 6;
                } else if (s instanceof NumberSetting) {
                    NumberSetting ns = (NumberSetting) s;
                    int rowH = 34;

                    RenderUtils.drawRoundedRect(matrices, sListX, curY, sListW, rowH, 4, 0xFF1E2330);
                    Compat.drawWithShadow(null, matrices, s.getName(), sListX + 10, curY + 5, 0xFFDBDEE1);

                    String valStr = String.format("%.2f", ns.getValue());
                    int vw = textRenderer.getWidth(valStr);
                    Compat.drawWithShadow(null, matrices, valStr, sListX + sListW - vw - 10, curY + 5, accent);

                    int sliderX = sListX + 10;
                    int sliderY = curY + 20;
                    int sliderW = sListW - 20;
                    RenderUtils.drawRoundedRect(matrices, sliderX, sliderY, sliderW, 4, 2, 0xFF2B3245);

                    float pct = (float) ((ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin()));
                    int fillW = (int) (sliderW * Math.max(0, Math.min(1, pct)));
                    if (fillW > 0) {
                        RenderUtils.drawRoundedRect(matrices, sliderX, sliderY, fillW, 4, 2, accent);
                    }
                    RenderUtils.drawRoundedRect(matrices, sliderX + Math.max(0, fillW - 4), sliderY - 2, 8, 8, 4, 0xFFFFFFFF);

                    curY += rowH + 6;
                } else if (s instanceof ModeSetting) {
                    ModeSetting ms = (ModeSetting) s;
                    int rowH = 26;

                    RenderUtils.drawRoundedRect(matrices, sListX, curY, sListW, rowH, 4, 0xFF1E2330);
                    Compat.drawWithShadow(null, matrices, s.getName(), sListX + 10, curY + 8, 0xFFDBDEE1);

                    String modeVal = "< " + ms.getValue() + " >";
                    int mw2 = textRenderer.getWidth(modeVal);
                    int mx2 = sListX + sListW - mw2 - 10;
                    RenderUtils.drawRoundedRect(matrices, mx2 - 4, curY + 4, mw2 + 8, 16, 3, 0xFF2B3245);
                    Compat.drawWithShadow(null, matrices, modeVal, mx2, curY + 8, accent);

                    curY += rowH + 6;
                } else if (s instanceof ColorSetting) {
                    ColorSetting cs = (ColorSetting) s;
                    int rowH = 26;

                    RenderUtils.drawRoundedRect(matrices, sListX, curY, sListW, rowH, 4, 0xFF1E2330);
                    Compat.drawWithShadow(null, matrices, s.getName(), sListX + 10, curY + 8, 0xFFDBDEE1);

                    int colBoxW = 30;
                    int colBoxH = 14;
                    int colBoxX = sListX + sListW - colBoxW - 10;
                    int colBoxY = curY + 6;
                    RenderUtils.drawRoundedRect(matrices, colBoxX, colBoxY, colBoxW, colBoxH, 3, cs.getColor().getRGB());

                    curY += rowH + 6;
                }
            }
        }

        RenderUtils.endScissor();
    }

    private void renderThemesTab(MatrixStack matrices, int x, int y, int w, int mouseX, int mouseY) {
        Compat.drawWithShadow(null, matrices, "SELECT GUI LAYOUT STYLE:", x + 4, y + 4, 0xFFFFFFFF);

        com.nexuspvp.gui.GuiStyle[] styles = com.nexuspvp.gui.GuiStyle.values();
        int btnW = (w - 12) / 2;
        int btnH = 40;

        for (int i = 0; i < styles.length; i++) {
            com.nexuspvp.gui.GuiStyle s = styles[i];
            int col = i % 2;
            int row = i / 2;
            int bx = x + col * (btnW + 12);
            int by = y + 22 + row * (btnH + 8);

            boolean active = ThemeManager.getInstance().getCurrentStyle() == s;
            boolean hovered = mouseX >= bx && mouseX <= bx + btnW && mouseY >= by && mouseY <= by + btnH;
            int accent = ThemeManager.getInstance().getAccentColor().getRGB();

            RenderUtils.drawRoundedRect(matrices, bx, by, btnW, btnH, 5, active ? accent : (hovered ? 0xFF2E3440 : 0xFF1C212B));
            Compat.drawWithShadow(null, matrices, s.getIcon() + " " + s.getDisplayName(), bx + 10, by + 8, active ? 0xFFFFFFFF : 0xFFD8DEE9);
            Compat.drawWithShadow(null, matrices, s.getDescription(), bx + 10, by + 22, 0xFF8892B0);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelW = 540;
        int panelH = 340;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        if (selectedModule != null) {
            int mw = 360;
            int mh = 260;
            int mx = panelX + (panelW - mw) / 2;
            int my = panelY + (panelH - mh) / 2;

            int closeX = mx + mw - 24;
            int closeY = my + 8;
            if (mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16) {
                selectedModule = null;
                bindingKey = false;
                draggingSlider = null;
                return true;
            }

            String keyText = bindingKey ? "..." : (selectedModule.getKeyBind() > 0 ? "[" + GLFW.glfwGetKeyName(selectedModule.getKeyBind(), 0) + "]" : "[BIND]");
            int kw = textRenderer.getWidth(keyText);
            int kx = mx + mw - kw - 38;
            int ky = my + 8;
            if (mouseX >= kx - 3 && mouseX <= kx + kw + 3 && mouseY >= ky - 1 && mouseY <= ky + 15) {
                if (button == 0) {
                    bindingKey = !bindingKey;
                    return true;
                } else if (button == 1) {
                    selectedModule.setKeyBind(0);
                    bindingKey = false;
                    return true;
                }
            }
            bindingKey = false;

            int sListX = mx + 12;
            int sListY = my + 32 + 8;
            int sListW = mw - 24;
            int curY = sListY + modalScrollY;

            for (Setting<?> s : selectedModule.getSettings()) {
                if (s instanceof BooleanSetting) {
                    int rowH = 24;
                    if (mouseX >= sListX && mouseX <= sListX + sListW && mouseY >= curY && mouseY <= curY + rowH && button == 0) {
                        ((BooleanSetting) s).toggle();
                        return true;
                    }
                    curY += rowH + 6;
                } else if (s instanceof NumberSetting) {
                    NumberSetting ns = (NumberSetting) s;
                    int rowH = 34;
                    int sliderX = sListX + 10;
                    int sliderY = curY + 16;
                    int sliderW = sListW - 20;

                    if (mouseX >= sliderX && mouseX <= sliderX + sliderW && mouseY >= sliderY - 4 && mouseY <= sliderY + 12 && button == 0) {
                        draggingSlider = ns;
                        updateSlider(ns, mouseX, sliderX, sliderW);
                        return true;
                    }
                    curY += rowH + 6;
                } else if (s instanceof ModeSetting) {
                    ModeSetting ms = (ModeSetting) s;
                    int rowH = 26;
                    if (mouseX >= sListX && mouseX <= sListX + sListW && mouseY >= curY && mouseY <= curY + rowH) {
                        ms.cycle();
                        return true;
                    }
                    curY += rowH + 6;
                } else if (s instanceof ColorSetting) {
                    ColorSetting cs = (ColorSetting) s;
                    int rowH = 26;
                    if (mouseX >= sListX && mouseX <= sListX + sListW && mouseY >= curY && mouseY <= curY + rowH && button == 0) {
                        cs.cycle();
                        return true;
                    }
                    curY += rowH + 6;
                }
            }

            if (mouseX < mx || mouseX > mx + mw || mouseY < my || mouseY > my + mh) {
                selectedModule = null;
                bindingKey = false;
                draggingSlider = null;
                return true;
            }
            return true;
        }

        int navH = 36;
        DashTab[] tabs = DashTab.values();
        int tabStartX = panelX + 130;
        int tabW = 75;

        for (int i = 0; i < tabs.length; i++) {
            DashTab t = tabs[i];
            int tx = tabStartX + i * (tabW + 6);
            int ty = panelY + 7;
            if (mouseX >= tx && mouseX <= tx + tabW && mouseY >= ty && mouseY <= ty + 22) {
                currentTab = t;
                scrollY = 0;
                return true;
            }
        }

        int contentX = panelX + 16;
        int contentY = panelY + navH + 12;
        int contentW = panelW - 32;

        if (currentTab == DashTab.THEMES) {
            com.nexuspvp.gui.GuiStyle[] styles = com.nexuspvp.gui.GuiStyle.values();
            int btnW = (contentW - 12) / 2;
            int btnH = 40;

            for (int i = 0; i < styles.length; i++) {
                com.nexuspvp.gui.GuiStyle s = styles[i];
                int col = i % 2;
                int row = i / 2;
                int bx = contentX + col * (btnW + 12);
                int by = contentY + scrollY + 22 + row * (btnH + 8);

                if (mouseX >= bx && mouseX <= bx + btnW && mouseY >= by && mouseY <= by + btnH && button == 0) {
                    ThemeManager.getInstance().setStyle(s);
                    if (client != null) {
                        ClickGui.openCurrentStyleScreen();
                    }
                    return true;
                }
            }
        } else {
            List<Module> mods = categoryModules.get(currentTab.category);
            if (mods != null) {
                int colW = (contentW - 12) / 2;
                int cardH = 48;

                for (int i = 0; i < mods.size(); i++) {
                    Module m = mods.get(i);
                    int col = i % 2;
                    int row = i / 2;
                    int cardX = contentX + col * (colW + 12);
                    int cardY = contentY + scrollY + row * (cardH + 8);

                    if (mouseX >= cardX && mouseX <= cardX + colW && mouseY >= cardY && mouseY <= cardY + cardH) {
                        int gearX = cardX + colW - 56;
                        int gearY = cardY + 16;
                        if (button == 1 || (mouseX >= gearX && mouseX <= gearX + 16 && mouseY >= gearY && mouseY <= gearY + 16)) {
                            selectedModule = m;
                            modalScrollY = 0;
                            return true;
                        } else if (button == 0) {
                            m.toggle();
                            return true;
                        }
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider != null && selectedModule != null) {
            int panelW = 540;
            int panelX = (width - panelW) / 2;
            int mw = 360;
            int mx = panelX + (panelW - mw) / 2;
            int sliderX = mx + 22;
            int sliderW = mw - 44;
            updateSlider(draggingSlider, mouseX, sliderX, sliderW);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSlider(Setting<?> s, double mouseX, int sliderX, int sliderW) {
        if (!(s instanceof NumberSetting)) return;
        NumberSetting ns = (NumberSetting) s;
        double pct = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / (double) sliderW));
        double val = ns.getMin() + pct * (ns.getMax() - ns.getMin());
        ns.setValue(val);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (selectedModule != null) {
            modalScrollY += (int) (verticalAmount * 16);
            if (modalScrollY > 0) modalScrollY = 0;
            return true;
        }
        scrollY += (int) (verticalAmount * 20);
        if (scrollY > 0) scrollY = 0;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingKey && selectedModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                selectedModule.setKeyBind(0);
            } else {
                selectedModule.setKeyBind(keyCode);
            }
            bindingKey = false;
            return true;
        }

        if (selectedModule != null && (keyCode == GLFW.GLFW_KEY_ESCAPE)) {
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
        NexusPVP.getInstance().getModuleManager().getModuleByName("ClickGui").ifPresent(m -> {
            if (m.isEnabled()) {
                m.toggle();
            }
        });
    }
}
