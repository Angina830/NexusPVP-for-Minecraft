package com.nexuspvp.gui;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.modules.Radio;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.List;

public class PlayerPanel {
    private int x, y;
    private int width = 180;
    private int height = 250; // Taller for playlist
    private boolean dragging = false;
    private int dragOffsetX = 0, dragOffsetY = 0;
    
    private TextFieldWidget addTrackField;
    private int scrollY = 0;
    
    public PlayerPanel(int x, int y) {
        this.x = x;
        this.y = y;
        // Do not initialize TextFieldWidget here, textRenderer might be null during mod init!
    }
    
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        if (addTrackField == null) {
            String translatedAdd = LanguageManager.getInstance().get("Add Track...");
            addTrackField = new TextFieldWidget(mc.textRenderer, x + 5, y + 65, 170, 15, Text.literal(translatedAdd));
            addTrackField.setMaxLength(256);
            addTrackField.setDrawsBackground(true);
            addTrackField.setSuggestion(translatedAdd);
        } else {
            // Simply update it to the current translation if it's not currently displaying "Added!"
            // Since we can't get the suggestion, we'll just force update it.
            // When user adds a track, it will temporarily say "Added!", and then next tick might get overwritten, 
            // but that's okay for now, or we can just always set it to "Add Track..." if it's not focused.
            if (!addTrackField.isFocused()) {
                addTrackField.setSuggestion(LanguageManager.getInstance().get("Add Track..."));
            }
        }
        
        if (dragging) {
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
            addTrackField.x = x + 5;
            addTrackField.y = y + 65;
        }
        
        Radio radio = NexusPVP.getInstance().getModuleManager().getModule(Radio.class);
        boolean enabled = radio != null && radio.isEnabled();
        
        // Discord Outer border
        RenderUtils.drawRoundedRect(matrices, x - 1, y - 1, width + 2, height + 2, 8, 0xFF18191C);
        // Discord Card Background
        RenderUtils.drawRoundedRect(matrices, x, y, width, height, 7, 0xFF2B2D31);
        
        // Discord Top Header ("LISTENING TO SPOTIFY")
        RenderUtils.drawRoundedRect(matrices, x, y, width, 22, 7, 0xFF1E1F22);
        RenderUtils.drawRect(matrices, x, y + 14, width, 8, 0xFF1E1F22);
        
        String translatedHeader = LanguageManager.getInstance().get("Spotify (Radio)").toUpperCase();
        mc.textRenderer.drawWithShadow(matrices, translatedHeader, x + 10, y + 7, 0xFF1DB954); // Spotify Green
        
        // Green Spotify status dot
        RenderUtils.drawRoundedRect(matrices, x + width - 18, y + 7, 8, 8, 4, enabled ? 0xFF23A55A : 0xFF80848E);
        
        // Track Name
        String track = Radio.currentTrackName;
        if (track == null || track.isEmpty()) {
            if (Radio.downloadProgress != null && !Radio.downloadProgress.isEmpty()) {
                track = LanguageManager.getInstance().get("Downloading...");
            } else {
                track = enabled ? LanguageManager.getInstance().get("Loading...") : LanguageManager.getInstance().get("Turn on Radio");
            }
        }
        
        if (mc.textRenderer.getWidth(track) > width - 20) {
            track = track.substring(0, Math.min(track.length(), 24)) + "...";
        }
        mc.textRenderer.drawWithShadow(matrices, track, x + 10, y + 27, 0xFFF2F3F5);
        
        // Download / subtitle status
        if (Radio.downloadProgress != null && !Radio.downloadProgress.isEmpty()) {
            String prog = Radio.downloadProgress;
            if (mc.textRenderer.getWidth(prog) > width - 20) {
                prog = prog.substring(0, Math.min(prog.length(), 28)) + "...";
            }
            mc.textRenderer.drawWithShadow(matrices, prog, x + 10, y + 37, 0xFF949BA4);
        } else {
            String statusText = enabled ? "Playing live" : "Idle";
            mc.textRenderer.drawWithShadow(matrices, statusText, x + 10, y + 37, 0xFF949BA4);
        }
        
        // Buttons
        int btnY = y + 48;
        // Prev btn
        RenderUtils.drawRoundedRect(matrices, x + 10, btnY, 45, 15, 4, 0xFF1E1F22);
        mc.textRenderer.drawWithShadow(matrices, "⏮ Prev", x + 17, btnY + 4, 0xFFDBDEE1);
        
        // Toggle btn
        int toggleColor = enabled ? 0xFF23A55A : 0xFF5865F2; // Discord Green / Blurple
        RenderUtils.drawRoundedRect(matrices, x + 60, btnY, 60, 15, 4, toggleColor);
        String toggleText = enabled ? "⏸ Pause" : "▶ Play";
        mc.textRenderer.drawWithShadow(matrices, toggleText, x + 60 + (60 - mc.textRenderer.getWidth(toggleText)) / 2.0f, btnY + 4, 0xFFFFFFFF);
        
        // Next btn
        RenderUtils.drawRoundedRect(matrices, x + 125, btnY, 45, 15, 4, 0xFF1E1F22);
        mc.textRenderer.drawWithShadow(matrices, "Next ⏭", x + 130, btnY + 4, 0xFFDBDEE1);
        
        // Render text field
        if (addTrackField != null) {
            addTrackField.render(matrices, mouseX, mouseY, delta);
        }
        
        // Render Playlist
        if (radio != null) {
            int listY = y + 85;
            int listHeight = height - 85 - 6;
            
            // Draw list container background (Discord dark chat box #1E1F22)
            RenderUtils.drawRoundedRect(matrices, x + 5, listY, width - 10, listHeight, 4, 0xFF1E1F22);
            
            List<String> playlist = radio.getPlaylist();
            if (playlist != null && !playlist.isEmpty()) {
                int itemY = listY + 2 - scrollY;
                
                for (int i = 0; i < playlist.size(); i++) {
                    String name = playlist.get(i);
                    if (itemY >= listY - 5 && itemY + 14 <= listY + listHeight + 5) {
                        boolean isCurrent = (i == radio.getCurrentTrackIndex());
                        int color = isCurrent ? 0xFF1DB954 : 0xFFDBDEE1;
                        
                        // Hover effect
                        if (mouseX >= x + 6 && mouseX <= x + width - 6 && mouseY >= itemY && mouseY <= itemY + 14) {
                            if (mouseY >= listY && mouseY <= listY + listHeight) {
                                RenderUtils.drawRoundedRect(matrices, x + 6, itemY, width - 12, 14, 3, 0xFF35373C);
                            }
                        }
                        
                        String display = (i + 1) + ". " + name;
                        if (mc.textRenderer.getWidth(display) > width - 20) {
                            display = display.substring(0, Math.min(display.length(), 24)) + "...";
                        }
                        
                        if (itemY >= listY && itemY + 10 <= listY + listHeight) {
                            mc.textRenderer.drawWithShadow(matrices, display, x + 10, itemY + 3, color);
                        }
                    }
                    itemY += 15;
                }
            } else {
                String translatedEmpty = LanguageManager.getInstance().get("Playlist is empty");
                mc.textRenderer.drawWithShadow(matrices, translatedEmpty, x + 12, listY + 10, 0xFF949BA4);
            }
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (addTrackField != null && addTrackField.mouseClicked(mouseX, mouseY, button)) {
            if (addTrackField.isFocused()) {
                addTrackField.setSuggestion("");
            }
            return true;
        }
        
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (mouseY <= y + 15 && button == 0) {
                dragging = true;
                dragOffsetX = (int)mouseX - x;
                dragOffsetY = (int)mouseY - y;
                return true;
            }
            
            Radio radio = NexusPVP.getInstance().getModuleManager().getModule(Radio.class);
            if (radio == null) return true;
            
            int btnY = y + 45;
            
            if (mouseX >= x + 15 && mouseX <= x + 55 && mouseY >= btnY && mouseY <= btnY + 15) {
                radio.skipTrack(-1);
            }
            if (mouseX >= x + 70 && mouseX <= x + 110 && mouseY >= btnY && mouseY <= btnY + 15) {
                radio.toggle();
            }
            if (mouseX >= x + 125 && mouseX <= x + 165 && mouseY >= btnY && mouseY <= btnY + 15) {
                radio.skipTrack(1);
            }
            
            // Playlist click
            int listY = y + 85;
            int listHeight = height - 85 - 5;
            if (mouseX >= x + 5 && mouseX <= x + width - 5 && mouseY >= listY && mouseY <= listY + listHeight) {
                if (button == 0) { // left click
                    int clickedIndex = (int)(mouseY - listY + scrollY) / 15;
                    List<String> playlist = radio.getPlaylist();
                    if (playlist != null && clickedIndex >= 0 && clickedIndex < playlist.size()) {
                        radio.playTrack(clickedIndex);
                    }
                }
            }
            
            return true;
        }
        return false;
    }
    
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            scrollY -= amount * 15;
            Radio radio = NexusPVP.getInstance().getModuleManager().getModule(Radio.class);
            if (radio != null) {
                List<String> playlist = radio.getPlaylist();
                if (playlist != null) {
                    int maxScroll = Math.max(0, playlist.size() * 15 - (height - 85 - 5));
                    if (scrollY < 0) scrollY = 0;
                    if (scrollY > maxScroll) scrollY = maxScroll;
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (addTrackField != null && addTrackField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                String text = addTrackField.getText().trim();
                if (!text.isEmpty()) {
                    Radio.addTrack(text);
                    addTrackField.setText("");
                    addTrackField.setSuggestion(LanguageManager.getInstance().get("Added!"));
                }
                return true;
            }
            return addTrackField.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }
    
    public boolean charTyped(char chr, int modifiers) {
        if (addTrackField != null && addTrackField.isFocused()) {
            return addTrackField.charTyped(chr, modifiers);
        }
        return false;
    }
}