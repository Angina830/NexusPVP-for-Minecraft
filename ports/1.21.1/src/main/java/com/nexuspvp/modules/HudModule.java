package com.nexuspvp.modules;

import com.nexuspvp.NexusPVP;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;

public class HudModule extends Module {

    private final BooleanSetting arrayList = new BooleanSetting("ArrayList", true);
    private final BooleanSetting coords = new BooleanSetting("Coords", true);
    private final BooleanSetting fps = new BooleanSetting("FPS", true);
    private final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    private final ColorSetting color = new ColorSetting("Color", new Color(160, 0, 255));
    private final ModeSetting arrayListPosition = new ModeSetting("ArrayListPos", "Right", "Right", "Left");
    private final BooleanSetting rainbow = new BooleanSetting("Rainbow", false);
    private final BooleanSetting background = new BooleanSetting("Background", true);

    public HudModule() {
        super("HUD", "On-screen display elements", Category.RENDER, 0);
        setEnabled(true);
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
        if (mc.player == null) return;

        int accent = rainbow.isEnabled() ? com.nexuspvp.util.ColorUtils.rainbow(0).getRGB() : color.getColor().getRGB();

        if (watermark.isEnabled()) {
            Compat.drawText(matrices, "NexusPVP", 4, 4, accent);
        }

        if (fps.isEnabled()) {
            int y = watermark.isEnabled() ? 16 : 4;
            Compat.drawText(matrices, "FPS: " + mc.getCurrentFps(), 4, y, 0xFFFFFFFF);
        }

        if (coords.isEnabled()) {
            String coordText = String.format("XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            int screenHeight = mc.getWindow().getScaledHeight();
            Compat.drawText(matrices, coordText, 4, screenHeight - 12, 0xFFFFFFFF);
        }

        if (arrayList.isEnabled()) {
            List<Module> active = NexusPVP.getInstance().getModuleManager().getEnabledModules();
            active.sort(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())));

            int y = 4;
            int screenWidth = mc.getWindow().getScaledWidth();
            boolean right = arrayListPosition.is("Right");

            for (int i = 0; i < active.size(); i++) {
                Module m = active.get(i);
                if (m instanceof HudModule) continue;
                String name = m.getName();
                int textWidth = mc.textRenderer.getWidth(name);
                int x = right ? screenWidth - textWidth - 4 : 4;
                int modColor = rainbow.isEnabled() ? com.nexuspvp.util.ColorUtils.rainbow(i * 100L).getRGB() : accent;

                if (background.isEnabled()) {
                    RenderUtils.drawRect(matrices, x - 2, y - 1, textWidth + 4, 10, 0x80000000);
                }
                Compat.drawText(matrices, name, x, y, modColor);
                y += 10;
            }
        }
    }
}
