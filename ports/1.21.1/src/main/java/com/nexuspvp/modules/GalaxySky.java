package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GalaxySky extends Module {

    private final ColorSetting starColor = addSetting(new ColorSetting("StarColor", new Color(190, 140, 255)));
    private final NumberSetting starCount = addSetting(new NumberSetting("Stars", 250, 50, 600, 25));
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1));
    private final BooleanSetting rotate = addSetting(new BooleanSetting("Rotate", true));

    private final List<Vec3d> stars = new ArrayList<>();

    public GalaxySky() {
        super("GalaxySky", "Transforms the night sky with animated nebulae and custom starfield", Category.RENDER);
        generateStars();
    }

    private void generateStars() {
        stars.clear();
        Random r = new Random(424242);
        for (int i = 0; i < 400; i++) {
            double u = r.nextDouble();
            double v = r.nextDouble();
            double theta = 2.0 * Math.PI * u;
            double phi = Math.acos(2.0 * v - 1.0);
            double dist = 80.0;
            double x = dist * Math.sin(phi) * Math.cos(theta);
            double y = Math.abs(dist * Math.cos(phi)) + 5.0;
            double z = dist * Math.sin(phi) * Math.sin(theta);
            stars.add(new Vec3d(x, y, z));
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.gameRenderer == null) return;
        if (stars.isEmpty()) generateStars();

        int count = Math.min(stars.size(), starCount.getIntValue());
        Color sc = starColor.getColor();
        float rot = rotate.isEnabled() ? (System.currentTimeMillis() % 72000) / (200.0f / speed.getFloatValue()) : 0.0f;

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot));

        for (int i = 0; i < count; i++) {
            Vec3d sp = stars.get(i);
            int tw = (int) (180 + Math.sin(i * 10 + System.currentTimeMillis() / 400.0) * 70);
            Color c = new Color(sc.getRed(), sc.getGreen(), sc.getBlue(), Math.max(0, Math.min(255, tw)));
            RenderUtils.drawCircle3D(matrices, sp.x, sp.y, sp.z, 0.35f, c, 1.5f);
        }

        matrices.pop();
    }
}
