package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.ColorSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.TntEntity;

import java.awt.Color;

public class TNTTimer extends Module {

    private final BooleanSetting showTime = new BooleanSetting("ShowTime", true);
    private final BooleanSetting dangerZone = new BooleanSetting("DangerZone", true);
    private final NumberSetting scale = new NumberSetting("Scale", 1.0, 0.5, 2.5, 0.1);
    private final ColorSetting safeColor = new ColorSetting("Safe", new Color(0, 255, 120));
    private final ColorSetting dangerColor = new ColorSetting("Danger", new Color(255, 40, 40));

    public TNTTimer() {
        super("TNTTimer", "Shows precise countdown and danger zone over primed TNT", Category.VISUAL, 0);
        addSetting(showTime);
        addSetting(dangerZone);
        addSetting(scale);
        addSetting(safeColor);
        addSetting(dangerColor);
    }

    public static void renderTNTTimer(TntEntity tnt, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {}
}
