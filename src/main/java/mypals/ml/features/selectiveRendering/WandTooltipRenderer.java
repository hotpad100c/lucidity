package mypals.ml.features.selectiveRendering;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.renderable.RenderContext;
import java.util.ArrayList;
import java.util.List;

import static mypals.ml.Lucidity.MOD_ID;
import static mypals.ml.config.Keybinds.*;
import static mypals.ml.config.LucidityConfig.selectInSpectator;
import static mypals.ml.features.renderKeyPresses.KeyPressesManager.getTranslatedKeyName;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.selectedAreas;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;
import static mypals.ml.features.selectiveRendering.WandActionsManager.*;

public class WandTooltipRenderer {
    private static final List<ToolTipItem> hudItems = new ArrayList<>();
    private static class ToolTipItem {
        String text;
        int color;

        @Nullable
        Identifier icon;
        public ToolTipItem(String text, Color color, Identifier icon) {
            this.text = text;
            this.color = (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
            this.icon = icon;
        }
    }
    public static void addTooltip(String text, Color color, Identifier icon) {
        hudItems.add(new ToolTipItem(text, color, icon));
    }

    // 动态移除HUD条目（按索引）
    public void removeTooltip(int index) {
        if (index >= 0 && index < hudItems.size()) {
            hudItems.remove(index);
        }
    }
    public static void generateTooltip(){
        hudItems.clear();
        if(switchRenderMode.isPressed()){
            WandTooltipRenderer.addTooltip(Text.translatable("config.lucidity.wand.switchWandMode").getString(), new Color(255,255,255,200), Identifier.of(MOD_ID, "textures/gui/mouse_middle.png"));
            WandTooltipRenderer.addTooltip(Text.translatable("config.lucidity.wand.switchRenderingNext").getString(), new Color(255,255,255,200), Identifier.of(MOD_ID, "textures/gui/mouse_right.png"));
            WandTooltipRenderer.addTooltip(Text.translatable("config.lucidity.wand.switchRenderingLast").getString(), new Color(255,255,255,200), Identifier.of(MOD_ID, "textures/gui/mouse_left.png"));
        }else if(addArea.isPressed()){
            if(wandApplyToMode != WandApplyToMode.APPLY_TO_PARTICLES) {
                WandTooltipRenderer.addTooltip(Text.translatable("config.lucidity.wand.addSpecific").getString(), new Color(255, 255, 255, 200), Identifier.of(MOD_ID, "textures/gui/mouse_right.png"));
            }if(pos1 != null && pos2 != null){
                WandTooltipRenderer.addTooltip(Text.translatable("config.lucidity.wand.addArea").getString(), new Color(255,255,255,200), Identifier.of(MOD_ID, "textures/gui/mouse_left.png"));
            }
        }if(deleteArea.isPressed()){
            WandTooltipRenderer.addTooltip(Text.translatable("config.lucidity.wand.delete").getString(), new Color(255,180,180,200), Identifier.of(MOD_ID, "textures/gui/mouse_right.png"));
            if(pos1 != null && pos2 != null){
                WandTooltipRenderer.addTooltip(Text.translatable("config.lucidity.wand.cut").getString(), new Color(255,200,200,200), Identifier.of(MOD_ID, "textures/gui/mouse_left.png"));
            }
        }else{
            WandTooltipRenderer.addTooltip(Text.translatable(switchRenderMode.getTranslationKey()).getString() +"("+switchRenderMode.getBoundKeyLocalizedText().getString()+")", new Color(255,255,255,200), Identifier.of(MOD_ID, "textures/gui/hotkey.png"));
            if(pos1 == null){
                WandTooltipRenderer.addTooltip(Text.translatable("config.lucidity.wand.selectP1").getString(), new Color(255,255,255,200), Identifier.of(MOD_ID, "textures/gui/mouse_left.png"));
            }if(pos2 == null){
                WandTooltipRenderer.addTooltip(Text.translatable("config.lucidity.wand.selectP2").getString(), new Color(255,255,255,200), Identifier.of(MOD_ID, "textures/gui/mouse_right.png"));
            }if(pos1 != null && pos2 != null){
                WandTooltipRenderer.addTooltip(Text.translatable(addArea.getTranslationKey()).getString() +"("+addArea.getBoundKeyLocalizedText().getString()+")", new Color(255,255,255,200), Identifier.of(MOD_ID, "textures/gui/hotkey.png"));
                WandTooltipRenderer.addTooltip(Text.translatable(deleteArea.getTranslationKey()).getString()+"("+deleteArea.getBoundKeyLocalizedText().getString()+")", new Color(255,200,200,200), Identifier.of(MOD_ID, "textures/gui/hotkey.png"));

            }
        }

    }
    public static void renderWandTooltip(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        boolean shouldSelect = player.getMainHandStack().getItem() == wand || (selectInSpectator && player.isSpectator());
        if (!shouldSelect) {
            return;
        }

        generateTooltip();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int x = centerX - 50;
        int y = centerY + 5;
        int lineHeight = 15;

        int maxTextWidth = 0;
        for (ToolTipItem item : hudItems) {
            int textWidth = client.textRenderer.getWidth(item.text);
            maxTextWidth = Math.max(maxTextWidth, textWidth);
        }

        x = centerX - maxTextWidth / 2;

        for (ToolTipItem item : hudItems) {
            if (item.icon != null) {
                context.drawTexture(item.icon, x, y, 0, 0, 16, 16, 16, 16);
            }

            int textX = x + (item.icon != null ? 20 : 0);
            context.drawText(client.textRenderer, item.text, textX, (int) (y + 4), item.color, true);

            y += lineHeight;
        }
    }

}
