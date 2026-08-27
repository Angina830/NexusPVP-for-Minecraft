package com.nexuspvp;

import com.nexuspvp.config.ConfigManager;
import com.nexuspvp.gui.ClickGui;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class NexusPVP implements ClientModInitializer {
    private static NexusPVP instance;
    private ModuleManager moduleManager;
    private ThemeManager themeManager;
    private ConfigManager configManager;
    public static KeyBinding clickGuiKey;

    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.LAST.register(context -> {
            if (moduleManager != null) {
                moduleManager.onRender3D(context.matrixStack(), context.tickDelta());
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
        
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nexuspvp.clickgui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.nexuspvp"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            moduleManager.onTick();
            
            while (clickGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    ClickGui.openCurrentStyleScreen();
                }
            }
        });
        
        System.out.println("[NexusPVP] Mod initialized! Version 0.1.29-beta [Polaris]");
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