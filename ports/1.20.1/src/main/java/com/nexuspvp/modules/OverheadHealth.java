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
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class OverheadHealth extends Module {

    private final BooleanSetting playersOnly = addSetting(new BooleanSetting("PlayersOnly", false));
    private final BooleanSetting ghostDamage = addSetting(new BooleanSetting("GhostDamage", true));
    private final NumberSetting range = addSetting(new NumberSetting("Range", 35.0, 5.0, 60.0, 1.0));

    private static class HealthTracker {
        float ghostHp;
        float previousHp;
        long lastHitTime;
    }

    private final Map<Integer, HealthTracker> trackers = new HashMap<>();

    public OverheadHealth() {
        super("OverheadHealth", "Clean health bar above entities with ghost damage", Category.RENDER);
        setEnabled(true);
    }

    public void renderOverhead(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider vcp, float tickDelta, int light) {
        if (mc.player == null || entity == mc.player || !entity.isAlive() || entity.isInvisible()) return;
        if (playersOnly.isEnabled() && !(entity instanceof PlayerEntity)) return;
        if (entity.squaredDistanceTo(mc.player) > range.getValue() * range.getValue()) return;
        if (!mc.player.canSee(entity)) return;

        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float absorb = entity.getAbsorptionAmount();

        // Ghost damage
        HealthTracker t = trackers.computeIfAbsent(entity.getId(), id -> {
            HealthTracker tr = new HealthTracker();
            tr.ghostHp = health;
            tr.previousHp = health;
            tr.lastHitTime = 0;
            return tr;
        });
        if (health < t.previousHp) {
            t.ghostHp = t.previousHp;
            t.lastHitTime = System.currentTimeMillis();
        }
        t.previousHp = health;
        if (System.currentTimeMillis() - t.lastHitTime > 400) {
            t.ghostHp = MathHelper.lerp(0.08f, t.ghostHp, health);
        }
        if (health > t.ghostHp) t.ghostHp = health;

        float hpPct = MathHelper.clamp(health / maxHealth, 0f, 1f);
        float ghostPct = MathHelper.clamp(t.ghostHp / maxHealth, 0f, 1f);

        // Color
        int hpColor = hpPct > 0.55f ? 0xFF23A55A : (hpPct > 0.25f ? 0xFFFEE75C : 0xFFED4245);

        // Format HP text
        String hpText = String.format("%.0f / %.0f", health, maxHealth);
        if (absorb > 0) hpText += String.format(" +%.0f", absorb);

        matrices.push();

        // Position above entity head (same as vanilla nameplate)
        matrices.translate(0.0, entity.getHeight() + 0.5, 0.0);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);

        Matrix4f mat = matrices.peek().getPositionMatrix();
        TextRenderer tr = mc.textRenderer;

        int textW = tr.getWidth(hpText);
        float centerX = -textW / 2.0f;

        // 1) HP text with dark background (above the bar)
        tr.draw(hpText, centerX, -3, hpColor, false, mat, vcp,
                TextRenderer.TextLayerType.NORMAL, 0x80000000, light);

        // 2) Health bar below text using space characters with backgrounds
        // Bar width = text width (matches text above), height = ~4px
        // We abuse TextRenderer background rectangles for solid color fills:
        // Draw invisible spaces with colored backgrounds

        int barW = Math.max(textW, 60);
        float barX = -barW / 2.0f;
        float barY = 7;

        // Background track (dark) - draw a string of spaces
        String barSpaces = generateSpaces(barW, tr);
        int spacesW = tr.getWidth(barSpaces);
        float spacesX = -spacesW / 2.0f;

        // Dark background track
        tr.draw(barSpaces, spacesX, barY, 0x00000000, false, mat, vcp,
                TextRenderer.TextLayerType.NORMAL, 0xDD2B2D31, light);

        // Ghost damage segment (white)
        if (ghostDamage.isEnabled() && ghostPct > hpPct) {
            int ghostChars = (int) Math.ceil(ghostPct * barSpaces.length());
            int activeChars = (int) Math.ceil(hpPct * barSpaces.length());
            if (ghostChars > activeChars && ghostChars <= barSpaces.length()) {
                String ghostSeg = barSpaces.substring(0, ghostChars - activeChars);
                float ghostX = spacesX + tr.getWidth(barSpaces.substring(0, activeChars));
                tr.draw(ghostSeg, ghostX, barY, 0x00000000, false, mat, vcp,
                        TextRenderer.TextLayerType.NORMAL, 0xCCFFFFFF, light);
            }
        }

        // Active health fill (colored)
        int activeChars = (int) Math.ceil(hpPct * barSpaces.length());
        if (activeChars > 0 && activeChars <= barSpaces.length()) {
            String activeSeg = barSpaces.substring(0, activeChars);
            tr.draw(activeSeg, spacesX, barY, 0x00000000, false, mat, vcp,
                    TextRenderer.TextLayerType.NORMAL, hpColor, light);
        }

        // Absorption (gold)
        if (absorb > 0 && activeChars < barSpaces.length()) {
            float absPct = MathHelper.clamp(absorb / maxHealth, 0f, 1f);
            int absChars = Math.min((int) Math.ceil(absPct * barSpaces.length()), barSpaces.length() - activeChars);
            if (absChars > 0) {
                String absSeg = barSpaces.substring(0, absChars);
                float absX = spacesX + tr.getWidth(barSpaces.substring(0, activeChars));
                tr.draw(absSeg, absX, barY, 0x00000000, false, mat, vcp,
                        TextRenderer.TextLayerType.NORMAL, 0xDDFFD700, light);
            }
        }

        matrices.pop();
    }

    private String generateSpaces(int targetWidth, TextRenderer tr) {
        // Generate a string of thin characters to approximate target pixel width
        StringBuilder sb = new StringBuilder();
        int w = 0;
        while (w < targetWidth) {
            sb.append(' ');
            w = tr.getWidth(sb.toString());
        }
        return sb.toString();
    }
}
