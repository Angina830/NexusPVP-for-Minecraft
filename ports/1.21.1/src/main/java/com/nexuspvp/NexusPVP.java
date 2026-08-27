package com.nexuspvp;

import com.nexuspvp.config.ConfigManager;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;

public class NexusPVP implements ClientModInitializer {
    private static NexusPVP instance;
    private ModuleManager moduleManager;
    private ThemeManager themeManager;
    private ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.LAST.register(context -> {
            if (moduleManager != null) {
                moduleManager.onRender3D(context.matrixStack(), context.tickCounter().getTickDelta(false));
            }
        });
        instance = this;
        themeManager = new ThemeManager();
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();

        configManager.loadConfig();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (configManager != null) {
                configManager.saveConfig();
            }
        }));

        System.out.println("[NexusPVP] Mod initialized! Version 0.1.27-beta [Chroma](Standalone)");
    }

    public static NexusPVP getInstance() {
        return instance;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
