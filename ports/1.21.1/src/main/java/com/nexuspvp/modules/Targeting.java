package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class Targeting extends Module {

    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 60, 60, 220)));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 5.0, 2.0, 12.0, 0.5));
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Circle", "Circle", "Box", "Diamond"));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 5.0, 0.5));
    private final BooleanSetting animate = addSetting(new BooleanSetting("Animate", true));

    public Targeting() {
        super("Targeting", "Highlights targeted entity with visual effects", Category.VISUAL);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.targetedEntity == null || !(mc.targetedEntity instanceof LivingEntity) || mc.player == null || mc.gameRenderer == null) return;

        LivingEntity entity = (LivingEntity) mc.targetedEntity;
        if (entity.squaredDistanceTo(mc.player) > range.getValue() * range.getValue()) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()) - cam.x;
        double y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()) - cam.y;
        double z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()) - cam.z;

        Color c = color.getColor();
        float lw = lineWidth.getFloatValue();
        float width = entity.getWidth();
        float height = entity.getHeight();

        String s = style.getValue();
        if (s.equals("Circle")) {
            float rot = animate.isEnabled() ? (System.currentTimeMillis() % 3600) / 10.0f : 0.0f;
            matrices.push();
            matrices.translate(x, y + 0.05, z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot));
            RenderUtils.drawCircle3D(matrices, 0, 0, 0, width * 0.75f, c, lw);
            RenderUtils.drawCircle3D(matrices, 0, 0, 0, width * 0.95f, new Color(c.getRed(), c.getGreen(), c.getBlue(), 120), lw * 0.6f);
            matrices.pop();
        } else if (s.equals("Box")) {
            RenderUtils.drawBox3D(matrices, x - width / 2, y, z - width / 2, x + width / 2, y + height, z + width / 2, c, lw);
        } else if (s.equals("Diamond")) {
            matrices.push();
            matrices.translate(x, y + height + 0.4, z);
            matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
            float anim = (float) Math.sin(System.currentTimeMillis() / 200.0) * 0.05f;
            RenderUtils.drawCircle3D(matrices, 0, 0, 0, 0.25f + anim, c, lw);
            matrices.pop();
        }
    }
}
