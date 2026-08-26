package com.nexuspvp.gui;
import com.nexuspvp.util.Compat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.CommandKeybinds;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandKeybindsScreen extends Screen {

    private final Screen parent;
    private final CommandKeybinds module;
    private TextFieldWidget commandField;
    private int selectedKey = -1;
    private boolean listeningForKey = false;
    private int scrollY = 0;

    public CommandKeybindsScreen(Screen parent) {
        super(Text.literal("Command Keybinds Manager"));
        this.parent = parent;
        this.module = NexusPVP.getInstance().getModuleManager().getModule(CommandKeybinds.class);
    }

    @Override
    protected void init() {
        super.init();
        int w = 400;
        int x = (this.width - w) / 2;
        int y = (this.height - 240) / 2;

        commandField = new TextFieldWidget(this.client.textRenderer, x + 110, y + 200, 180, 18, Text.literal("/command"));
        commandField.setMaxLength(128);
        commandField.setDrawsBackground(true);
        commandField.setText("/feed");
    }

    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        // Dark backdrop
        RenderUtils.drawRect(matrices, 0, 0, (this.width)-(0), (this.height)-(0), 0xC0111214);

        int modalW = 420;
        int modalH = 250;
        int modalX = (this.width - modalW) / 2;
        int modalY = (this.height - modalH) / 2;

        // Container
        RenderUtils.drawRoundedRect(matrices, modalX - 1, modalY - 1, modalW + 2, modalH + 2, 7, 0xFF111214);
        RenderUtils.drawRoundedRect(matrices, modalX, modalY, modalW, modalH, 6, 0xFF313338);

        // Header
        RenderUtils.drawRoundedRect(matrices, modalX, modalY, modalW, 26, 6, 0xFF1E1F22);
        RenderUtils.drawRoundedRect(matrices, modalX, modalY, 4, 26, 2, 0xFF5865F2);
        String header = "⌨ " + (LanguageManager.getInstance().isRussian() ? "БИНДЫ КОМАНД СЕРВЕРА" : "COMMAND KEYBINDS MANAGER");
        Compat.drawText(matrices, header, modalX + 14, modalY + 8, 0xFFFFFFFF);

        // Back button
        boolean backHover = mouseX >= modalX + modalW - 65 && mouseX <= modalX + modalW - 10 && mouseY >= modalY + 4 && mouseY <= modalY + 22;
        RenderUtils.drawRoundedRect(matrices, modalX + modalW - 65, modalY + 4, 55, 18, 4, backHover ? 0xFF5865F2 : 0xFF2B2D31);
        String backText = LanguageManager.getInstance().get("Back");
        Compat.drawText(matrices, backText, modalX + modalW - 65 + (55 - this.textRenderer.getWidth(backText)) / 2, modalY + 8, 0xFFFFFFFF);

        // Binds List Area
        int listX = modalX + 12;
        int listY = modalY + 34;
        int listW = modalW - 24;
        int listH = 150;

        RenderUtils.drawRoundedRect(matrices, listX, listY, listW, listH, 4, 0xFF1E1F22);

        if (module != null) {
            Map<Integer, String> binds = module.getBinds();
            int rowY = listY + 6 + scrollY;

            if (binds.isEmpty()) {
                String empty = LanguageManager.getInstance().isRussian() ? "Список биндов пуст. Добавьте бинд ниже!" : "No keybinds configured. Add one below!";
                Compat.drawText(matrices, empty, listX + 12, listY + 20, 0xFF949BA4);
            } else {
                List<Map.Entry<Integer, String>> entries = new ArrayList<>(binds.entrySet());
                for (Map.Entry<Integer, String> entry : entries) {
                    if (rowY >= listY && rowY + 20 <= listY + listH) {
                        int key = entry.getKey();
                        String cmd = entry.getValue();
                        String keyName = getKeyName(key);

                        // Row background
                        RenderUtils.drawRoundedRect(matrices, listX + 6, rowY, listW - 12, 18, 3, 0xFF2B2D31);

                        // Key badge
                        RenderUtils.drawRoundedRect(matrices, listX + 10, rowY + 2, 45, 14, 2, 0xFF5865F2);
                        int kw = this.textRenderer.getWidth(keyName);
                        Compat.drawText(matrices, keyName, listX + 10 + (45 - kw) / 2, rowY + 5, 0xFFFFFFFF);

                        // Command text
                        Compat.drawText(matrices, "->  " + cmd, listX + 62, rowY + 5, 0xFFDBDEE1);

                        // Delete (X) button
                        int delX = listX + listW - 32;
                        int delY = rowY + 2;
                        boolean delHover = mouseX >= delX && mouseX <= delX + 20 && mouseY >= delY && mouseY <= delY + 14;
                        RenderUtils.drawRoundedRect(matrices, delX, delY, 20, 14, 2, delHover ? 0xFFDA373C : 0xFF35373C);
                        Compat.drawText(matrices, "X", delX + 6, delY + 3, 0xFFFFFFFF);
                    }
                    rowY += 22;
                }
            }
        }

        // Add New Bind Bottom Controls
        int addY = modalY + 194;
        RenderUtils.drawRoundedRect(matrices, listX, addY - 4, listW, 52, 4, 0xFF2B2D31);

        // Key Selection Button
        int keyBtnX = listX + 6;
        int keyBtnY = addY + 4;
        int keyBtnW = 90;
        int keyBtnH = 20;
        boolean keyHover = mouseX >= keyBtnX && mouseX <= keyBtnX + keyBtnW && mouseY >= keyBtnY && mouseY <= keyBtnY + keyBtnH;
        int keyBg = listeningForKey ? 0xFFE87D0D : (keyHover ? 0xFF4752C4 : 0xFF5865F2);
        RenderUtils.drawRoundedRect(matrices, keyBtnX, keyBtnY, keyBtnW, keyBtnH, 4, keyBg);
        String keyLabel = listeningForKey ? "..." : (selectedKey == -1 ? "Клавиша" : getKeyName(selectedKey));
        int klw = this.textRenderer.getWidth(keyLabel);
        Compat.drawText(matrices, keyLabel, keyBtnX + (keyBtnW - klw) / 2, keyBtnY + 6, 0xFFFFFFFF);

        // Command TextField
        if (commandField != null) {
            commandField.setX(listX + 102);
            commandField.setY(addY + 4);
            commandField.setWidth(180);
            commandField.render(matrices, mouseX, mouseY, delta);
        }

        // Add Button
        int addBtnX = listX + 288;
        int addBtnY = addY + 4;
        int addBtnW = listW - 294;
        int addBtnH = 20;
        boolean addHover = mouseX >= addBtnX && mouseX <= addBtnX + addBtnW && mouseY >= addBtnY && mouseY <= addBtnY + addBtnH;
        RenderUtils.drawRoundedRect(matrices, addBtnX, addBtnY, addBtnW, addBtnH, 4, addHover ? 0xFF23A55A : 0xFF2B2D31);
        String addText = "+ " + (LanguageManager.getInstance().isRussian() ? "Добавить" : "Add");
        int atw = this.textRenderer.getWidth(addText);
        Compat.drawText(matrices, addText, addBtnX + (addBtnW - atw) / 2, addBtnY + 6, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    private String getKeyName(int keyCode) {
        if (keyCode == -1) return "NONE";
        String name = InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
        if (name == null || name.isEmpty() || name.equals("key.keyboard.unknown")) {
            return "KEY " + keyCode;
        }
        return name.toUpperCase();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int modalW = 420;
        int modalH = 250;
        int modalX = (this.width - modalW) / 2;
        int modalY = (this.height - modalH) / 2;

        // Back button
        if (mouseX >= modalX + modalW - 65 && mouseX <= modalX + modalW - 10 && mouseY >= modalY + 4 && mouseY <= modalY + 22 && button == 0) {
            Compat.setScreen(client, parent);
            return true;
        }

        int listX = modalX + 12;
        int listY = modalY + 34;
        int listW = modalW - 24;
        int listH = 150;

        // Check Delete Click
        if (module != null) {
            Map<Integer, String> binds = module.getBinds();
            int rowY = listY + 6 + scrollY;
            List<Map.Entry<Integer, String>> entries = new ArrayList<>(binds.entrySet());
            for (Map.Entry<Integer, String> entry : entries) {
                if (rowY >= listY && rowY + 20 <= listY + listH) {
                    int delX = listX + listW - 32;
                    int delY = rowY + 2;
                    if (mouseX >= delX && mouseX <= delX + 20 && mouseY >= delY && mouseY <= delY + 14 && button == 0) {
                        module.removeBind(entry.getKey());
                        return true;
                    }
                }
                rowY += 22;
            }
        }

        // Key Selection button click
        int addY = modalY + 194;
        int keyBtnX = listX + 6;
        int keyBtnY = addY + 4;
        int keyBtnW = 90;
        int keyBtnH = 20;
        if (mouseX >= keyBtnX && mouseX <= keyBtnX + keyBtnW && mouseY >= keyBtnY && mouseY <= keyBtnY + keyBtnH && button == 0) {
            listeningForKey = true;
            return true;
        }

        // Add Button click
        int addBtnX = listX + 288;
        int addBtnY = addY + 4;
        int addBtnW = listW - 294;
        int addBtnH = 20;
        if (mouseX >= addBtnX && mouseX <= addBtnX + addBtnW && mouseY >= addBtnY && mouseY <= addBtnY + addBtnH && button == 0) {
            if (selectedKey != -1 && commandField != null && !commandField.getText().trim().isEmpty() && module != null) {
                module.setBind(selectedKey, commandField.getText().trim());
                selectedKey = -1;
                listeningForKey = false;
            }
            return true;
        }

        if (commandField != null && commandField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKey) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                selectedKey = keyCode;
            }
            listeningForKey = false;
            return true;
        }

        if (commandField != null && commandField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (selectedKey != -1 && !commandField.getText().trim().isEmpty() && module != null) {
                    module.setBind(selectedKey, commandField.getText().trim());
                    selectedKey = -1;
                }
                return true;
            }
            return commandField.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Compat.setScreen(client, parent);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (commandField != null && commandField.isFocused()) {
            return commandField.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }
}