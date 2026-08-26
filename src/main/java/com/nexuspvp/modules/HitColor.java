package com.nexuspvp.modules;

import com.nexuspvp.module.Module;
import com.nexuspvp.module.Category;
import com.nexuspvp.setting.*;
import net.minecraft.entity.LivingEntity;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class HitColor extends Module {
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 0, 0, 150)));
    private final NumberSetting opacity = addSetting(new NumberSetting("Opacity", 150, 10, 255, 5));
    private final NumberSetting duration = addSetting(new NumberSetting("Duration", 0.3, 0.1, 1.0, 0.1));
    private final ModeSetting mode = addSetting(new ModeSetting("Mode", "Solid", "Solid", "Fade", "Pulse"));

    private final Map<Integer, Long> hitTimes = new HashMap<>();
    private final Map<Integer, Float> lastHealth = new HashMap<>();

    public HitColor() {
        super("HitColor", "Entities flash a color when hit", Category.PVP);
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;
        
        for (net.minecraft.entity.Entity e : mc.world.getEntities()) {
            if (e instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) e;
                float health = living.getHealth() + living.getAbsorptionAmount();
                Float prev = lastHealth.get(e.getEntityId());
                if (prev != null && health < prev) {
                    hitTimes.put(e.getEntityId(), System.currentTimeMillis());
                }
                lastHealth.put(e.getEntityId(), health);
            }
        }
    }
    
    public boolean isEntityHit(int entityId) {
        if (!isEnabled()) return false;
        Long time = hitTimes.get(entityId);
        if (time == null) return false;
        return System.currentTimeMillis() - time < (duration.getFloatValue() * 1000);
    }
    
    public float getHitAlpha(int entityId) {
        if (!isEnabled()) return 0;
        Long time = hitTimes.get(entityId);
        if (time == null) return 0;
        
        long elapsed = System.currentTimeMillis() - time;
        long maxTime = (long)(duration.getFloatValue() * 1000);
        
        if (elapsed > maxTime) return 0;
        
        float baseAlpha = opacity.getIntValue() / 255.0f;
        if (mode.is("Fade")) {
            return baseAlpha * (1.0f - ((float)elapsed / maxTime));
        } else if (mode.is("Pulse")) {
            return baseAlpha * (float) (Math.sin(elapsed * 0.02) * 0.5 + 0.5);
        }
        return baseAlpha;
    }
    
    public Color getColor() {
        return color.getColor();
    }
    
    public int getOpacity() {
        return opacity.getIntValue();
    }
}
