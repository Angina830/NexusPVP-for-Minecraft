package com.nexuspvp.modules;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class DebugLogger extends Module {

    private final BooleanSetting showHUD = addSetting(new BooleanSetting("ShowHUD", true));
    private final BooleanSetting moduleProfiler = addSetting(new BooleanSetting("ModuleProfiler", true));
    private final BooleanSetting aimDiagnostics = addSetting(new BooleanSetting("AimDiagnostics", true));
    private final BooleanSetting stutterDetector = addSetting(new BooleanSetting("StutterDetector", true));
    private final BooleanSetting fileLogging = addSetting(new BooleanSetting("FileLogging", false));
    private final BooleanSetting logSpikesOnly = addSetting(new BooleanSetting("LogSpikesOnly", true));
    private final NumberSetting posX = addSetting(new NumberSetting("PosX", 10.0, 0.0, 1000.0, 5.0));
    private final NumberSetting posY = addSetting(new NumberSetting("PosY", 50.0, 0.0, 1000.0, 5.0));

    private static File logFile = null;
    private long lastLogTime = 0;

    // Frame timing & Stutter statistics
    private final long[] frameTimesNanos = new long[120];
    private int frameIndex = 0;
    private long lastFrameNanos = System.nanoTime();
    private float currentFps = 60.0f;
    private float frameTimeMs = 16.6f;
    private float low1PercentFps = 60.0f;
    private float low01PercentFps = 60.0f;
    private int stutterCount = 0;
    private long lastStutterAlertTime = 0;

    // Aim / Target FPS Impact tracking
    private float idleAvgFrameTimeMs = 16.6f;
    private float aimAvgFrameTimeMs = 16.6f;
    private boolean currentlyAiming = false;
    private String lastTargetName = "None";

    public DebugLogger() {
        super("DebugLogger", "Realtime FPS, Stutter Detector & Module Render Profiler", Category.MISC);
        initLogFile();
    }

    private static void initLogFile() {
        try {
            File runDir = MinecraftClient.getInstance().runDirectory;
            File logDir = new File(runDir, "logs");
            if (!logDir.exists()) logDir.mkdirs();
            logFile = new File(logDir, "nexus_debug.log");
        } catch (Exception ignored) {}
    }

    public static synchronized void log(String tag, String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        String entry = String.format("[%s] [%s] %s", timestamp, tag, message);
        System.out.println("[NexusPVP-Profiler] " + entry);
        if (logFile != null) {
            try (FileWriter fw = new FileWriter(logFile, true); PrintWriter pw = new PrintWriter(fw)) {
                pw.println(entry);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onTick() {
        if (!fileLogging.isEnabled() || mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now - lastLogTime > 4000 && !logSpikesOnly.isEnabled()) {
            lastLogTime = now;
            log("STATS", String.format("FPS: %.1f | 1%% Low: %.1f | Frame: %.2fms | Stutters: %d | Aiming: %b",
                    currentFps, low1PercentFps, frameTimeMs, stutterCount, currentlyAiming));
        }
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        long nowNanos = System.nanoTime();
        long deltaNanos = nowNanos - lastFrameNanos;
        lastFrameNanos = nowNanos;

        if (deltaNanos > 0) {
            frameTimesNanos[frameIndex % frameTimesNanos.length] = deltaNanos;
            frameIndex++;
            frameTimeMs = deltaNanos / 1_000_000.0f;
            currentFps = 1000.0f / Math.max(0.1f, frameTimeMs);
        }

        // Calculate 1% low and 0.1% low every 15 frames
        if (frameIndex % 15 == 0) {
            calculatePercentiles();
        }

        // Check aim target (Zero-raycast: uses vanilla precomputed hit result)
        LivingEntity target = getVanillaCrosshairTarget();
        currentlyAiming = (target != null);

        if (currentlyAiming) {
            lastTargetName = target.getName().getString();
            aimAvgFrameTimeMs = aimAvgFrameTimeMs * 0.94f + frameTimeMs * 0.06f;
        } else {
            idleAvgFrameTimeMs = idleAvgFrameTimeMs * 0.94f + frameTimeMs * 0.06f;
        }

        // Detect micro-stutter / lag spike (> 1.6x average or > 30ms)
        float avgMs = (idleAvgFrameTimeMs + aimAvgFrameTimeMs) / 2.0f;
        if (frameTimeMs > Math.max(28.0f, avgMs * 1.65f)) {
            stutterCount++;
            long nowMs = System.currentTimeMillis();
            if (nowMs - lastStutterAlertTime > 1500) {
                lastStutterAlertTime = nowMs;
                if (fileLogging.isEnabled()) {
                    Module topMod = getHeaviestModule();
                    String topModInfo = (topMod != null) ? String.format("%s (%.1fµs)", topMod.getName(), topMod.getAvgRenderMicros()) : "None";
                    log("STUTTER", String.format("Frame spike: %.1fms (Aiming at %s | Heaviest: %s)",
                            frameTimeMs, (currentlyAiming ? lastTargetName : "Idle"), topModInfo));
                }
            }
        }

        if (!showHUD.isEnabled() || mc.player == null || mc.world == null) return;

        // Build Diagnostic HUD
        List<String> lines = new ArrayList<>();
        lines.add("§d§l[NexusPVP Performance Sentinel]");

        // 1. FPS & Frametime Section
        lines.add(String.format("§fFPS: §a%.0f §7| §fFrame: §e%.1f ms §7| §f1%% Low: §c%.0f §7| §f0.1%% Low: §4%.0f",
                currentFps, frameTimeMs, low1PercentFps, low01PercentFps));

        if (stutterDetector.isEnabled()) {
            String alert = (System.currentTimeMillis() - lastStutterAlertTime < 1500) ? " §c§l[LAG SPIKE!]" : "";
            lines.add(String.format("§fMicro-stutters: §e%d spikes detected%s", stutterCount, alert));
        }

        // 2. Aim / Target Hover Diagnostics
        if (aimDiagnostics.isEnabled()) {
            float aimFps = 1000.0f / Math.max(0.1f, aimAvgFrameTimeMs);
            float idleFps = 1000.0f / Math.max(0.1f, idleAvgFrameTimeMs);
            float fpsDrop = idleFps - aimFps;
            float msDrop = aimAvgFrameTimeMs - idleAvgFrameTimeMs;

            if (currentlyAiming && target != null) {
                float hp = target.getHealth();
                float maxHp = target.getMaxHealth();
                double dist = Math.sqrt(target.squaredDistanceTo(mc.player));
                lines.add(String.format("§b🎯 Aim Target: §f%s §7(Dist: §e%.1fm§7, HP: §a%.1f/%.0f§7)",
                        target.getName().getString(), dist, hp, maxHp));
            } else {
                lines.add("§b🎯 Aim Target: §7<No entity in crosshair>");
            }

            String dropColor = fpsDrop > 5.0f ? "§c" : (fpsDrop > 1.0f ? "§e" : "§a");
            lines.add(String.format("§b📉 Aim Impact: %s%+.1f FPS §7(%+.2f ms/frame vs idle)",
                    dropColor, -fpsDrop, msDrop));
        }

        // 3. Per-Module Profiler Top List
        if (moduleProfiler.isEnabled()) {
            lines.add("§6⚡ TOP MODULE RENDER TIMES:");
            List<Module> enabledMods = NexusPVP.getInstance().getModuleManager().getEnabledModules();
            enabledMods.sort((m1, m2) -> Float.compare(m2.getAvgRenderMicros(), m1.getAvgRenderMicros()));

            float totalModMicros = 0;
            int count = 0;
            for (Module m : enabledMods) {
                totalModMicros += m.getAvgRenderMicros();
                if (count < 4 && m.getAvgRenderMicros() > 0.5f) {
                    lines.add(String.format("  §7%d. §f%-16s §e%6.1f µs §7(Peak: §c%.0f µs§7)",
                            count + 1, m.getName(), m.getAvgRenderMicros(), m.getPeakRenderMicros()));
                    count++;
                }
            }
            float totalModMs = totalModMicros / 1000.0f;
            float frameBudgetPct = (frameTimeMs > 0) ? (totalModMs / frameTimeMs) * 100.0f : 0;
            lines.add(String.format("§6⚡ Total Mod Overhead: §f%.2f ms §7(§e%.1f%% §7of frame)", totalModMs, frameBudgetPct));
        }

        // Render Background Card
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
            Compat.drawWithShadow(mc.textRenderer, matrices, s, x + pad, lineY, 0xFFFFFFFF);
            lineY += 11;
        }
    }

    private void calculatePercentiles() {
        long[] copy = frameTimesNanos.clone();
        Arrays.sort(copy);
        int valid = 0;
        for (long l : copy) if (l > 0) valid++;
        if (valid < 10) return;

        int p99Idx = (int) (valid * 0.99);
        int p999Idx = (int) (valid * 0.999);
        p99Idx = Math.min(valid - 1, Math.max(0, p99Idx));
        p999Idx = Math.min(valid - 1, Math.max(0, p999Idx));

        float p99Ms = copy[p99Idx] / 1_000_000.0f;
        float p999Ms = copy[p999Idx] / 1_000_000.0f;

        low1PercentFps = 1000.0f / Math.max(0.1f, p99Ms);
        low01PercentFps = 1000.0f / Math.max(0.1f, p999Ms);
    }

    private LivingEntity getVanillaCrosshairTarget() {
        if (mc.targetedEntity instanceof LivingEntity && mc.targetedEntity != mc.player) {
            return (LivingEntity) mc.targetedEntity;
        }
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity ent = ((EntityHitResult) mc.crosshairTarget).getEntity();
            if (ent instanceof LivingEntity && ent != mc.player) {
                return (LivingEntity) ent;
            }
        }
        return null;
    }

    private Module getHeaviestModule() {
        List<Module> list = NexusPVP.getInstance().getModuleManager().getEnabledModules();
        Module best = null;
        for (Module m : list) {
            if (best == null || m.getAvgRenderMicros() > best.getAvgRenderMicros()) {
                best = m;
            }
        }
        return best;
    }
}
