package com.nexuspvp;

import com.nexuspvp.config.ConfigManager;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class NexusPVP implements ClientModInitializer {
    private static NexusPVP instance;
    private ModuleManager moduleManager;
    private ThemeManager themeManager;
    private ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        instance = this;
        themeManager = new ThemeManager();
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();

        configManager.loadConfig();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (moduleManager != null) {
                moduleManager.onTick();
            }
        });

        WorldRenderEvents.LAST.register(context -> {
            if (moduleManager != null && context.matrixStack() != null) {
                moduleManager.onRender3D(context.matrixStack(), context.tickCounter().getTickDelta(true));
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (configManager != null) {
                configManager.saveConfig();
            }
        }));

        System.out.println("[NexusPVP] Mod initialized! Version 0.1.33-beta [Occlusion](Standalone)");
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
