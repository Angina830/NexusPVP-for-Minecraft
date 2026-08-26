package com.nexuspvp.gui;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.ViewModel;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ViewModelEditorScreen extends Screen {

    private final Screen parent;
    private final ViewModel viewModel;
    private double lastMouseX;
    private double lastMouseY;
    private boolean showHelp = false;

    public ViewModelEditorScreen(Screen parent) {
        super(Text.literal("ViewModel Blender Editor"));
        this.parent = parent;
        this.viewModel = NexusPVP.getInstance().getModuleManager().getModule(ViewModel.class);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices(); {
        int w = this.width;
        int h = this.height;

        // Auto-enable ViewModel module when editor is open so user sees effect immediately
        if (viewModel != null && !viewModel.isEnabled()) {
            viewModel.setEnabled(true);
        }

        // Top Header Banner (Blender Style)
        int headerH = 26;
        int headerW = 260;
        RenderUtils.drawRoundedRect(matrices, w / 2 - headerW / 2, 8, headerW, headerH, 6, 0xDD1E1F22);
        RenderUtils.drawRoundedRect(matrices, w / 2 - headerW / 2, 8, 4, headerH, 2, 0xFFE87D0D); // Blender Orange Accent

        String title = "🖐 " + LanguageManager.getInstance().get("BLENDER VIEWMODEL EDITOR");
        int tw = this.textRenderer.getWidth(title);
        this.context.drawTextWithShadow(textRenderer, title, w / 2 - tw / 2, 16, 0xFFFFB020);

        // Top-Left Action Buttons Row
        // 1. Back Button
        boolean backHover = mouseX >= 10 && mouseX <= 80 && mouseY >= 10 && mouseY <= 30;
        int backBg = backHover ? 0xFF5865F2 : 0xDD2B2D31;
        RenderUtils.drawRoundedRect(matrices, 10, 10, 70, 20, 4, backBg);
        String backText = "< " + LanguageManager.getInstance().get("Back");
        this.context.drawTextWithShadow(textRenderer, backText, 16, 16, 0xFFFFFFFF);

        // 2. Reset Button
        boolean resetHover = mouseX >= 86 && mouseX <= 156 && mouseY >= 10 && mouseY <= 30;
        int resetBg = resetHover ? 0xFFDA373C : 0xDD2B2D31;
        RenderUtils.drawRoundedRect(matrices, 86, 10, 70, 20, 4, resetBg);
        String resetText = "↺ " + LanguageManager.getInstance().get("Reset");
        this.context.drawTextWithShadow(textRenderer, resetText, 92, 16, 0xFFFFFFFF);

        // 3. Help / Tutorial Button
        boolean helpHover = mouseX >= 162 && mouseX <= 262 && mouseY >= 10 && mouseY <= 30;
        int helpBg = showHelp ? 0xFF5865F2 : (helpHover ? 0xFF4752C4 : 0xDD2B2D31);
        RenderUtils.drawRoundedRect(matrices, 162, 10, 100, 20, 4, helpBg);
        String helpBtnText = "? " + (LanguageManager.getInstance().isRussian() ? "Инструкция" : "Help Guide");
        this.context.drawTextWithShadow(textRenderer, helpBtnText, 168, 16, 0xFFFFFFFF);

        // Top-Right Live Transform Inspector Card
        if (viewModel != null) {
            int cardW = 160;
            int cardH = 88;
            int cardX = w - cardW - 10;
            int cardY = 10;

            RenderUtils.drawRoundedRect(matrices, cardX, cardY, cardW, cardH, 6, 0xEE1E1F22);
            RenderUtils.drawRoundedRect(matrices, cardX, cardY, cardW, 16, 4, 0xFF2B2D31);
            this.context.drawTextWithShadow(textRenderer, "⚙ " + LanguageManager.getInstance().get("TRANSFORM"), cardX + 8, cardY + 4, 0xFFE87D0D);

            // Position
            String posX = String.format("%.2f", viewModel.getTranslateX());
            String posY = String.format("%.2f", viewModel.getTranslateY());
            String posZ = String.format("%.2f", viewModel.getTranslateZ());
            this.context.drawTextWithShadow(textRenderer, "Pos:", cardX + 8, cardY + 22, 0xFF949BA4);
            this.context.drawTextWithShadow(textRenderer, "X " + posX, cardX + 36, cardY + 22, 0xFFFF5555);
            this.context.drawTextWithShadow(textRenderer, "Y " + posY, cardX + 80, cardY + 22, 0xFF55FF55);
            this.context.drawTextWithShadow(textRenderer, "Z " + posZ, cardX + 120, cardY + 22, 0xFF5599FF);

            // Rotation
            String rotX = String.format("%.0f°", viewModel.getRotateX());
            String rotY = String.format("%.0f°", viewModel.getRotateY());
            String rotZ = String.format("%.0f°", viewModel.getRotateZ());
            this.context.drawTextWithShadow(textRenderer, "Rot:", cardX + 8, cardY + 42, 0xFF949BA4);
            this.context.drawTextWithShadow(textRenderer, rotX, cardX + 36, cardY + 42, 0xFFFF8888);
            this.context.drawTextWithShadow(textRenderer, rotY, cardX + 80, cardY + 42, 0xFF88FF88);
            this.context.drawTextWithShadow(textRenderer, rotZ, cardX + 120, cardY + 42, 0xFF88BBFF);

            // Scale
            String scX = String.format("%.2fx", viewModel.getScaleX());
            this.context.drawTextWithShadow(textRenderer, "Scale:", cardX + 8, cardY + 62, 0xFF949BA4);
            this.context.drawTextWithShadow(textRenderer, scX, cardX + 46, cardY + 62, 0xFFFFD700);
        }

        // Center 3D Axis Gizmo Reticle
        int cx = w / 2;
        int cy = h / 2;
        // Red X axis arrow
        RenderUtils.drawRect(matrices, cx, cy - 1, 24, 2, 0xFFFF4444);
        RenderUtils.drawRect(matrices, cx + 22, cy - 3, 2, 6, 0xFFFF4444);
        this.context.drawTextWithShadow(textRenderer, "X", cx + 28, cy - 4, 0xFFFF4444);

        // Green Y axis arrow
        RenderUtils.drawRect(matrices, cx - 1, cy - 24, 2, 24, 0xFF44FF44);
        RenderUtils.drawRect(matrices, cx - 3, cy - 24, 6, 2, 0xFF44FF44);
        this.context.drawTextWithShadow(textRenderer, "Y", cx - 3, cy - 34, 0xFF44FF44);

        // Blue Z axis dot
        RenderUtils.drawRoundedRect(matrices, cx - 3, cy - 3, 6, 6, 3, 0xFF4488FF);

        // Real-time Active Mode Pill Badge (indicates currently held modifier)
        boolean shift = Screen.hasShiftDown();
        boolean ctrl = Screen.hasControlDown() || Screen.hasAltDown();
        String activeAction = "ЛКМ: Двигать X/Y  |  ПКМ: Вращать  |  Колёсико: Масштаб";
        int modeColor = 0xFFDBDEE1;
        if (shift) {
            activeAction = "⚡ РЕЖИМ ГЛУБИНЫ: Зажми ЛКМ и двигай мышь вверх/вниз (Ось Z)";
            modeColor = 0xFF5599FF;
        } else if (ctrl) {
            activeAction = "🔄 РЕЖИМ КРЕНА: Зажми ПКМ и двигай мышь для закручивания (Roll Z)";
            modeColor = 0xFF88BBFF;
        }

        int actW = this.textRenderer.getWidth(activeAction) + 16;
        int actY = h - 56;
        RenderUtils.drawRoundedRect(matrices, w / 2 - actW / 2, actY, actW, 16, 4, 0xEE1E1F22);
        this.context.drawTextWithShadow(textRenderer, activeAction, w / 2 - this.textRenderer.getWidth(activeAction) / 2, actY + 4, modeColor);

        // Bottom Legend Control Bar
        int legendH = 22;
        int legendW = Math.min(w - 20, 600);
        int legendX = w / 2 - legendW / 2;
        int legendY = h - legendH - 8;

        RenderUtils.drawRoundedRect(matrices, legendX, legendY, legendW, legendH, 5, 0xEE1E1F22);
        String legend = LanguageManager.getInstance().isRussian() ?
            "[ ЛКМ ]: Позиция X/Y  |  [ Shift+ЛКМ ]: Глубина Z  |  [ ПКМ ]: Вращение  |  [ Колёсико ]: Масштаб  |  [ R ]: Сброс  |  [ ESC ]: Сохранить" :
            "[ LMB ]: Drag X/Y  |  [ Shift+LMB ]: Depth Z  |  [ RMB ]: Rotate  |  [ Scroll ]: Scale  |  [ R ]: Reset  |  [ ESC ]: Save";
        int lw = this.textRenderer.getWidth(legend);
        this.context.drawTextWithShadow(textRenderer, legend, w / 2 - lw / 2, legendY + 7, 0xFFDBDEE1);

        // =========================================================================
        // INTERACTIVE HELP / TUTORIAL MODAL WINDOW
        // =========================================================================
        if (showHelp) {
            // Darken backdrop slightly
            RenderUtils.drawRect(matrices, 0, 0, w, h, 0x99000000);

            int modalW = 380;
            int modalH = 220;
            int modalX = w / 2 - modalW / 2;
            int modalY = h / 2 - modalH / 2;

            // Main Modal Card
            RenderUtils.drawRoundedRect(matrices, modalX, modalY, modalW, modalH, 8, 0xFF1E1F22);
            RenderUtils.drawRoundedRect(matrices, modalX, modalY, modalW, 26, 6, 0xFF2B2D31);
            RenderUtils.drawRoundedRect(matrices, modalX, modalY, 4, 26, 2, 0xFF5865F2);

            String modalHeader = "📖 " + (LanguageManager.getInstance().isRussian() ? "КАК УПРАВЛЯТЬ РУКОЙ В РЕДАКТОРЕ" : "HOW TO USE THE 3D GIZMO EDITOR");
            this.context.drawTextWithShadow(textRenderer, modalHeader, modalX + 12, modalY + 8, 0xFFFFFFFF);

            // Close (X) button on modal
            boolean closeHover = mouseX >= modalX + modalW - 22 && mouseX <= modalX + modalW - 6 && mouseY >= modalY + 6 && mouseY <= modalY + 20;
            this.context.drawTextWithShadow(textRenderer, "X", modalX + modalW - 16, modalY + 8, closeHover ? 0xFFDA373C : 0xFF949BA4);

            int rowY = modalY + 34;
            int lineHeight = 21;

            drawHelpRow(matrices, "🖱 [ Зажать ЛКМ ]", LanguageManager.getInstance().isRussian() ? "Двигать руку влево / вправо / вверх / вниз (X / Y)" : "Move hand Left / Right / Up / Down (X / Y)", modalX + 12, rowY, 0xFF55FF55);
            rowY += lineHeight;

            drawHelpRow(matrices, "⌨ [ Shift + ЛКМ ]", LanguageManager.getInstance().isRussian() ? "Приблизить / отдалить руку вглубь экрана (Ось Z)" : "Push / Pull hand forward & backward in depth (Z)", modalX + 12, rowY, 0xFF5599FF);
            rowY += lineHeight;

            drawHelpRow(matrices, "🖱 [ Зажать ПКМ ]", LanguageManager.getInstance().isRussian() ? "Вращать и наклонять руку (Углы наклона и поворота)" : "Rotate Pitch & Yaw angles freely", modalX + 12, rowY, 0xFFFF8888);
            rowY += lineHeight;

            drawHelpRow(matrices, "⌨ [ Ctrl / Alt + ПКМ ]", LanguageManager.getInstance().isRussian() ? "Закрутить руку по часовой / против (Крен Roll)" : "Roll rotation (Clockwise / Counter-clockwise)", modalX + 12, rowY, 0xFF88BBFF);
            rowY += lineHeight;

            drawHelpRow(matrices, "🎡 [ Колёсико мыши ]", LanguageManager.getInstance().isRussian() ? "Мгновенно увеличить / уменьшить размер руки (Scale)" : "Instantly scale hand size up or down", modalX + 12, rowY, 0xFFFFD700);
            rowY += lineHeight;

            drawHelpRow(matrices, "🔄 [ Клавиша R ]", LanguageManager.getInstance().isRussian() ? "Сбросить положение руки к стандартному виду" : "Reset hand position to default vanilla", modalX + 12, rowY, 0xFFDA373C);
            rowY += lineHeight;

            // Bottom Modal OK Button
            int okW = 140;
            int okH = 20;
            int okX = modalX + modalW / 2 - okW / 2;
            int okY = modalY + modalH - okH - 8;
            boolean okHover = mouseX >= okX && mouseX <= okX + okW && mouseY >= okY && mouseY <= okY + okH;
            RenderUtils.drawRoundedRect(matrices, okX, okY, okW, okH, 4, okHover ? 0xFF4752C4 : 0xFF5865F2);
            String okText = LanguageManager.getInstance().isRussian() ? "Понятно, настроить!" : "Got it, start editing!";
            int otw = this.textRenderer.getWidth(okText);
            this.context.drawTextWithShadow(textRenderer, okText, okX + (okW - otw) / 2, okY + 6, 0xFFFFFFFF);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void drawHelpRow(MatrixStack matrices, String key, String desc, int x, int y, int keyColor) {
        this.context.drawTextWithShadow(textRenderer, key, x, y, keyColor);
        int kw = this.textRenderer.getWidth(key);
        this.context.drawTextWithShadow(textRenderer, " - " + desc, x + kw, y, 0xFFDBDEE1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        int w = this.width;
        int h = this.height;

        if (showHelp) {
            int modalW = 380;
            int modalH = 220;
            int modalX = w / 2 - modalW / 2;
            int modalY = h / 2 - modalH / 2;

            // Close (X) click
            if (mouseX >= modalX + modalW - 22 && mouseX <= modalX + modalW - 6 && mouseY >= modalY + 6 && mouseY <= modalY + 20 && button == 0) {
                showHelp = false;
                return true;
            }

            // OK Button click
            int okW = 140;
            int okH = 20;
            int okX = modalX + modalW / 2 - okW / 2;
            int okY = modalY + modalH - okH - 8;
            if (mouseX >= okX && mouseX <= okX + okW && mouseY >= okY && mouseY <= okY + okH && button == 0) {
                showHelp = false;
                return true;
            }
            return true;
        }

        // Back button
        if (mouseX >= 10 && mouseX <= 80 && mouseY >= 10 && mouseY <= 30 && button == 0) {
            this.client.openScreen(parent);
            return true;
        }

        // Reset button
        if (mouseX >= 86 && mouseX <= 156 && mouseY >= 10 && mouseY <= 30 && button == 0) {
            if (viewModel != null) {
                viewModel.resetAll();
            }
            return true;
        }

        // Help / Tutorial Button
        if (mouseX >= 162 && mouseX <= 262 && mouseY >= 10 && mouseY <= 30 && button == 0) {
            showHelp = !showHelp;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (showHelp || viewModel == null) return false;

        boolean shift = Screen.hasShiftDown();
        boolean ctrl = Screen.hasControlDown() || Screen.hasAltDown();

        if (button == 0) {
            // LMB: Translate
            if (shift) {
                // Depth (Z)
                viewModel.setTranslateZ(viewModel.getTranslateZ() + deltaY * 0.005);
            } else {
                // X & Y
                viewModel.setTranslateX(viewModel.getTranslateX() + deltaX * 0.005);
                viewModel.setTranslateY(viewModel.getTranslateY() - deltaY * 0.005);
            }
            return true;
        } else if (button == 1) {
            // RMB: Rotate
            if (ctrl) {
                // Roll (Z)
                viewModel.setRotateZ(viewModel.getRotateZ() + deltaX * 0.5);
            } else {
                // Pitch (X) & Yaw (Y)
                viewModel.setRotateX(viewModel.getRotateX() - deltaY * 0.5);
                viewModel.setRotateY(viewModel.getRotateY() + deltaX * 0.5);
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (showHelp) return false;
        if (viewModel != null) {
            float step = (float) (amount * 0.05);
            viewModel.setScaleX(viewModel.getScaleX() + step);
            viewModel.setScaleY(viewModel.getScaleY() + step);
            viewModel.setScaleZ(viewModel.getScaleZ() + step);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showHelp) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
                showHelp = false;
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_H || keyCode == GLFW.GLFW_KEY_F1) {
            showHelp = !showHelp;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_R) {
            if (viewModel != null) {
                viewModel.resetAll();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.openScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}