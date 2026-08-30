package com.nexuspvp.gui;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.*;
import com.nexuspvp.modules.CrosshairHealth;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class HudEditorScreen extends Screen {

    private final Screen parent;
    private final List<HudBox> hudBoxes = new ArrayList<>();
    private HudBox draggingBox = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HudEditorScreen(Screen parent) {
        super(new LiteralText("HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        hudBoxes.clear();

        int screenW = this.width;
        int screenH = this.height;

        // 1. TargetHUD
        TargetHUD targetHUD = NexusPVP.getInstance().getModuleManager().getModule(TargetHUD.class);
        if (targetHUD != null) {
            hudBoxes.add(new HudBox("TargetHUD", 140, 44, true, () -> {
                int x = screenW / 2 + targetHUD.getPosX().getIntValue() - 70;
                int y = screenH / 2 + targetHUD.getPosY().getIntValue();
                return new int[]{x, y};
            }, (newX, newY) -> {
                targetHUD.getPosX().setValue((double) (newX + 70 - screenW / 2));
                targetHUD.getPosY().setValue((double) (newY - screenH / 2));
            }));
        }

        // 2. ArmorHUD
        ArmorHUD armorHUD = NexusPVP.getInstance().getModuleManager().getModule(ArmorHUD.class);
        if (armorHUD != null) {
            hudBoxes.add(new HudBox("ArmorHUD", 110, 24, true, () -> {
                int x = screenW / 2 + armorHUD.getPosX().getIntValue() - 55;
                int y = screenH / 2 + armorHUD.getPosY().getIntValue();
                return new int[]{x, y};
            }, (newX, newY) -> {
                armorHUD.getPosX().setValue((double) (newX + 55 - screenW / 2));
                armorHUD.getPosY().setValue((double) (newY - screenH / 2));
            }));
        }

        // 3. PotionHUD
        PotionHUD potionHUD = NexusPVP.getInstance().getModuleManager().getModule(PotionHUD.class);
        if (potionHUD != null) {
            hudBoxes.add(new HudBox("PotionHUD", 110, 50, false, () -> {
                return new int[]{potionHUD.getPosX().getIntValue(), potionHUD.getPosY().getIntValue()};
            }, (newX, newY) -> {
                potionHUD.getPosX().setValue((double) newX);
                potionHUD.getPosY().setValue((double) newY);
            }));
        }

        // 4. DamageIndicator (Combo Badge)
        DamageIndicator dmg = NexusPVP.getInstance().getModuleManager().getModule(DamageIndicator.class);
        if (dmg != null) {
            hudBoxes.add(new HudBox("Combo Badge", 130, 20, true, () -> {
                int x = screenW / 2 + dmg.getComboOffsetX().getIntValue() - 65;
                int y = screenH / 2 + dmg.getComboOffsetY().getIntValue();
                return new int[]{x, y};
            }, (newX, newY) -> {
                dmg.getComboOffsetX().setValue((double) (newX + 65 - screenW / 2));
                dmg.getComboOffsetY().setValue((double) (newY - screenH / 2));
            }));
        }

        // 5. Keystrokes
        Keystrokes keystrokes = NexusPVP.getInstance().getModuleManager().getModule(Keystrokes.class);
        if (keystrokes != null) {
            hudBoxes.add(new HudBox("Keystrokes", 72, 70, false, () -> {
                return new int[]{keystrokes.getPosX().getIntValue(), keystrokes.getPosY().getIntValue()};
            }, (newX, newY) -> {
                keystrokes.getPosX().setValue((double) newX);
                keystrokes.getPosY().setValue((double) newY);
            }));
        }

        // 6. TotemPop
        TotemPop totemPop = NexusPVP.getInstance().getModuleManager().getModule(TotemPop.class);
        if (totemPop != null) {
            hudBoxes.add(new HudBox("Totem Alerts", 130, 22, false, () -> {
                return new int[]{totemPop.getPosX().getIntValue(), totemPop.getPosY().getIntValue()};
            }, (newX, newY) -> {
                totemPop.getPosX().setValue((double) newX);
                totemPop.getPosY().setValue((double) newY);
            }));
        }

        // 7. CrosshairHealth (Under-Crosshair Mini Bar)
        CrosshairHealth chHealth = NexusPVP.getInstance().getModuleManager().getModule(CrosshairHealth.class);
        if (chHealth != null) {
            hudBoxes.add(new HudBox("Crosshair HP", 80, 16, true, () -> {
                int x = screenW / 2 + chHealth.getPosX().getIntValue() - 40;
                int y = screenH / 2 + chHealth.getPosY().getIntValue();
                return new int[]{x, y};
            }, (newX, newY) -> {
                chHealth.getPosX().setValue((double) (newX + 40 - screenW / 2));
                chHealth.getPosY().setValue((double) (newY - screenH / 2));
            }));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Dark grid background
        fill(matrices, 0, 0, this.width, this.height, 0x88111214);

        int accent = ThemeManager.getInstance().getAccentColor().getRGB();

        // Alignment guides (center vertical and horizontal)
        fill(matrices, this.width / 2, 0, this.width / 2 + 1, this.height, 0x33FFFFFF);
        fill(matrices, 0, this.height / 2, this.width, this.height / 2 + 1, 0x33FFFFFF);

        // Top Header Banner
        RenderUtils.drawRoundedRect(matrices, this.width / 2 - 160, 8, 320, 32, 6, 0xEE1E1F22);
        RenderUtils.drawRoundedRect(matrices, this.width / 2 - 160, 8, 320, 2, 1, accent);

        String title = LanguageManager.getInstance().isRussian() ? "\u22BE \u0420\u0415\u0414\u0410\u041A\u0422\u041E\u0420 \u0420\u0410\u0421\u041F\u041E\u041B\u041E\u0416\u0415\u041D\u0418\u042F HUD" : "\u22BE HUD LAYOUT EDITOR";
        int tw = this.client.textRenderer.getWidth(title);
        this.client.textRenderer.drawWithShadow(matrices, title, this.width / 2 - tw / 2, 13, 0xFFF2F3F5);

        String sub = LanguageManager.getInstance().isRussian() ? "\u041F\u0435\u0440\u0435\u0442\u0430\u0441\u043A\u0438\u0432\u0430\u0439\u0442\u0435 \u0432\u0438\u0434\u0436\u0435\u0442\u044B \u043C\u044B\u0448\u043A\u043E\u0439 \u2022 ESC \u0434\u043B\u044F \u0441\u043E\u0445\u0440\u0430\u043D\u0435\u043D\u0438\u044F" : "Drag widgets with mouse \u2022 Press ESC to save & exit";
        int stw = this.client.textRenderer.getWidth(sub);
        this.client.textRenderer.drawWithShadow(matrices, sub, this.width / 2 - stw / 2, 25, 0xFF949BA4);

        // Save & Exit button
        int saveBtnW = 100;
        int saveBtnH = 20;
        int saveBtnX = this.width / 2 - saveBtnW / 2;
        int saveBtnY = this.height - 30;
        boolean saveHover = mouseX >= saveBtnX && mouseX <= saveBtnX + saveBtnW && mouseY >= saveBtnY && mouseY <= saveBtnY + saveBtnH;
        RenderUtils.drawRoundedRect(matrices, saveBtnX, saveBtnY, saveBtnW, saveBtnH, 4, saveHover ? 0xFF4752C4 : accent);
        String saveText = LanguageManager.getInstance().isRussian() ? "\u2714 \u0421\u043E\u0445\u0440\u0430\u043D\u0438\u0442\u044C" : "\u2714 Save & Exit";
        int stwt = this.client.textRenderer.getWidth(saveText);
        this.client.textRenderer.drawWithShadow(matrices, saveText, saveBtnX + (saveBtnW - stwt) / 2, saveBtnY + 6, 0xFFFFFFFF);

        // Render each Draggable HUD Box
        for (HudBox box : hudBoxes) {
            int[] pos = box.posSupplier.getPos();
            int bx = pos[0];
            int by = pos[1];
            int bw = box.width;
            int bh = box.height;

            boolean hovered = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
            boolean isThisDragging = (draggingBox == box);

            int borderCol = (isThisDragging || hovered) ? accent : 0x885865F2;
            int bgCol = isThisDragging ? 0xDD2B2D31 : (hovered ? 0xBB2B2D31 : 0x881E1F22);

            RenderUtils.drawRoundedRect(matrices, bx - 1, by - 1, bw + 2, bh + 2, 4, borderCol);
            RenderUtils.drawRoundedRect(matrices, bx, by, bw, bh, 3, bgCol);

            String label = box.name;
            int lw = this.client.textRenderer.getWidth(label);
            this.client.textRenderer.drawWithShadow(matrices, label, bx + (bw - lw) / 2, by + (bh - 8) / 2, 0xFFFFFFFF);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int saveBtnW = 100;
        int saveBtnH = 20;
        int saveBtnX = this.width / 2 - saveBtnW / 2;
        int saveBtnY = this.height - 30;
        if (mouseX >= saveBtnX && mouseX <= saveBtnX + saveBtnW && mouseY >= saveBtnY && mouseY <= saveBtnY + saveBtnH && button == 0) {
            this.onClose();
            return true;
        }

        if (button == 0) {
            for (HudBox box : hudBoxes) {
                int[] pos = box.posSupplier.getPos();
                int bx = pos[0];
                int by = pos[1];
                if (mouseX >= bx && mouseX <= bx + box.width && mouseY >= by && mouseY <= by + box.height) {
                    draggingBox = box;
                    dragOffsetX = (int) mouseX - bx;
                    dragOffsetY = (int) mouseY - by;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingBox = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingBox != null && button == 0) {
            int newX = (int) mouseX - dragOffsetX;
            int newY = (int) mouseY - dragOffsetY;

            newX = Math.max(0, Math.min(this.width - draggingBox.width, newX));
            newY = Math.max(0, Math.min(this.height - draggingBox.height, newY));

            draggingBox.posConsumer.setPos(newX, newY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (NexusPVP.getInstance().getConfigManager() != null) {
            NexusPVP.getInstance().getConfigManager().saveConfig();
        }
        if (this.client != null) {
            this.client.openScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @FunctionalInterface
    public interface PosSupplier {
        int[] getPos();
    }

    @FunctionalInterface
    public interface PosConsumer {
        void setPos(int x, int y);
    }

    private static class HudBox {
        final String name;
        final int width;
        final int height;
        final boolean relativeCenter;
        final PosSupplier posSupplier;
        final PosConsumer posConsumer;

        HudBox(String name, int width, int height, boolean relativeCenter, PosSupplier posSupplier, PosConsumer posConsumer) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.relativeCenter = relativeCenter;
            this.posSupplier = posSupplier;
            this.posConsumer = posConsumer;
        }
    }
}