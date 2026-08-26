package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;


import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CommandKeybinds extends Module {

    private final BooleanSetting showToast = addSetting(new BooleanSetting("ShowNotification", true));
    private final Map<Integer, String> binds = new HashMap<>();

    public CommandKeybinds() {
        super("CommandKeybinds", "Binds chat and server commands to keyboard keys", Category.PLAYER);
        loadBinds();
    }

    public void loadBinds() {
        binds.clear();
        File dir = new File(MinecraftClient.getInstance().runDirectory, "nexus_pvp");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "keybinds.txt");

        if (!file.exists()) {
            // Default sample binds for HolyWorld / PvP
            binds.put(GLFW.GLFW_KEY_H, "/feed");
            binds.put(GLFW.GLFW_KEY_J, "/fix all");
            binds.put(GLFW.GLFW_KEY_K, "/heal");
            binds.put(GLFW.GLFW_KEY_L, "/home");
            saveBinds();
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        try {
                            int key = Integer.parseInt(parts[0].trim());
                            String cmd = parts[1].trim();
                            binds.put(key, cmd);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveBinds() {
        File dir = new File(MinecraftClient.getInstance().runDirectory, "nexus_pvp");
        File file = new File(dir, "keybinds.txt");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            pw.println("# CommandKeybinds Config - Format: KEY_CODE=COMMAND");
            pw.println("# Example: 72=/feed (Key 72 is H)");
            for (Map.Entry<Integer, String> entry : binds.entrySet()) {
                pw.println(entry.getKey() + "=" + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBind(int keyCode, String command) {
        binds.put(keyCode, command);
        saveBinds();
    }

    public void removeBind(int keyCode) {
        binds.remove(keyCode);
        saveBinds();
    }

    public Map<Integer, String> getBinds() {
        return binds;
    }

    public void handleKey(int key) {
        if (!this.isEnabled() || mc.player == null || mc.currentScreen != null) return;

        String cmd = binds.get(key);
        if (cmd != null && !cmd.isEmpty()) {
            if (!cmd.startsWith("/")) {
                cmd = "/" + cmd;
            }
            Compat.sendChat(cmd);

            if (showToast.isEnabled()) {
                mc.player.sendMessage(Text.literal("\u00a7b[NexusPVP]\u00a7f \u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u00a7e" + cmd + "\u00a7f \u0432\u044b\u043f\u043e\u043b\u043d\u0435\u043d\u0430!"), true);
            }
        }
    }
}