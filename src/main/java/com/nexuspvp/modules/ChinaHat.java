package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import net.minecraft.util.math.Vec3d;


import java.awt.Color;

public class ChinaHat extends Module {

    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 240, 255, 160)));
    private final NumberSetting radius = addSetting(new NumberSetting("Radius", 0.65, 0.3, 1.5, 0.05));
    private final NumberSetting height = addSetting(new NumberSetting("Height", 0.22, 0.0, 0.6, 0.02));
    private final NumberSetting yOffset = addSetting(new NumberSetting("YOffset", 0.35, 0.0, 0.8, 0.05));
    private final BooleanSetting rotate = addSetting(new BooleanSetting("Rotate", true));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final BooleanSetting thirdPersonOnly = addSetting(new BooleanSetting("ThirdPersonOnly", true));

    public ChinaHat() {
        super("ChinaHat", "Animated conical hat above player head", Category.VISUAL);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.gameRenderer == null) return;
        if (thirdPersonOnly.isEnabled() && mc.options.getPerspective() == Perspective.FIRST_PERSON) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double px = MathHelper.lerp(tickDelta, mc.player.lastRenderX, mc.player.getX()) - cam.x;
        double py = MathHelper.lerp(tickDelta, mc.player.lastRenderY, mc.player.getY()) + mc.player.getEyeHeight(mc.player.getPose()) + yOffset.getValue() - cam.y;
        double pz = MathHelper.lerp(tickDelta, mc.player.lastRenderZ, mc.player.getZ()) - cam.z;

        float r = radius.getFloatValue();
        float h = height.getFloatValue();
        Color c = rainbow.isEnabled() ? ColorUtils.rainbow(System.currentTimeMillis()) : color.getColor();
        float cr = c.getRed() / 255.0f;
        float cg = c.getGreen() / 255.0f;
        float cb = c.getBlue() / 255.0f;
        float ca = c.getAlpha() / 255.0f;

        matrices.push();
        matrices.translate(px, py, pz);

        if (rotate.isEnabled()) {
            float rot = (System.currentTimeMillis() % 3600) / 10.0f;
            matrices.multiply(net.minecraft.util.math.Vec3f.POSITIVE_Y.getDegreesQuaternion(rot));
        }

        int segments = 36;
        for (int i = 0; i < segments; i++) {
            double angle1 = (i * 2.0 * Math.PI) / segments;
            double angle2 = ((i + 1) * 2.0 * Math.PI) / segments;

            double x1 = Math.cos(angle1) * r;
            double z1 = Math.sin(angle1) * r;
            double x2 = Math.cos(angle2) * r;
            double z2 = Math.sin(angle2) * r;

            RenderUtils.drawLine3D(matrices, x1, 0, z1, x2, 0, z2, c, 2.0f);
            if (h > 0.01f) {
                RenderUtils.drawLine3D(matrices, x1, 0, z1, 0, h, 0, new Color(cr, cg, cb, ca * 0.7f), 1.5f);
            }
        }

        matrices.pop();
    }
}
