package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Particles extends Module {

    private final BooleanSetting damageNumbers = addSetting(new BooleanSetting("DamageNumbers", true));
    private final BooleanSetting hearts = addSetting(new BooleanSetting("Hearts", true));
    private final ColorSetting color = addSetting(new ColorSetting("Color", Color.RED));
    private final NumberSetting size = addSetting(new NumberSetting("Size", 1.0, 0.5, 3.0, 0.1));
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Default", "Default", "Crit", "Hearts"));

    private final List<HitParticle> particles = new ArrayList<>();

    public Particles() {
        super("Particles", "Damage numbers and hit particles", Category.VISUAL);
    }

    public void spawnHitParticles(Vec3d pos) {
        if (!isEnabled()) return;
        for (int i = 0; i < 6; i++) {
            double vx = (Math.random() - 0.5) * 0.2;
            double vy = Math.random() * 0.15;
            double vz = (Math.random() - 0.5) * 0.2;
            particles.add(new HitParticle(pos, new Vec3d(vx, vy, vz), color.getColor()));
        }
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();
        particles.removeIf(p -> now - p.spawnTime > 800);
        for (HitParticle p : particles) {
            p.pos = p.pos.add(p.velocity);
            p.velocity = p.velocity.multiply(0.95).subtract(0, 0.005, 0);
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (particles.isEmpty() || mc.player == null) return;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();

        for (HitParticle p : particles) {
            Vec3d rPos = p.pos.subtract(cam);
            long age = System.currentTimeMillis() - p.spawnTime;
            float alpha = 1.0f - (age / 800.0f);
            int a = (int)(alpha * 255);
            Color c = new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), Math.max(0, Math.min(255, a)));

            RenderUtils.drawCircle3D(matrices, rPos.x, rPos.y, rPos.z, 0.08f * size.getFloatValue(), c, 2.0f);
        }
    }

    private static class HitParticle {
        Vec3d pos;
        Vec3d velocity;
        Color color;
        long spawnTime;

        HitParticle(Vec3d pos, Vec3d velocity, Color color) {
            this.pos = pos;
            this.velocity = velocity;
            this.color = color;
            this.spawnTime = System.currentTimeMillis();
        }
    }
}
