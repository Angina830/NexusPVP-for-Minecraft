package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class TNTTimer extends Module {

    private final BooleanSetting dangerZone = addSetting(new BooleanSetting("DangerZone", true));
    private final ColorSetting circleColor = addSetting(new ColorSetting("CircleColor", new Color(255, 50, 50, 180)));

    public TNTTimer() {
        super("TNTTimer", "Shows precise explosion countdown timer and danger radius above primed TNT", Category.RENDER);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.world == null || mc.player == null || mc.gameRenderer == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof TntEntity) {
                TntEntity tnt = (TntEntity) entity;
                int fuse = tnt.getFuse();
                float seconds = fuse / 20.0f;
                String text = String.format("%.2fs", seconds);

                double x = MathHelper.lerp(tickDelta, tnt.lastRenderX, tnt.getX()) - cam.x;
                double y = MathHelper.lerp(tickDelta, tnt.lastRenderY, tnt.getY()) + 1.2 - cam.y;
                double z = MathHelper.lerp(tickDelta, tnt.lastRenderZ, tnt.getZ()) - cam.z;

                matrices.push();
                matrices.translate(x, y, z);
                matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
                float scale = 0.035F;
                matrices.scale(-scale, -scale, scale);

                int col = fuse > 40 ? 0xFF55FF55 : (fuse > 20 ? 0xFFFFAA00 : 0xFFFF3333);
                int textW = mc.textRenderer.getWidth(text);
                Compat.drawWithShadow(mc.textRenderer, matrices, text, -textW / 2, 0, col);
                matrices.pop();

                if (dangerZone.isEnabled()) {
                    double by = MathHelper.lerp(tickDelta, tnt.lastRenderY, tnt.getY()) - cam.y + 0.05;
                    Color dc = fuse > 20 ? new Color(255, 170, 0, 180) : circleColor.getColor();
                    RenderUtils.drawNeonCircle3D(matrices, x, by, z, 4.0f, dc);
                }
            }
        }
    }
}
