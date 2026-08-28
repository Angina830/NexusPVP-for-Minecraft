package com.nexuspvp.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexuspvp.gui.ThemeManager;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GalaxySky extends Module {

    private final ModeSetting style = addSetting(new ModeSetting("Style", "Galaxy", "Galaxy", "Aurora", "Custom", "Theme"));
    private final ColorSetting customColor = addSetting(new ColorSetting("Color", new Color(140, 40, 255)));
    private final NumberSetting starCount = addSetting(new NumberSetting("Stars", 1500, 300, 3000, 100));
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 1.0, 0.1, 4.0, 0.1));
    private final BooleanSetting twinkle = addSetting(new BooleanSetting("Twinkle", true));

    private final List<Star> stars = new ArrayList<>();
    private float rotationAngle = 0.0f;

    public GalaxySky() {
        super("GalaxySky", "Custom cosmic galaxy sky and aurora borealis at night", Category.VISUAL);
        generateStars(1500);
    }

    private void generateStars(int count) {
        stars.clear();
        Random rand = new Random(1337L);
        for (int i = 0; i < count; i++) {
            double u = rand.nextDouble();
            double v = rand.nextDouble();
            double theta = u * 2.0 * Math.PI;
            double phi = Math.acos(2.0 * v - 1.0);
            double r = 100.0;

            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = r * Math.sin(phi) * Math.sin(theta);
            double z = r * Math.cos(phi);

            float size = 0.4f + rand.nextFloat() * 0.7f;
            float twinkleSpeed = 0.02f + rand.nextFloat() * 0.05f;
            float twinkleOffset = rand.nextFloat() * (float) Math.PI * 2;
            int rCol = 200 + rand.nextInt(55);
            int gCol = 200 + rand.nextInt(55);
            int bCol = 230 + rand.nextInt(25);

            stars.add(new Star(x, y, z, size, twinkleSpeed, twinkleOffset, new Color(rCol, gCol, bCol)));
        }
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;
        rotationAngle += 0.05f * speed.getFloatValue();
        if (rotationAngle >= 360.0f) rotationAngle -= 360.0f;

        if (stars.size() != starCount.getIntValue()) {
            generateStars(starCount.getIntValue());
        }
    }

    public void renderCustomSky(MatrixStack matrices, float tickDelta) {
        if (mc.world == null) return;

        RenderSystem.disableTexture();
        RenderSystem.disableFog();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        matrices.push();
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationAngle));

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();

        // 1. Color configuration
        Color primary;
        Color secondary;
        String st = style.getValue();

        if (st.equals("Aurora")) {
            primary = new Color(13, 79, 60, 255);
            secondary = new Color(24, 160, 100, 180);
        } else if (st.equals("Custom")) {
            primary = customColor.getColor();
            secondary = new Color(primary.getRed() / 2, primary.getGreen() / 2, primary.getBlue() / 2, 180);
        } else if (st.equals("Theme")) {
            primary = ThemeManager.getInstance().getAccentColor();
            secondary = new Color(primary.getRed() / 3, primary.getGreen() / 3, primary.getBlue() / 3, 180);
        } else {
            // Galaxy default
            primary = new Color(50, 12, 85, 255);
            secondary = new Color(12, 25, 75, 200);
        }

        // Draw Sky Dome Quads
        float r = 100.0f;
        int prR = primary.getRed();
        int prG = primary.getGreen();
        int prB = primary.getBlue();
        int scR = secondary.getRed();
        int scG = secondary.getGreen();
        int scB = secondary.getBlue();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);

        // Top Sky
        buffer.vertex(matrix, -r, r, -r).color(prR, prG, prB, 255).next();
        buffer.vertex(matrix, r, r, -r).color(prR, prG, prB, 255).next();
        buffer.vertex(matrix, r, r, r).color(prR, prG, prB, 255).next();
        buffer.vertex(matrix, -r, r, r).color(prR, prG, prB, 255).next();

        // North Wall
        buffer.vertex(matrix, -r, -r, -r).color(scR, scG, scB, 180).next();
        buffer.vertex(matrix, r, -r, -r).color(scR, scG, scB, 180).next();
        buffer.vertex(matrix, r, r, -r).color(prR, prG, prB, 255).next();
        buffer.vertex(matrix, -r, r, -r).color(prR, prG, prB, 255).next();

        // South Wall
        buffer.vertex(matrix, r, -r, r).color(scR, scG, scB, 180).next();
        buffer.vertex(matrix, -r, -r, r).color(scR, scG, scB, 180).next();
        buffer.vertex(matrix, -r, r, r).color(prR, prG, prB, 255).next();
        buffer.vertex(matrix, r, r, r).color(prR, prG, prB, 255).next();

        // East Wall
        buffer.vertex(matrix, r, -r, -r).color(scR, scG, scB, 180).next();
        buffer.vertex(matrix, r, -r, r).color(scR, scG, scB, 180).next();
        buffer.vertex(matrix, r, r, r).color(prR, prG, prB, 255).next();
        buffer.vertex(matrix, r, r, -r).color(prR, prG, prB, 255).next();

        // West Wall
        buffer.vertex(matrix, -r, -r, r).color(scR, scG, scB, 180).next();
        buffer.vertex(matrix, -r, -r, -r).color(scR, scG, scB, 180).next();
        buffer.vertex(matrix, -r, r, -r).color(prR, prG, prB, 255).next();
        buffer.vertex(matrix, -r, r, r).color(prR, prG, prB, 255).next();

        tessellator.draw();

        // 2. Render Procedural Twinkling Stars
        long now = System.currentTimeMillis();
        GL11.glPointSize(2.0f);
        buffer.begin(GL11.GL_POINTS, VertexFormats.POSITION_COLOR);

        for (Star star : stars) {
            float alpha = 1.0f;
            if (twinkle.isEnabled()) {
                alpha = 0.35f + 0.65f * (float) (Math.sin(now * star.twinkleSpeed + star.twinkleOffset) * 0.5 + 0.5);
            }
            int starAlpha = (int) (alpha * 255);
            buffer.vertex(matrix, (float) star.x, (float) star.y, (float) star.z)
                  .color(star.color.getRed(), star.color.getGreen(), star.color.getBlue(), starAlpha)
                  .next();
        }

        tessellator.draw();

        matrices.pop();
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableFog();
    }

    private static class Star {
        final double x, y, z;
        final float size;
        final float twinkleSpeed;
        final float twinkleOffset;
        final Color color;

        Star(double x, double y, double z, float size, float twinkleSpeed, float twinkleOffset, Color color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
            this.twinkleSpeed = twinkleSpeed;
            this.twinkleOffset = twinkleOffset;
            this.color = color;
        }
    }
}
