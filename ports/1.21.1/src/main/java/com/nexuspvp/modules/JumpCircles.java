package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JumpCircles extends Module {

    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 240, 255)));
    private final NumberSetting maxRadius = addSetting(new NumberSetting("MaxRadius", 2.2, 0.5, 5.0, 0.1));
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 1.2, 0.2, 3.0, 0.1));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 6.0, 0.5));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));

    private final List<CircleAnim> circles = new ArrayList<>();
    private boolean wasOnGround = true;

    public JumpCircles() {
        super("JumpCircles", "Expanding shockwave circles at feet on jump", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        if (wasOnGround && !onGround && mc.player.getVelocity().y > 0.1) {
            Color c = rainbow.isEnabled() ? ColorUtils.rainbow(System.currentTimeMillis()) : color.getColor();
            circles.add(new CircleAnim(mc.player.getPos(), c));
        }
        wasOnGround = onGround;

        circles.removeIf(CircleAnim::isDead);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (circles.isEmpty() || mc.player == null || mc.gameRenderer == null) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float maxR = maxRadius.getFloatValue();
        float spd = speed.getFloatValue();
        float lw = lineWidth.getFloatValue();

        for (CircleAnim ca : circles) {
            float progress = ca.getProgress(spd);
            float curR = progress * maxR;
            float alpha = 1.0f - progress;
            int a = (int) (alpha * 240);

            Color c = new Color(ca.color.getRed(), ca.color.getGreen(), ca.color.getBlue(), Math.max(0, Math.min(255, a)));
            Vec3d rPos = ca.pos.subtract(cam);

            RenderUtils.drawCircle3D(matrices, rPos.x, rPos.y + 0.05, rPos.z, curR, c, lw);
            RenderUtils.drawCircle3D(matrices, rPos.x, rPos.y + 0.05, rPos.z, curR * 0.7f, new Color(c.getRed(), c.getGreen(), c.getBlue(), a / 2), lw * 0.7f);
        }
    }

    private static class CircleAnim {
        Vec3d pos;
        Color color;
        long startTime;

        CircleAnim(Vec3d pos, Color color) {
            this.pos = pos;
            this.color = color;
            this.startTime = System.currentTimeMillis();
        }

        float getProgress(float speed) {
            long elapsed = System.currentTimeMillis() - startTime;
            return Math.min(1.0f, (elapsed / 1000.0f) * speed);
        }

        boolean isDead() {
            return getProgress(1.0f) >= 1.0f;
        }
    }
}
