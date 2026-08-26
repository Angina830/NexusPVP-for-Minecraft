package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class Nimb extends Module {

    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 220, 50, 200)));
    private final NumberSetting radius = addSetting(new NumberSetting("Radius", 0.45, 0.2, 1.0, 0.05));
    private final NumberSetting yOffset = addSetting(new NumberSetting("YOffset", 0.4, 0.1, 0.8, 0.05));
    private final BooleanSetting glow = addSetting(new BooleanSetting("Glow", true));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final BooleanSetting thirdPersonOnly = addSetting(new BooleanSetting("ThirdPersonOnly", true));

    public Nimb() {
        super("Nimb", "Glowing angel halo above player head", Category.VISUAL);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.gameRenderer == null) return;
        if (thirdPersonOnly.isEnabled() && mc.options.getPerspective() == Perspective.FIRST_PERSON) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float bob = (float) Math.sin(System.currentTimeMillis() / 300.0) * 0.03f;
        double px = MathHelper.lerp(tickDelta, mc.player.lastRenderX, mc.player.getX()) - cam.x;
        double py = MathHelper.lerp(tickDelta, mc.player.lastRenderY, mc.player.getY()) + mc.player.getEyeHeight(mc.player.getPose()) + yOffset.getValue() + bob - cam.y;
        double pz = MathHelper.lerp(tickDelta, mc.player.lastRenderZ, mc.player.getZ()) - cam.z;

        float r = radius.getFloatValue();
        Color c = rainbow.isEnabled() ? ColorUtils.rainbow(System.currentTimeMillis()) : color.getColor();

        matrices.push();
        matrices.translate(px, py, pz);
        matrices.multiply(net.minecraft.util.math.Vec3f.POSITIVE_X.getDegreesQuaternion(12.0f));

        float rot = (System.currentTimeMillis() % 4000) / 11.1f;
        matrices.multiply(net.minecraft.util.math.Vec3f.POSITIVE_Y.getDegreesQuaternion(rot));

        RenderUtils.drawCircle3D(matrices, 0, 0, 0, r, c, 3.0f);
        if (glow.isEnabled()) {
            Color glowCol = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha() * 0.4f));
            RenderUtils.drawCircle3D(matrices, 0, 0.02, 0, r + 0.04f, glowCol, 1.5f);
            RenderUtils.drawCircle3D(matrices, 0, -0.02, 0, r - 0.04f, glowCol, 1.5f);
        }

        matrices.pop();
    }
}
