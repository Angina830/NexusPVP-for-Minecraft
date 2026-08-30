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
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
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
import java.util.Iterator;
import java.util.List;

public class TrajectoryPreview extends Module {

    private final BooleanSetting inFlightTracking = addSetting(new BooleanSetting("InFlightTrack", true));
    private final BooleanSetting customColor = addSetting(new BooleanSetting("CustomColor", false));
    private final ColorSetting color = addSetting(new ColorSetting("Color", new Color(0, 220, 255)));
    private final NumberSetting lineWidth = addSetting(new NumberSetting("LineWidth", 2.5, 1.0, 5.0, 0.5));
    private final BooleanSetting showHitBox = addSetting(new BooleanSetting("HitBox", true));

    public static class FlyingProjectile {
        public final int entityId;
        public final List<Vec3d> fullPath;
        public final HitResult hitResult;
        public final Color color;
        public long spawnTime;

        public FlyingProjectile(int entityId, List<Vec3d> fullPath, HitResult hitResult, Color color, long spawnTime) {
            this.entityId = entityId;
            this.fullPath = fullPath;
            this.hitResult = hitResult;
            this.color = color;
            this.spawnTime = spawnTime;
        }
    }

    private final List<FlyingProjectile> flyingProjectiles = new ArrayList<>();

    public TrajectoryPreview() {
        super("TrajectoryPreview", "Predicts ballistic trajectory and erases path dynamically as pearl/projectile flies", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();

        // Track newly spawned pearls/projectiles thrown by player
        if (inFlightTracking.isEnabled()) {
            for (Entity entity : mc.world.getEntities()) {
                if (isPlayerProjectile(entity)) {
                    int id = entity.getEntityId();
                    boolean exists = false;
                    for (FlyingProjectile fp : flyingProjectiles) {
                        if (fp.entityId == id) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        // Calculate full simulated path starting from projectile's initial position
                        Color c = customColor.isEnabled() ? color.getColor() : ThemeManager.getInstance().getAccentColor();
                        SimulatedResult sim = simulateProjectile(entity);
                        if (sim != null && sim.path.size() >= 2) {
                            flyingProjectiles.add(new FlyingProjectile(id, sim.path, sim.hitResult, c, now));
                        }
                    }
                }
            }
        }

        // Clean up dead/impacted projectiles
        Iterator<FlyingProjectile> it = flyingProjectiles.iterator();
        while (it.hasNext()) {
            FlyingProjectile fp = it.next();
            Entity ent = mc.world.getEntityById(fp.entityId);
            if (ent == null || !ent.isAlive() || ent.isRemoved() || (now - fp.spawnTime > 15000)) {
                it.remove();
            }
        }
    }

    private boolean isPlayerProjectile(Entity entity) {
        if (entity instanceof EnderPearlEntity || entity instanceof SnowballEntity ||
            entity instanceof PotionEntity || entity instanceof ExperienceBottleEntity ||
            entity instanceof ArrowEntity || entity instanceof TridentEntity) {
            
            // Check distance to player when first detected (< 5 blocks)
            return entity.squaredDistanceTo(mc.player) <= 36.0;
        }
        return false;
    }

    private static class SimulatedResult {
        final List<Vec3d> path;
        final HitResult hitResult;

        SimulatedResult(List<Vec3d> path, HitResult hitResult) {
            this.path = path;
            this.hitResult = hitResult;
        }
    }

    private SimulatedResult simulateProjectile(Entity entity) {
        Vec3d currentPos = entity.getPos();
        Vec3d velocity = entity.getVelocity();

        float gravity = 0.03f;
        float drag = 0.99f;

        if (entity instanceof ArrowEntity || entity instanceof TridentEntity || entity instanceof PotionEntity) {
            gravity = 0.05f;
        }

        double motionX = velocity.x;
        double motionY = velocity.y;
        double motionZ = velocity.z;

        List<Vec3d> path = new ArrayList<>();
        path.add(currentPos);
        HitResult hitResult = null;

        for (int step = 0; step < 160; step++) {
            Vec3d nextPos = currentPos.add(motionX, motionY, motionZ);

            RaycastContext context = new RaycastContext(currentPos, nextPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
            BlockHitResult blockHit = mc.world.raycast(context);

            if (blockHit.getType() != HitResult.Type.MISS) {
                hitResult = blockHit;
                path.add(blockHit.getPos());
                break;
            }

            Box box = new Box(currentPos, nextPos).expand(1.0);
            EntityHitResult entityHit = ProjectileUtil.raycast(entity, currentPos, nextPos, box, e -> !e.isSpectator() && e.isAlive() && e != mc.player, 0.5);

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

        return new SimulatedResult(path, hitResult);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null) return;

        // 1. Render in-flight projectiles with dynamic path erasure (only remaining path in front of projectile)
        if (inFlightTracking.isEnabled() && !flyingProjectiles.isEmpty()) {
            for (FlyingProjectile fp : flyingProjectiles) {
                Entity ent = mc.world.getEntityById(fp.entityId);
                if (ent != null && ent.isAlive() && !ent.isRemoved()) {
                    Vec3d pearlPos = new Vec3d(
                        MathHelper.lerp(tickDelta, ent.prevX, ent.getX()),
                        MathHelper.lerp(tickDelta, ent.prevY, ent.getY()),
                        MathHelper.lerp(tickDelta, ent.prevZ, ent.getZ())
                    );

                    // Find closest point index in full path
                    int closestIdx = 0;
                    double minDist = Double.MAX_VALUE;
                    for (int i = 0; i < fp.fullPath.size(); i++) {
                        double d = fp.fullPath.get(i).squaredDistanceTo(pearlPos);
                        if (d < minDist) {
                            minDist = d;
                            closestIdx = i;
                        }
                    }

                    // Slice remaining path in front of the flying pearl
                    List<Vec3d> remainingPath = new ArrayList<>();
                    remainingPath.add(pearlPos);
                    for (int i = closestIdx + 1; i < fp.fullPath.size(); i++) {
                        remainingPath.add(fp.fullPath.get(i));
                    }

                    renderTrajectoryLine(remainingPath, fp.color);

                    if (showHitBox.isEnabled() && fp.hitResult != null && fp.hitResult.getType() != HitResult.Type.MISS) {
                        renderHitMarker(fp.hitResult.getPos(), fp.color);
                    }
                }
            }
        }

        // 2. Render held item preview (pre-throw)
        ItemStack stack = mc.player.getMainHandStack();
        if (!isThrowable(stack)) {
            stack = mc.player.getOffHandStack();
            if (!isThrowable(stack)) return;
        }

        Item item = stack.getItem();

        float pitch = mc.player.pitch;
        float yaw = mc.player.yaw;

        double posX = MathHelper.lerp(tickDelta, mc.player.prevX, mc.player.getX());
        double posY = MathHelper.lerp(tickDelta, mc.player.prevY, mc.player.getY()) + mc.player.getEyeHeight(mc.player.getPose());
        double posZ = MathHelper.lerp(tickDelta, mc.player.prevZ, mc.player.getZ());

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
        } else if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
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

        for (int step = 0; step < 140; step++) {
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
        renderTrajectoryLine(path, c);

        if (showHitBox.isEnabled() && hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            Vec3d hitPos = hitResult.getPos();
            renderHitMarker(hitPos, c);
        }
    }

    private void renderTrajectoryLine(List<Vec3d> path, Color c) {
        if (path.size() < 2) return;

        RenderUtils.setupBloom3D();
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
            float alpha = 1.0f - ((float) i / (float) path.size()) * 0.35f;
            buffer.vertex(point.x, point.y, point.z).color(r, g, b, alpha).next();
        }
        tessellator.draw();

        RenderSystem.popMatrix();
        RenderUtils.cleanupBloom3D();
    }

    private void renderHitMarker(Vec3d hitPos, Color c) {
        RenderUtils.setupBloom3D();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();

        RenderSystem.pushMatrix();
        RenderSystem.translated(hitPos.x - cam.x, hitPos.y - cam.y, hitPos.z - cam.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float r = c.getRed() / 255.0f;
        float g = c.getGreen() / 255.0f;
        float b = c.getBlue() / 255.0f;

        double s = 0.25;
        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        // Bottom outline
        buffer.vertex(-s, 0.02, -s).color(r, g, b, 1.0f).next();
        buffer.vertex(s, 0.02, -s).color(r, g, b, 1.0f).next();
        buffer.vertex(s, 0.02, -s).color(r, g, b, 1.0f).next();
        buffer.vertex(s, 0.02, s).color(r, g, b, 1.0f).next();
        buffer.vertex(s, 0.02, s).color(r, g, b, 1.0f).next();
        buffer.vertex(-s, 0.02, s).color(r, g, b, 1.0f).next();
        buffer.vertex(-s, 0.02, s).color(r, g, b, 1.0f).next();
        buffer.vertex(-s, 0.02, -s).color(r, g, b, 1.0f).next();
        // Cross marker
        buffer.vertex(-s, 0.02, -s).color(r, g, b, 0.8f).next();
        buffer.vertex(s, 0.02, s).color(r, g, b, 0.8f).next();
        buffer.vertex(-s, 0.02, s).color(r, g, b, 0.8f).next();
        buffer.vertex(s, 0.02, -s).color(r, g, b, 0.8f).next();
        tessellator.draw();

        RenderSystem.popMatrix();
        RenderUtils.cleanupBloom3D();
    }

    private boolean isThrowable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof EnderPearlItem || item instanceof BowItem || item instanceof CrossbowItem ||
               item instanceof TridentItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem ||
               item instanceof SnowballItem || item instanceof EggItem;
    }
}
