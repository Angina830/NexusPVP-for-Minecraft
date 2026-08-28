package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryPreview extends Module {

    private final BooleanSetting customColor = addSetting(new BooleanSetting("CustomColor", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 220, 255)));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 5.0, 0.5));
    private final BooleanSetting showHitBox = addSetting(new BooleanSetting("HitBox", true));

    public TrajectoryPreview() {
        super("TrajectoryPreview", "Predicts and renders ballistic trajectory for pearls, bows and potions", Category.RENDER);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null) return;

        ItemStack stack = mc.player.getMainHandStack();
        if (!isThrowable(stack)) {
            stack = mc.player.getOffHandStack();
            if (!isThrowable(stack)) return;
        }

        Item item = stack.getItem();

        float pitch = mc.player.getPitch(tickDelta);
        float yaw = mc.player.getYaw(tickDelta);

        double posX = MathHelper.lerp(tickDelta, mc.player.lastRenderX, mc.player.getX());
        double posY = MathHelper.lerp(tickDelta, mc.player.lastRenderY, mc.player.getY()) + mc.player.getEyeHeight(mc.player.getPose());
        double posZ = MathHelper.lerp(tickDelta, mc.player.lastRenderZ, mc.player.getZ());

        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        double motionX = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad);
        double motionY = -MathHelper.sin(pitchRad);
        double motionZ = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad);

        double length = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX /= length;
        motionY /= length;
        motionZ /= length;

        float power = 1.5f;
        float gravity = 0.03f;
        float drag = 0.99f;

        if (item instanceof BowItem) {
            int useTime = mc.player.getItemUseTime();
            float pull = BowItem.getPullProgress(useTime);
            if (pull <= 0.1f) pull = 1.0f;
            power = pull * 3.0f;
            gravity = 0.05f;
        } else if (item instanceof CrossbowItem) {
            power = 3.15f;
            gravity = 0.05f;
        } else if (item instanceof TridentItem) {
            power = 2.5f;
            gravity = 0.05f;
        } else if (item instanceof PotionItem) {
            power = 0.5f;
            gravity = 0.05f;
            motionY += 0.1;
        } else if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem) {
            power = 1.5f;
            gravity = 0.03f;
        }

        motionX *= power;
        motionY *= power;
        motionZ *= power;

        List<Vec3d> path = new ArrayList<>();
        Vec3d currentPos = new Vec3d(posX, posY, posZ);
        path.add(currentPos);

        HitResult hitResult = null;

        for (int step = 0; step < 120; step++) {
            Vec3d nextPos = currentPos.add(motionX, motionY, motionZ);

            RaycastContext context = new RaycastContext(currentPos, nextPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult blockHit = mc.world.raycast(context);

            if (blockHit.getType() != HitResult.Type.MISS) {
                hitResult = blockHit;
                path.add(blockHit.getPos());
                break;
            }

            Box box = new Box(currentPos, nextPos).expand(1.0);
            EntityHitResult entityHit = ProjectileUtil.raycast(mc.player, currentPos, nextPos, box, entity -> !entity.isSpectator() && entity.isAlive() && entity != mc.player, 0.5);

            if (entityHit != null && entityHit.getType() != HitResult.Type.MISS) {
                hitResult = entityHit;
                path.add(entityHit.getPos());
                break;
            }

            path.add(nextPos);
            currentPos = nextPos;

            motionX *= drag;
            motionY *= drag;
            motionZ *= drag;
            motionY -= gravity;
        }

        Color c = customColor.isEnabled() ? color.getColor() : ThemeManager.getInstance().getAccentColor();
        renderTrajectoryLine(matrices, path, c);

        if (showHitBox.isEnabled() && hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            Vec3d hitPos = hitResult.getPos();
            renderHitMarker(matrices, hitPos, c);
        }
    }

    private void renderTrajectoryLine(MatrixStack matrices, List<Vec3d> path, Color c) {
        if (path.size() < 2 || mc.gameRenderer == null) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float lw = lineWidth.getFloatValue();

        for (int i = 0; i < path.size() - 1; i++) {
            Vec3d p1 = path.get(i).subtract(cam);
            Vec3d p2 = path.get(i + 1).subtract(cam);
            float alpha = 1.0f - ((float) i / (float) path.size()) * 0.4f;
            Color segColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * alpha));
            RenderUtils.drawLine3D(matrices, p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, segColor, lw);
        }
    }

    private void renderHitMarker(MatrixStack matrices, Vec3d hitPos, Color c) {
        if (mc.gameRenderer == null) return;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double hx = hitPos.x - cam.x;
        double hy = hitPos.y - cam.y;
        double hz = hitPos.z - cam.z;
        double s = 0.25;

        RenderUtils.drawBox3D(matrices, hx - s, hy + 0.01, hz - s, hx + s, hy + 0.05, hz + s, c, 2.0f);
        RenderUtils.drawLine3D(matrices, hx - s, hy + 0.02, hz - s, hx + s, hy + 0.02, hz + s, c, 1.5f);
        RenderUtils.drawLine3D(matrices, hx - s, hy + 0.02, hz + s, hx + s, hy + 0.02, hz - s, c, 1.5f);
    }

    private boolean isThrowable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof EnderPearlItem || item instanceof BowItem || item instanceof CrossbowItem ||
               item instanceof TridentItem || item instanceof PotionItem ||
               item instanceof SnowballItem || item instanceof EggItem;
    }
}
