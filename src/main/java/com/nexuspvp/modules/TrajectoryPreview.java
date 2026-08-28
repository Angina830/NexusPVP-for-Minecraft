package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.opengl.GL11;

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

        // 1. Calculate Initial Parameters (Velocity, Gravity, Drag)
        float pitch = mc.player.pitch;
        float yaw = mc.player.yaw;

        double posX = MathHelper.lerp(tickDelta, mc.player.prevX, mc.player.getX());
        double posY = MathHelper.lerp(tickDelta, mc.player.prevY, mc.player.getY()) + mc.player.getEyeHeight(mc.player.getPose());
        double posZ = MathHelper.lerp(tickDelta, mc.player.prevZ, mc.player.getZ());

        // Direction Vector
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        double motionX = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad);
        double motionY = -MathHelper.sin(pitchRad);
        double motionZ = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad);

        // Normalize
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
            if (pull <= 0.1f) pull = 1.0f; // Default preview to full pull
            power = pull * 3.0f;
            gravity = 0.05f;
        } else if (item instanceof CrossbowItem) {
            power = 3.15f;
            gravity = 0.05f;
        } else if (item instanceof TridentItem) {
            power = 2.5f;
            gravity = 0.05f;
        } else if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
            power = 0.5f;
            gravity = 0.05f;
            motionY += 0.1; // Toss offset
        } else if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem) {
            power = 1.5f;
            gravity = 0.03f;
        }

        motionX *= power;
        motionY *= power;
        motionZ *= power;

        // 2. Trace Path using Step Simulation & Raycasting
        List<Vec3d> path = new ArrayList<>();
        Vec3d currentPos = new Vec3d(posX, posY, posZ);
        path.add(currentPos);

        HitResult hitResult = null;

        for (int step = 0; step < 120; step++) {
            Vec3d nextPos = currentPos.add(motionX, motionY, motionZ);

            // Block Raycast
            RaycastContext context = new RaycastContext(currentPos, nextPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult blockHit = mc.world.raycast(context);

            if (blockHit.getType() != HitResult.Type.MISS) {
                hitResult = blockHit;
                path.add(blockHit.getPos());
                break;
            }

            // Entity Raycast
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

        // 3. Render 3D Trajectory Path
        Color c = customColor.isEnabled() ? color.getColor() : ThemeManager.getInstance().getAccentColor();
        renderTrajectoryLine(path, c);

        // 4. Render Target Hit Marker
        if (showHitBox.isEnabled() && hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            Vec3d hitPos = hitResult.getPos();
            renderHitMarker(hitPos, c);
        }
    }

    private void renderTrajectoryLine(List<Vec3d> path, Color c) {
        if (path.size() < 2) return;

        RenderUtils.setup3D();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();

        RenderSystem.pushMatrix();
        RenderSystem.translated(-cam.x, -cam.y, -cam.z);
        GL11.glLineWidth(lineWidth.getFloatValue());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float r = c.getRed() / 255.0f;
        float g = c.getGreen() / 255.0f;
        float b = c.getBlue() / 255.0f;

        buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < path.size(); i++) {
            Vec3d point = path.get(i);
            float alpha = 1.0f - ((float) i / (float) path.size()) * 0.4f; // Soft fade
            buffer.vertex(point.x, point.y, point.z).color(r, g, b, alpha).next();
        }
        tessellator.draw();

        RenderSystem.popMatrix();
        RenderUtils.cleanup3D();
    }

    private void renderHitMarker(Vec3d hitPos, Color c) {
        RenderUtils.setup3D();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();

        RenderSystem.pushMatrix();
        RenderSystem.translated(hitPos.x - cam.x, hitPos.y - cam.y, hitPos.z - cam.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float r = c.getRed() / 255.0f;
        float g = c.getGreen() / 255.0f;
        float b = c.getBlue() / 255.0f;

        // Draw glowing mini landing box (0.3 x 0.3)
        double s = 0.2;
        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        // Bottom
        buffer.vertex(-s, 0.02, -s).color(r, g, b, 1.0f).next();
        buffer.vertex(s, 0.02, -s).color(r, g, b, 1.0f).next();
        buffer.vertex(s, 0.02, -s).color(r, g, b, 1.0f).next();
        buffer.vertex(s, 0.02, s).color(r, g, b, 1.0f).next();
        buffer.vertex(s, 0.02, s).color(r, g, b, 1.0f).next();
        buffer.vertex(-s, 0.02, s).color(r, g, b, 1.0f).next();
        buffer.vertex(-s, 0.02, s).color(r, g, b, 1.0f).next();
        buffer.vertex(-s, 0.02, -s).color(r, g, b, 1.0f).next();
        // Top cross
        buffer.vertex(-s, 0.02, -s).color(r, g, b, 0.8f).next();
        buffer.vertex(s, 0.02, s).color(r, g, b, 0.8f).next();
        buffer.vertex(-s, 0.02, s).color(r, g, b, 0.8f).next();
        buffer.vertex(s, 0.02, -s).color(r, g, b, 0.8f).next();
        tessellator.draw();

        RenderSystem.popMatrix();
        RenderUtils.cleanup3D();
    }

    private boolean isThrowable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof EnderPearlItem || item instanceof BowItem || item instanceof CrossbowItem ||
               item instanceof TridentItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem ||
               item instanceof SnowballItem || item instanceof EggItem;
    }
}