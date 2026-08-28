package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JumpCircles extends Module {
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Shockwave", "Shockwave", "NeonRing", "GradientDisc", "DoubleWave"));
    private final NumberSetting maxRadius = addSetting(new NumberSetting("MaxRadius", 2.2, 0.8, 5.0, 0.1));
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 1.2, 0.3, 3.0, 0.1));
    private final NumberSetting thickness = addSetting(new NumberSetting("Thickness", 0.15, 0.05, 0.5, 0.01));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 230, 255, 220)));

    private final List<CircleAnimation> circles = new ArrayList<>();
    private boolean wasOnGround = true;

    public JumpCircles() {
        super("JumpCircles", "Expanding neon shockwaves at feet on jump & landing", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        boolean onGround = mc.player.isOnGround();
        if (wasOnGround && !onGround && mc.player.getVelocity().y > 0.05) {
            spawnCircle(mc.player.getPos());
        } else if (!wasOnGround && onGround && mc.player.fallDistance > 0.6f) {
            spawnCircle(mc.player.getPos());
        }
        wasOnGround = onGround;

        float speedVal = speed.getFloatValue() * 0.04f;
        float maxR = maxRadius.getFloatValue();
        Iterator<CircleAnimation> it = circles.iterator();
        while (it.hasNext()) {
            CircleAnimation c = it.next();
            c.progress += speedVal;
            c.radius = (float) (maxR * Math.sin(Math.min(1.0, c.progress) * Math.PI * 0.5));
            c.alpha = (float) Math.max(0.0, 1.0 - c.progress);
            if (c.progress >= 1.0f) {
                it.remove();
            }
        }
    }

    private void spawnCircle(Vec3d pos) {
        Color c = rainbow.isEnabled() ? ColorUtils.rainbow(0) : color.getColor();
        circles.add(new CircleAnimation(new Vec3d(pos.x, pos.y + 0.03, pos.z), c));
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null || circles.isEmpty()) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float thick = thickness.getFloatValue();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        for (CircleAnimation c : circles) {
            if (c.alpha <= 0.01f) continue;

            matrices.push();
            matrices.translate(c.pos.x - cam.x, c.pos.y - cam.y, c.pos.z - cam.z);
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            int alpha = (int) (c.color.getAlpha() * c.alpha);
            if (alpha <= 0) {
                matrices.pop();
                continue;
            }

            int r = c.color.getRed();
            int g = c.color.getGreen();
            int b = c.color.getBlue();
            int segments = 40;

            float innerR = Math.max(0f, c.radius - thick);
            float outerR = c.radius;

            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double angle = 2 * Math.PI * i / segments;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                buffer.vertex(matrix, innerR * cos, 0f, innerR * sin).color(r, g, b, 0);
                buffer.vertex(matrix, outerR * cos, 0f, outerR * sin).color(r, g, b, alpha);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            matrices.pop();
        }

        RenderSystem.disableBlend();
    }

    private static class CircleAnimation {
        final Vec3d pos;
        final Color color;
        float progress = 0.0f;
        float radius = 0.0f;
        float alpha = 1.0f;

        CircleAnimation(Vec3d pos, Color color) {
            this.pos = pos;
            this.color = color;
        }
    }
}
