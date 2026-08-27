package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;

public class BlockOutline extends Module {
    private final ModeSetting mode = addSetting(new ModeSetting("Mode", "NeonGlow", "NeonGlow", "FilledBox", "Wireframe"));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 6.0, 0.5));
    private final NumberSetting fillAlpha = addSetting(new NumberSetting("FillAlpha", 45, 0, 200, 5));
    private final BooleanSetting rainbow = addSetting(new BooleanSetting("Rainbow", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 230, 255, 230)));

    public BlockOutline() {
        super("BlockOutline", "Glowing animated neon outlines & soft fills on targeted blocks", Category.VISUAL);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        if (state.isAir()) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Box box = state.getOutlineShape(mc.world, pos).getBoundingBox().offset(pos.getX(), pos.getY(), pos.getZ());
        Color c = rainbow.isEnabled() ? ColorUtils.rainbow(0) : color.getColor();
        int fAlpha = fillAlpha.getIntValue();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        if (fAlpha > 0) {
            matrices.push();
            matrices.translate(-cam.x, -cam.y, -cam.z);
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, b, fAlpha).next();
            buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, b, fAlpha).next();
            buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, b, fAlpha).next();
            buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, b, fAlpha).next();

            buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, b, fAlpha).next();
            buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, b, fAlpha).next();
            buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, b, fAlpha).next();
            buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, b, fAlpha).next();
            tessellator.draw();

            matrices.pop();
        }

        RenderSystem.disableBlend();
    }
}
