package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class TNTTimer extends Module {

    private final BooleanSetting showTime = addSetting(new BooleanSetting("ShowTime", true));
    private final BooleanSetting dangerZone = addSetting(new BooleanSetting("DangerZone", true));
    private final NumberSetting scale = addSetting(new NumberSetting("Scale", 1.0, 0.5, 2.5, 0.1));
    private final ColorSetting safeColor = addSetting(new ColorSetting("Safe", new Color(0, 255, 120)));
    private final ColorSetting dangerColor = addSetting(new ColorSetting("Danger", new Color(255, 40, 40)));

    public TNTTimer() {
        super("TNTTimer", "Shows precise countdown and danger zone over primed TNT", Category.VISUAL);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.world == null || mc.player == null) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();

        for (Entity e : mc.world.getEntities()) {
            if (e instanceof TntEntity) {
                TntEntity tnt = (TntEntity) e;
                int fuse = tnt.getFuse();
                float seconds = fuse / 20.0f;

                double x = MathHelper.lerp(tickDelta, tnt.lastRenderX, tnt.getX()) - cam.x;
                double y = MathHelper.lerp(tickDelta, tnt.lastRenderY, tnt.getY()) - cam.y;
                double z = MathHelper.lerp(tickDelta, tnt.lastRenderZ, tnt.getZ()) - cam.z;

                if (dangerZone.isEnabled()) {
                    Color dc = fuse < 20 ? dangerColor.getColor() : safeColor.getColor();
                    RenderUtils.drawCircle3D(matrices, x, y + 0.05, z, 4.0f, dc, 2.0f);
                }

                if (showTime.isEnabled()) {
                    matrices.push();
                    matrices.translate(x, y + 1.3, z);
                    matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
                    float sc = 0.025f * scale.getFloatValue();
                    matrices.scale(-sc, -sc, sc);

                    String text = String.format("%.2fs", seconds);
                    int tw = mc.textRenderer.getWidth(text);
                    int col = fuse < 20 ? 0xFFFF3333 : 0xFF33FF55;

                    mc.textRenderer.draw(text, -tw / 2.0f, 0.0f, col, false, matrices.peek().getPositionMatrix(),
                            mc.getBufferBuilders().getEntityVertexConsumers(),
                            net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                    matrices.pop();
                }
            }
        }
    }
}
