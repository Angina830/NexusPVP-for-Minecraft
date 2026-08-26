package com.nexuspvp.modules;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.util.ColorUtils;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HudModule extends Module {

    private final BooleanSetting arrayList = new BooleanSetting("ArrayList", true);
    private final BooleanSetting coords = new BooleanSetting("Coords", true);
    private final BooleanSetting fps = new BooleanSetting("FPS", true);
    private final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    
    private final ColorSetting color = new ColorSetting("Color", new Color(160, 0, 255));
    private final ModeSetting arrayListPosition = new ModeSetting("ArrayListPosition", "Right", "Right", "Left");
    private final BooleanSetting rainbow = new BooleanSetting("Rainbow", false);
    private final BooleanSetting background = new BooleanSetting("Background", true);

    public HudModule() {
        super("HUD", "Customizable HUD", Category.HUD);
        addSetting(arrayList);
        addSetting(coords);
        addSetting(fps);
        addSetting(watermark);
        addSetting(color);
        addSetting(arrayListPosition);
        addSetting(rainbow);
        addSetting(background);
    }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.options.hudHidden) return;

        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        Color baseColor = color.getColor();
        if (rainbow.isEnabled()) {
            baseColor = ColorUtils.rainbow(0);
        }
        
        // Draw Radio Player Info
        if (Radio.downloadProgress != null && !Radio.downloadProgress.isEmpty()) {
            String text = Radio.downloadProgress;
            int textWidth = mc.textRenderer.getWidth(text);
            int x = width / 2 - textWidth / 2;
            int y = 20; 
            
            net.minecraft.client.gui.DrawableHelper.fill(matrices, x - 4, y - 4, x + textWidth + 4, y + mc.textRenderer.fontHeight + 4, 0x80000000);
            mc.textRenderer.drawWithShadow(matrices, text, x, y, 0xFF00FFFF); 
        } else if (Radio.currentTrackName != null && !Radio.currentTrackName.isEmpty()) {
            String text = "Now Playing: " + Radio.currentTrackName;
            int textWidth = mc.textRenderer.getWidth(text);
            int x = width / 2 - textWidth / 2;
            int y = 20;
            
            net.minecraft.client.gui.DrawableHelper.fill(matrices, x - 4, y - 4, x + textWidth + 4, y + mc.textRenderer.fontHeight + 4, 0x80000000);
            mc.textRenderer.drawWithShadow(matrices, text, x, y, 0xFF00FF00); 
        }

        int yOffset = 2;
        if (watermark.isEnabled()) {
            String text = "NexusPVP";
            if (background.isEnabled()) {
                net.minecraft.client.gui.DrawableHelper.fill(matrices, 2, yOffset, 2 + mc.textRenderer.getWidth(text) + 2, yOffset + mc.textRenderer.fontHeight + 2, 0x80000000);
            }
            mc.textRenderer.drawWithShadow(matrices, text, 4, yOffset + 2, baseColor.getRGB());
            yOffset += mc.textRenderer.fontHeight + 4;
        }

        if (fps.isEnabled()) {
            String text = "FPS: " + mc.fpsDebugString.split(" ")[0];
            if (background.isEnabled()) {
                net.minecraft.client.gui.DrawableHelper.fill(matrices, 2, yOffset, 2 + mc.textRenderer.getWidth(text) + 2, yOffset + mc.textRenderer.fontHeight + 2, 0x80000000);
            }
            mc.textRenderer.drawWithShadow(matrices, text, 4, yOffset + 2, baseColor.getRGB());
        }

        if (coords.isEnabled() && mc.player != null) {
            String text = String.format("XYZ: %.1f, %.1f, %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            int textWidth = mc.textRenderer.getWidth(text);
            int y = height - mc.textRenderer.fontHeight - 4;
            if (background.isEnabled()) {
                net.minecraft.client.gui.DrawableHelper.fill(matrices, 2, y, 2 + textWidth + 2, y + mc.textRenderer.fontHeight + 2, 0x80000000);
            }
            mc.textRenderer.drawWithShadow(matrices, text, 4, y + 2, baseColor.getRGB());
        }

        if (arrayList.isEnabled()) {
            List<Module> activeModules = NexusPVP.getInstance().getModuleManager().getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())))
                .collect(Collectors.toList());

            int y = 2;
            boolean isRight = arrayListPosition.is("Right");

            int index = 0;
            for (Module m : activeModules) {
                String text = m.getName();
                int textWidth = mc.textRenderer.getWidth(text);
                int x = isRight ? width - textWidth - 4 : 2;

                Color c = baseColor;
                if (rainbow.isEnabled()) {
                    c = ColorUtils.rainbow(index * 200L);
                }

                if (background.isEnabled()) {
                    net.minecraft.client.gui.DrawableHelper.fill(matrices, x - 2, y, x + textWidth + 2, y + mc.textRenderer.fontHeight + 2, 0x80000000);
                }
                mc.textRenderer.drawWithShadow(matrices, text, x, y + 2, c.getRGB());
                y += mc.textRenderer.fontHeight + 2;
                index++;
            }
        }
    }
}