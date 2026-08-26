package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.util.Compat;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;

public class DebugLogger extends Module {

    public DebugLogger() {
        super("DebugLogger", "Logs debug info", Category.MISC, 0);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {}
}
