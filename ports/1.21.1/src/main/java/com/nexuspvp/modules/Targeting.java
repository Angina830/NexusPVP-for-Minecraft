package com.nexuspvp.modules;

import com.nexuspvp.module.Module;
import com.nexuspvp.module.Category;
import com.nexuspvp.setting.*;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import java.awt.Color;

public class Targeting extends Module {
    private final ColorSetting color = addSetting(new ColorSetting("Color", Color.RED));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 4.0, 2.0, 8.0, 0.5));
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Circle", "Circle", "Box", "Diamond"));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.0, 1.0, 5.0, 0.5));
    private final BooleanSetting animate = addSetting(new BooleanSetting("Animate", true));

    public Targeting() {
        super("Targeting", "Highlights targeted entity", Category.VISUAL);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null) return;
        
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) mc.crosshairTarget).getEntity();
            if (entity instanceof LivingEntity && !entity.isInvisible() && mc.player.distanceTo(entity) <= range.getFloatValue()) {
                Vec3d pos = RenderUtils.getInterpolatedPos(entity, tickDelta);
                
                if (style.is("Circle")) {
                    double yOffset = animate.isEnabled() ? (Math.sin(System.currentTimeMillis() / 200.0) * 0.5 + 0.5) * entity.getHeight() : 0;
                    RenderUtils.drawBloomCircle3D(pos.add(0, yOffset, 0), entity.getWidth(), 32, color.getColor(), lineWidth.getFloatValue());
                }
            }
        }
    }
}
