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

    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 240, 255, 230)));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 5.0, 2.0, 12.0, 0.5));
    private final ModeSetting style = addSetting(new ModeSetting("Style", "NeonCircle", "NeonCircle", "NeonBox", "Diamond"));
    private final BooleanSetting animate = addSetting(new BooleanSetting("Animate", true));

    public Targeting() {
        super("Targeting", "Highlights targeted entity with neon bloom visual effects", Category.VISUAL);
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
        float width = entity.getWidth();
        float height = entity.getHeight();

        String s = style.getValue();
        if (s.equals("NeonCircle")) {
            float rot = animate.isEnabled() ? (System.currentTimeMillis() % 3600) / 10.0f : 0.0f;
            matrices.push();
            matrices.translate(x, y + 0.05, z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot));
            RenderUtils.drawNeonCircle3D(matrices, 0, 0, 0, width * 0.75f, c);
            matrices.pop();
        } else if (s.equals("NeonBox")) {
            RenderUtils.drawNeonBox3D(matrices, x - width / 2, y, z - width / 2, x + width / 2, y + height, z + width / 2, c, true, 25);
        } else if (s.equals("Diamond")) {
            matrices.push();
            matrices.translate(x, y + height + 0.4, z);
            matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
            float anim = (float) Math.sin(System.currentTimeMillis() / 200.0) * 0.05f;
            RenderUtils.drawNeonCircle3D(matrices, 0, 0, 0, 0.22f + anim, c);
            matrices.pop();
        }
    }
}
