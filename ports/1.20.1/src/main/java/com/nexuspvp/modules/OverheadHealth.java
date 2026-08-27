package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class OverheadHealth extends Module {

    private final BooleanSetting playersOnly = addSetting(new BooleanSetting("PlayersOnly", true));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 20.0, 5.0, 40.0, 1.0));

    public OverheadHealth() {
        super("OverheadHealth", "Renders clear floating health indicators above entities", Category.RENDER);
    }

    public void renderOverhead(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light) {
        if (mc.player == null || entity == mc.player || !entity.isAlive() || entity.isInvisible()) return;
        if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) return;
        if (entity.squaredDistanceTo(mc.player) > range.getValue() * range.getValue()) return;

        matrices.push();
        matrices.translate(0.0D, entity.getHeight() + 0.5F, 0.0D);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
        float scale = 0.025F;
        matrices.scale(-scale, -scale, scale);

        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        String hpText = String.format("%.1f HP", health);
        int color = health > maxHealth * 0.5f ? 0xFF55FF55 : (health > maxHealth * 0.25f ? 0xFFFFAA00 : 0xFFFF5555);

        int textW = mc.textRenderer.getWidth(hpText);
        mc.textRenderer.draw(hpText, -textW / 2.0f, 0, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x90000000, 0xF000F0);

        matrices.pop();
    }
}
