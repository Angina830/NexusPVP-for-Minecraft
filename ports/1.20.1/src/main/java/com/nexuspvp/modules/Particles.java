package com.nexuspvp.modules;

import com.nexuspvp.module.Module;
import com.nexuspvp.module.Category;
import com.nexuspvp.setting.*;
import com.nexuspvp.util.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class Particles extends Module {
    private final BooleanSetting damageNumbers = addSetting(new BooleanSetting("DamageNumbers", true));
    private final BooleanSetting hearts = addSetting(new BooleanSetting("Hearts", true));
    private final ColorSetting color = addSetting(new ColorSetting("Color", Color.RED));
    private final NumberSetting size = addSetting(new NumberSetting("Size", 1.0, 0.5, 3.0, 0.1));
    private final ModeSetting style = addSetting(new ModeSetting("Style", "Default", "Default", "Crit", "Hearts"));

    private final List<DamageParticle> particles = new ArrayList<>();
    private Entity lastTarget = null;
    private float lastHealth = -1;

    public Particles() {
        super("Particles", "Beautiful particles on entity hit", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        Entity target = null;
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            target = ((EntityHitResult) mc.crosshairTarget).getEntity();
        }

        if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) target;
            float totalHealth = living.getHealth() + living.getAbsorptionAmount();
            if (lastTarget == living) {
                if (totalHealth < lastHealth) {
                    float damage = lastHealth - totalHealth;
                    Vec3d offset = new Vec3d(
                            (Math.random() - 0.5) * 1.5,
                            living.getStandingEyeHeight() + (Math.random() - 0.5) * 0.4,
                            (Math.random() - 0.5) * 1.5
                    );
                    if (damageNumbers.isEnabled()) {
                        particles.add(new DamageParticle(living, offset, String.format("%.1f", damage), 40));
                    }
                    if (hearts.isEnabled()) {
                        particles.add(new DamageParticle(
                                living, 
                                offset.add((Math.random() - 0.5) * 0.8, (Math.random() - 0.5) * 0.8, (Math.random() - 0.5) * 0.8),
                                "\u2764", 30));
                    }
                }
            }
            lastHealth = totalHealth;
            lastTarget = living;
        } else {
            lastTarget = null;
        }

        Iterator<DamageParticle> it = particles.iterator();
        while (it.hasNext()) {
            DamageParticle p = it.next();
            p.tick();
            if (p.life <= 0 || p.entity == null || !p.entity.isAlive()) {
                it.remove();
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack ignored, float tickDelta) {
        if (mc.player == null || mc.world == null || particles.isEmpty()) return;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        RenderSystem.pushMatrix();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        for (DamageParticle p : particles) {
            float alpha = (float) p.life / p.maxLife;
            Color c = color.getColor();
            if (p.text.equals("\u2764")) {
                c = new Color(255, 50, 50);
            }
            c = ColorUtils.withAlpha(c, (int) (alpha * 255));
            
            double ex = p.entity.prevX + (p.entity.getX() - p.entity.prevX) * tickDelta;
            double ey = p.entity.prevY + (p.entity.getY() - p.entity.prevY) * tickDelta;
            double ez = p.entity.prevZ + (p.entity.getZ() - p.entity.prevZ) * tickDelta;
            Vec3d renderPos = new Vec3d(ex, ey, ez).add(p.offset);

            MatrixStack m = new MatrixStack();
            m.translate(renderPos.x - camPos.x, renderPos.y - camPos.y, renderPos.z - camPos.z);
            m.multiply(camera.getRotation());
            float scale = 0.025f * size.getFloatValue();
            m.scale(-scale, -scale, scale);

            int textWidth = mc.textRenderer.getWidth(p.text);
            
            // Use our own immediate builder so we don't interfere with entity rendering
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            Matrix4f matrix = m.peek().getModel();
            
            mc.textRenderer.draw(p.text, -textWidth / 2.0f, 0, c.getRGB(), true, matrix, immediate, true, 0, 15728880);
            immediate.draw();
        }
        
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.popMatrix();
    }

    private static class DamageParticle {
        Entity entity;
        Vec3d offset;
        String text;
        int maxLife;
        int life;

        DamageParticle(Entity entity, Vec3d offset, String text, int life) {
            this.entity = entity;
            this.offset = offset;
            this.text = text;
            this.maxLife = life;
            this.life = life;
        }

        void tick() {
            life--;
            offset = offset.add((Math.random() - 0.5) * 0.02, 0.04, (Math.random() - 0.5) * 0.02); 
        }
    }
}
