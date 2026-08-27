package com.nexuspvp.config;
import com.nexuspvp.util.Compat;


import com.google.gson.*;
import com.nexuspvp.NexusPVP;
import com.nexuspvp.gui.LanguageManager;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.*;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final Path configDir;
    private final Gson gson;
    private String currentConfigName = "default";

    public ConfigManager() {
        this.configDir = FabricLoader.getInstance().getConfigDir().resolve("glebkavisuals");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        saveConfig(currentConfigName);
    }

    public void saveConfig(String configName) {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path file = configDir.resolve(configName + ".json");
            JsonObject root = new JsonObject();

            // Save Global Settings (Theme, Language)
            JsonObject globals = new JsonObject();
            if (NexusPVP.getInstance().getThemeManager() != null) {
                globals.addProperty("theme", NexusPVP.getInstance().getThemeManager().getCurrentTheme());
                globals.addProperty("style", NexusPVP.getInstance().getThemeManager().getCurrentStyle().name());
            }
            globals.addProperty("russian", LanguageManager.getInstance().isRussian());
            root.add("globals", globals);

            // Save Modules
            JsonObject modulesObj = new JsonObject();
            for (Module module : NexusPVP.getInstance().getModuleManager().getModules()) {
                // Do not auto-enable Radio or ClickGuiModule on startup
                if (module.getName().equalsIgnoreCase("Radio") || module.getName().equalsIgnoreCase("ClickGuiModule")) {
                    continue;
                }

                JsonObject modObj = new JsonObject();
                modObj.addProperty("enabled", module.isEnabled());
                modObj.addProperty("keyBind", module.getKeyBind());

                JsonObject settingsObj = new JsonObject();
                for (Setting<?> setting : module.getSettings()) {
                    saveSetting(settingsObj, setting);
                }
                modObj.add("settings", settingsObj);
                modulesObj.add(module.getName(), modObj);
            }
            root.add("modules", modulesObj);

            Files.write(file, gson.toJson(root).getBytes(StandardCharsets.UTF_8));
            System.out.println("[NexusPVP] Config saved: " + file.getFileName());
        } catch (Exception e) {
            System.err.println("[NexusPVP] Failed to save config: " + e.getMessage());
        }
    }

    public void loadConfig() {
        loadConfig(currentConfigName);
    }

    public void loadConfig(String configName) {
        try {
            Path file = configDir.resolve(configName + ".json");
            if (!Files.exists(file)) {
                System.out.println("[NexusPVP] Config file not found, creating default.");
                saveConfig(configName);
                return;
            }

            String json = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            JsonElement parsed = new JsonParser().parse(json);
            if (!parsed.isJsonObject()) return;
            JsonObject root = parsed.getAsJsonObject();

            // Load Globals
            if (root.has("globals")) {
                JsonObject globals = root.getAsJsonObject("globals");
                if (globals.has("theme") && NexusPVP.getInstance().getThemeManager() != null) {
                    NexusPVP.getInstance().getThemeManager().setTheme(globals.get("theme").getAsString());
                }
                if (globals.has("style") && NexusPVP.getInstance().getThemeManager() != null) {
                    try {
                        com.nexuspvp.gui.GuiStyle s = com.nexuspvp.gui.GuiStyle.valueOf(globals.get("style").getAsString());
                        NexusPVP.getInstance().getThemeManager().setStyle(s);
                    } catch (Exception ignored) {}
                }
                if (globals.has("russian")) {
                    LanguageManager.getInstance().setRussian(globals.get("russian").getAsBoolean());
                }
            }

            // Load Modules
            if (root.has("modules")) {
                JsonObject modulesObj = root.getAsJsonObject("modules");
                for (Module module : NexusPVP.getInstance().getModuleManager().getModules()) {
                    if (module.getName().equalsIgnoreCase("Radio") || module.getName().equalsIgnoreCase("ClickGuiModule")) {
                        continue;
                    }

                    if (!modulesObj.has(module.getName())) continue;
                    JsonObject modObj = modulesObj.getAsJsonObject(module.getName());

                    if (modObj.has("enabled")) {
                        boolean shouldEnable = modObj.get("enabled").getAsBoolean();
                        if (module.isEnabled() != shouldEnable) {
                            module.setEnabled(shouldEnable);
                        }
                    }

                    if (modObj.has("keyBind")) {
                        module.setKeyBind(modObj.get("keyBind").getAsInt());
                    }

                    if (modObj.has("settings")) {
                        JsonObject settingsObj = modObj.getAsJsonObject("settings");
                        for (Setting<?> setting : module.getSettings()) {
                            loadSetting(settingsObj, setting);
                        }
                    }
                }
            }
            this.currentConfigName = configName;
            System.out.println("[NexusPVP] Config loaded successfully: " + configName);
        } catch (Exception e) {
            System.err.println("[NexusPVP] Failed to load config: " + e.getMessage());
        }
    }

    private void saveSetting(JsonObject obj, Setting<?> setting) {
        if (setting instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) setting;
            obj.addProperty(setting.getName(), bs.getValue());
        } else if (setting instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) setting;
            obj.addProperty(setting.getName(), ns.getValue());
        } else if (setting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) setting;
            obj.addProperty(setting.getName(), ms.getValue());
        } else if (setting instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) setting;
            JsonObject colorObj = new JsonObject();
            colorObj.addProperty("r", cs.getRed());
            colorObj.addProperty("g", cs.getGreen());
            colorObj.addProperty("b", cs.getBlue());
            colorObj.addProperty("a", cs.getAlpha());
            colorObj.addProperty("rainbow", cs.isRainbow());
            obj.add(setting.getName(), colorObj);
        }
    }

    private void loadSetting(JsonObject obj, Setting<?> setting) {
        if (!obj.has(setting.getName())) return;

        try {
            JsonElement value = obj.get(setting.getName());
            if (setting instanceof BooleanSetting) {
                BooleanSetting bs = (BooleanSetting) setting;
                bs.setValue(value.getAsBoolean());
            } else if (setting instanceof NumberSetting) {
                NumberSetting ns = (NumberSetting) setting;
                ns.setValue(value.getAsDouble());
            } else if (setting instanceof ModeSetting) {
                ModeSetting ms = (ModeSetting) setting;
                ms.setValue(value.getAsString());
            } else if (setting instanceof ColorSetting) {
                ColorSetting cs = (ColorSetting) setting;
                if (value.isJsonObject()) {
                    JsonObject colorObj = value.getAsJsonObject();
                    int r = colorObj.has("r") ? colorObj.get("r").getAsInt() : 255;
                    int g = colorObj.has("g") ? colorObj.get("g").getAsInt() : 255;
                    int b = colorObj.has("b") ? colorObj.get("b").getAsInt() : 255;
                    int a = colorObj.has("a") ? colorObj.get("a").getAsInt() : 255;
                    boolean rainbow = colorObj.has("rainbow") && colorObj.get("rainbow").getAsBoolean();
                    cs.setValue(new Color(r, g, b, a));
                    cs.setRainbow(rainbow);
                }
            }
        } catch (Exception e) {
            System.err.println("[NexusPVP] Failed to load setting: " + setting.getName() + " -> " + e.getMessage());
        }
    }

    public List<String> getAvailableConfigs() {
        List<String> configs = new ArrayList<>();
        try {
            if (Files.exists(configDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDir, "*.json")) {
                    for (Path entry : stream) {
                        String name = entry.getFileName().toString();
                        if (name.endsWith(".json")) {
                            configs.add(name.substring(0, name.length() - 5));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (configs.isEmpty()) {
            configs.add("default");
        }
        return configs;
    }

    public String getCurrentConfigName() {
        return currentConfigName;
    }

    public void deleteConfig(String configName) {
        try {
            Path file = configDir.resolve(configName + ".json");
            if (Files.exists(file)) {
                Files.delete(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}