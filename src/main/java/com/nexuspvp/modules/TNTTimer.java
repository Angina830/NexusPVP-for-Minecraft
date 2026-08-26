package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class TNTTimer extends Module {

    private final BooleanSetting showRadius = addSetting(new BooleanSetting("ShowRadius", true));

    public TNTTimer() {
        super("TNTTimer", "Shows remaining fuse seconds and blast radius over primed TNT", Category.RENDER);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof TntEntity)) continue;

            TntEntity tnt = (TntEntity) entity;
            float fuseSec = tnt.getFuse() / 20.0f;
            if (fuseSec <= 0) continue;

            double x = MathHelper.lerp(tickDelta, tnt.prevX, tnt.getX());
            double y = MathHelper.lerp(tickDelta, tnt.prevY, tnt.getY()) + 0.8;
            double z = MathHelper.lerp(tickDelta, tnt.prevZ, tnt.getZ());

            // 1. Draw 3D Billboard Countdown Badge
            matrices.push();
            matrices.translate(x - camPos.x, y - camPos.y + 0.5, z - camPos.z);
            matrices.multiply(camera.getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            String text = String.format("%.1fs", fuseSec);
            int tw = mc.textRenderer.getWidth(text);

            int color;
            if (fuseSec > 2.5f) {
                color = 0xFF22C55E; // Green
            } else if (fuseSec > 1.2f) {
                color = 0xFFEAB308; // Yellow
            } else {
                color = (System.currentTimeMillis() % 200 < 100) ? 0xFFFFFFFF : 0xFFEF4444; // Flashing Red
            }

            RenderUtils.drawRoundedRect(matrices, -tw / 2 - 4, -6, tw + 8, 14, 3, 0xEE1E1F22);
            RenderUtils.drawRoundedRect(matrices, -tw / 2 - 5, -7, tw + 10, 16, 4, 0x55000000);
            mc.textRenderer.drawWithShadow(matrices, text, -tw / 2, -3, color);

            matrices.pop();

            // 2. Draw 3D Blast Radius Circle (Optional)
            if (showRadius.isEnabled()) {
                Vec3d pos = new Vec3d(x, MathHelper.lerp(tickDelta, tnt.prevY, tnt.getY()) + 0.05, z);
                Color ringCol = (fuseSec > 1.2f) ? new Color(234, 179, 8, 120) : new Color(239, 68, 68, 180);
                RenderUtils.drawCircle3D(pos, 4.0, 32, ringCol, 2.0f);
            }
        }
    }
}