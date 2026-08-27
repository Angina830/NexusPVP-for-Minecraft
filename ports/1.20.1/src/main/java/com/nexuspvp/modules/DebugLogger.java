package com.nexuspvp.modules;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class DebugLogger extends Module {

    private final BooleanSetting showHUD = addSetting(new BooleanSetting("ShowHUD", true));
    private final BooleanSetting targetInfo = addSetting(new BooleanSetting("TargetInfo", true));
    private final BooleanSetting renderStats = addSetting(new BooleanSetting("RenderStats", true));
    private final BooleanSetting systemInfo = addSetting(new BooleanSetting("SystemInfo", true));
    private final NumberSetting posX = addSetting(new NumberSetting("PosX", 10.0, 0.0, 1000.0, 5.0));
    private final NumberSetting posY = addSetting(new NumberSetting("PosY", 60.0, 0.0, 1000.0, 5.0));

    public DebugLogger() {
        super("DebugLogger", "Realtime In-Game Diagnostics, Entity Inspector & Pipeline Monitor", Category.MISC);
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (!showHUD.isEnabled() || mc.player == null || mc.world == null) return;

        List<String> lines = new ArrayList<>();
        int accent = ThemeManager.getInstance().getAccentColor().getRGB();

        lines.add("§l[NexusPVP Diagnostic Sentinel]");

        // 1. Target & Entity Inspection
        if (targetInfo.isEnabled()) {
            LivingEntity target = getRaycastTarget(30.0);
            if (target != null) {
                float hp = target.getHealth();
                float maxHp = target.getMaxHealth();
                double dist = Math.sqrt(target.squaredDistanceTo(mc.player));
                lines.add(String.format("§bTarget: §f%s §7(ID: %d)", target.getName().getString(), target.getId()));
                lines.add(String.format("§bHP: §a%.1f / %.1f §7| Dist: §e%.1fm", hp, maxHp, dist));
                lines.add(String.format("§bTarget Pos: §7[%.1f, %.1f, %.1f]", target.getX(), target.getY(), target.getZ()));
            } else {
                lines.add("§bTarget: §7<None in crosshair>");
            }
        }

        // 2. Render Pipeline & OverheadHealth Status
        if (renderStats.isEnabled()) {
            OverheadHealth oh = NexusPVP.getInstance().getModuleManager().getModule(OverheadHealth.class);
            boolean ohActive = oh != null && oh.isEnabled();
            int trackedCount = oh != null ? oh.getTrackedEntitiesCount() : 0;
            lines.add(String.format("§dOverheadHealth: %s §7| Tracked: §f%d entities", (ohActive ? "§aACTIVE" : "§cDISABLED"), trackedCount));
            
            int livingInWorld = 0;
            for (Entity e : mc.world.getEntities()) {
                if (e instanceof LivingEntity && e != mc.player) livingInWorld++;
            }
            lines.add(String.format("§dWorld Entities: §f%d §7(Living: §e%d§7)", mc.world.getRegularEntityCount(), livingInWorld));
        }

        // 3. System & Position Stats
        if (systemInfo.isEnabled()) {
            long maxMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
            long totalMem = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            long freeMem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
            long usedMem = totalMem - freeMem;

            lines.add(String.format("§6Memory: §f%dMB / %dMB §7| §6FPS: §f%d", usedMem, maxMem, MinecraftClient.getInstance().getCurrentFps()));
            lines.add(String.format("§6Player: §f[%.1f, %.1f, %.1f] §7(Yaw: §e%.1f§7, Pitch: §e%.1f§7)", 
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch()));
        }

        // Draw sleek diagnostic container
        int x = posX.getValue().intValue();
        int y = posY.getValue().intValue();
        int maxW = 0;
        for (String s : lines) {
            maxW = Math.max(maxW, mc.textRenderer.getWidth(s));
        }
        int pad = 6;
        int boxW = maxW + pad * 2;
        int boxH = lines.size() * 11 + pad * 2;

        RenderUtils.drawRoundedRect(matrices, x - 1, y - 1, boxW + 2, boxH + 2, 4, 0xEE5865F2);
        RenderUtils.drawRoundedRect(matrices, x, y, boxW, boxH, 3, 0xDD1E1F22);

        int lineY = y + pad;
        for (String s : lines) {
            Compat.drawWithShadow(null, matrices, s, x + pad, lineY, 0xFFFFFFFF);
            lineY += 11;
        }
    }

    private LivingEntity getRaycastTarget(double maxDist) {
        if (mc.player == null || mc.world == null) return null;
        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
        Vec3d rotVec = mc.player.getRotationVec(1.0f);
        Vec3d reachVec = eyePos.add(rotVec.x * maxDist, rotVec.y * maxDist, rotVec.z * maxDist);
        Box box = mc.player.getBoundingBox().stretch(rotVec.multiply(maxDist)).expand(1.0D, 1.0D, 1.0D);

        EntityHitResult hit = ProjectileUtil.raycast(mc.player, eyePos, reachVec, box, entity -> entity instanceof LivingEntity && entity != mc.player && entity.isAlive(), maxDist * maxDist);
        if (hit != null && hit.getEntity() instanceof LivingEntity) {
            return (LivingEntity) hit.getEntity();
        }
        return null;
    }
}
