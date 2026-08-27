package com.nexuspvp.module;

import com.nexuspvp.modules.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        register(new Ambience());
        register(new BabyMode());
        register(new ClickGuiModule());
        register(new HitColor());
        register(new HudModule());
        register(new Particles());
        register(new JumpCircles());
        register(new Trails());
        register(new JumpParticles());
        register(new SwordSlash());
        register(new SwingAnimations());
        register(new Targeting());
        register(new ViewModel());
        register(new Zoom());
        register(new DamageIndicator());
        register(new LowFire());
        register(new NoSlowFOV());
        register(new NoHurtCam());
        register(new TargetHUD());
        register(new ArmorHUD());
        register(new PotionHUD());
        register(new Crosshair());
        register(new HitSounds());
        register(new CommandKeybinds());
        register(new SmartSprint());
        register(new Radio());
        register(new Keystrokes());
        register(new TotemPop());
        register(new ItemCooldowns());
        register(new BlockOutline());
        register(new AttackVignette());
        register(new OverheadHealth());
        register(new GalaxySky());
        register(new ShulkerPreview());
        register(new MotionBlur());
        register(new TrajectoryPreview());
        register(new TNTTimer());
        register(new ClearWater());
        register(new ChatTweaks());
        register(new DebugLogger());
    }

    private void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getEnabledModules() {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                result.add(module);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (module.getClass() == clazz) {
                return (T) module;
            }
        }
        return null;
    }

    public Optional<Module> getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public void onRender2D(net.minecraft.client.util.math.MatrixStack matrices, float tickDelta) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onRender2D(matrices, tickDelta);
            }
        }
    }

    public void onRender3D(net.minecraft.client.util.math.MatrixStack matrices, float tickDelta) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onRender3D(matrices, tickDelta);
            }
        }
    }

    public void onKeyPress(int keyCode) {
        onKeyPressed(keyCode);
    }

    public void onKeyPressed(int keyCode) {
        for (Module module : modules) {
            if (module.getKeyBind() == keyCode && keyCode != 0) {
                module.toggle();
            }
        }
    }
}
