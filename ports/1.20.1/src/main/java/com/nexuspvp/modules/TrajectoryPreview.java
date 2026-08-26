package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryPreview extends Module {

    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 255, 200, 220)));
    private final BooleanSetting showHitBlock = addSetting(new BooleanSetting("HitBox", true));
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Smooth", "Smooth", "Dotted"));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("Width", 2.0, 1.0, 5.0, 0.5));

    public TrajectoryPreview() {
        super("TrajectoryPreview", "Draws flight trajectory for pearls, bows, and potions", Category.VISUAL);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null) return;

        ItemStack stack = mc.player.getMainHandStack();
        if (!isThrowable(stack)) {
            stack = mc.player.getOffHandStack();
        }
        if (!isThrowable(stack)) return;

        Item item = stack.getItem();
        float power = 1.5f;
        float gravity = 0.03f;
        float drag = 0.99f;

        if (item instanceof BowItem) {
            int useTicks = mc.player.getItemUseTime();
            float pull = BowItem.getPullProgress(useTicks > 0 ? useTicks : 20);
            power = pull * 3.0f;
            gravity = 0.05f;
        } else if (item instanceof CrossbowItem) {
            power = 3.15f;
            gravity = 0.05f;
        } else if (item instanceof PotionItem) {
            power = 0.5f;
            gravity = 0.05f;
        } else if (item instanceof TridentItem) {
            power = 2.5f;
            gravity = 0.05f;
        }

        float yaw = mc.player.getYaw(tickDelta);
        float pitch = mc.player.getPitch(tickDelta);

        double posX = MathHelper.lerp(tickDelta, mc.player.lastRenderX, mc.player.getX());
        double posY = MathHelper.lerp(tickDelta, mc.player.lastRenderY, mc.player.getY()) + mc.player.getEyeHeight(mc.player.getPose()) - 0.1;
        double posZ = MathHelper.lerp(tickDelta, mc.player.lastRenderZ, mc.player.getZ());

        double motionX = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F) * power;
        double motionY = -MathHelper.sin(pitch * 0.017453292F) * power;
        double motionZ = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F) * power;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        List<Vec3d> points = new ArrayList<>();
        Vec3d cur = new Vec3d(posX, posY, posZ);
        points.add(cur);

        HitResult hit = null;
        for (int i = 0; i < 100; i++) {
            Vec3d next = cur.add(motionX, motionY, motionZ);
            hit = mc.world.raycast(new RaycastContext(cur, next, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                points.add(hit.getPos());
                break;
            }
            points.add(next);
            cur = next;
            motionX *= drag;
            motionY = (motionY * drag) - gravity;
            motionZ *= drag;
        }

        Color c = color.getColor();
        float lw = lineWidth.getFloatValue();

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d p1 = points.get(i).subtract(cam);
            Vec3d p2 = points.get(i + 1).subtract(cam);
            RenderUtils.drawLine3D(matrices, p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, c, lw);
        }

        if (showHitBlock.isEnabled() && hit instanceof BlockHitResult) {
            BlockPos bp = ((BlockHitResult) hit).getBlockPos();
            RenderUtils.drawBlockOutline3D(bp, c, 1.5f, true, 40);
        }
    }

    private boolean isThrowable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof EnderPearlItem || item instanceof BowItem || item instanceof CrossbowItem ||
               item instanceof PotionItem || item instanceof SnowballItem || item instanceof EggItem ||
               item instanceof TridentItem;
    }
}
