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
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.LinkedList;

public class Trails extends Module {

    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(255, 0, 220)));
    private final NumberSetting length = addSetting(new NumberSetting("Length", 24, 5, 60, 1));
    private final NumberSetting width = addSetting(new NumberSetting("Width", 2.5, 1.0, 6.0, 0.5));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final BooleanSetting thirdPersonOnly = addSetting(new BooleanSetting("ThirdPersonOnly", true));

    private final LinkedList<TrailPoint> points = new LinkedList<>();

    public Trails() {
        super("Trails", "Bright motion ribbon trail behind player", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        Vec3d pos = mc.player.getPos().add(0, 0.2, 0);
        points.addFirst(new TrailPoint(pos, System.currentTimeMillis()));

        int maxLen = length.getIntValue();
        while (points.size() > maxLen) {
            points.removeLast();
        }

        long now = System.currentTimeMillis();
        points.removeIf(p -> now - p.time > 1500);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (points.size() < 2 || mc.player == null || mc.gameRenderer == null) return;
        if (thirdPersonOnly.isEnabled() && mc.options.getPerspective() == Perspective.FIRST_PERSON) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float lw = width.getFloatValue();
        int total = points.size();

        for (int i = 0; i < total - 1; i++) {
            TrailPoint p1 = points.get(i);
            TrailPoint p2 = points.get(i + 1);

            float pct = 1.0f - ((float) i / total);
            int alpha = (int) (pct * 230);

            Color baseCol = rainbow.isEnabled() ? ColorUtils.rainbow(System.currentTimeMillis() - i * 50L) : color.getColor();
            Color c = new Color(baseCol.getRed(), baseCol.getGreen(), baseCol.getBlue(), Math.max(0, Math.min(255, alpha)));

            Vec3d r1 = p1.pos.subtract(cam);
            Vec3d r2 = p2.pos.subtract(cam);

            RenderUtils.drawLine3D(matrices, r1.x, r1.y, r1.z, r2.x, r2.y, r2.z, c, lw);
        }
    }

    private static class TrailPoint {
        Vec3d pos;
        long time;

        TrailPoint(Vec3d pos, long time) {
            this.pos = pos;
            this.time = time;
        }
    }
}
