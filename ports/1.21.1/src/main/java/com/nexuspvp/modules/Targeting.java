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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;

public class Targeting extends Module {
    private final ModeSetting style = addSetting(new ModeSetting("Style", "PulsingDoubleRing", "PulsingDoubleRing", "RotatingDiamond", "HologramBrackets"));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 5.0, 2.0, 10.0, 0.5));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 5.0, 0.5));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 240, 255, 230)));

    private float animTicks = 0f;

    public Targeting() {
        super("Targeting", "Futuristic 3D holographic targeting reticle & glowing aura on target", Category.VISUAL);
    }

    @Override
    public void onTick() {
        animTicks += 0.05f;
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (!(entity instanceof LivingEntity) || entity == mc.player) return;
        if (mc.player.distanceTo(entity) > range.getFloatValue()) return;

        LivingEntity target = (LivingEntity) entity;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Vec3d targetPos = target.getPos();
        Color c = rainbow.isEnabled() ? ColorUtils.rainbow(0) : color.getColor();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        int a = c.getAlpha();

        float pulse = (float) Math.sin(animTicks * 3.0f) * 0.08f;
        float r1 = 0.65f + pulse;

        matrices.push();
        matrices.translate(targetPos.x - cam.x, targetPos.y - cam.y + 0.05, targetPos.z - cam.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(animTicks * 50f));
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 32; i++) {
            double angle = 2 * Math.PI * i / 32;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            buffer.vertex(matrix, (r1 - 0.08f) * cos, 0f, (r1 - 0.08f) * sin).color(r, g, b, 0);
            buffer.vertex(matrix, r1 * cos, 0f, r1 * sin).color(r, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
        RenderSystem.disableBlend();
    }
}
