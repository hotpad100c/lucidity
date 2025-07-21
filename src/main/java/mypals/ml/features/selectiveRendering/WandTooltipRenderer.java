package mypals.ml.features.selectiveRendering;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.renderable.RenderContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static mypals.ml.Lucidity.MOD_ID;
import static mypals.ml.config.Keybinds.*;
import static mypals.ml.config.LucidityConfig.renderModeBlock;
import static mypals.ml.config.LucidityConfig.selectInSpectator;
import static mypals.ml.features.renderKeyPresses.KeyPressesManager.getTranslatedKeyName;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.*;
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

    public static void generateTooltip() {
        hudItems.clear();

        TriConsumer<String,Color,String> addTooltip = (key, color, icon) ->
                WandTooltipRenderer.addTooltip(Text.translatable(key).getString(), color, Identifier.of(MOD_ID, icon));

        TriConsumer<KeyBinding,Color ,String> addKeyTooltip = (key,color, icon) ->
                addTooltip.accept(Text.translatable(key.getTranslationKey()).getString() + "(" + key.getBoundKeyLocalizedText().getString() + ")", color, icon);

        if (switchRenderMode.isPressed()) {
            addKeyTooltip.accept(switchRenderMode, new Color(200, 255, 200, 200),"textures/gui/hotkey.png");
            addTooltip.accept("config.lucidity.wand.switchWandMode", new Color(255, 255, 255, 200), "textures/gui/mouse_middle.png");
            addTooltip.accept("config.lucidity.wand.switchRenderingNext", new Color(255, 255, 255, 200), "textures/gui/mouse_right.png");
            addTooltip.accept("config.lucidity.wand.switchRenderingLast", new Color(255, 255, 255, 200), "textures/gui/mouse_left.png");
        } else if (addArea.isPressed()) {
            addKeyTooltip.accept(addArea, new Color(200, 255, 200, 200),"textures/gui/hotkey.png");
            if (wandApplyToMode != WandApplyToMode.APPLY_TO_PARTICLES) {
                addTooltip.accept("config.lucidity.wand.addSpecific", new Color(255, 255, 255, 200), "textures/gui/mouse_right.png");
            }
            if (pos1 != null && pos2 != null) {
                addTooltip.accept("config.lucidity.wand.addArea", new Color(255, 255, 255, 200), "textures/gui/mouse_left.png");
            }
        } else if (deleteArea.isPressed()) {
            addKeyTooltip.accept(deleteArea,new Color(200, 255, 200, 200), "textures/gui/hotkey.png");
            addTooltip.accept("config.lucidity.wand.delete", new Color(255, 180, 180, 200), "textures/gui/mouse_right.png");
            if (pos1 != null && pos2 != null) {
                addTooltip.accept("config.lucidity.wand.cut", new Color(255, 200, 200, 200), "textures/gui/mouse_left.png");
            }
        } else {
            if (pos1 == null) {
                addTooltip.accept("config.lucidity.wand.selectP1", new Color(255, 255, 255, 200), "textures/gui/mouse_left.png");
            }
            if (pos2 == null) {
                addTooltip.accept("config.lucidity.wand.selectP2", new Color(255, 255, 255, 200), "textures/gui/mouse_right.png");
            }
            if (pos1 != null && pos2 != null) {
                if (!addArea.isPressed()) {
                    addKeyTooltip.accept(addArea, new Color(255, 255, 255, 200),"textures/gui/hotkey.png");
                }
                if (!deleteArea.isPressed()) {
                    addKeyTooltip.accept(deleteArea, new Color(255, 255, 255, 200),"textures/gui/hotkey.png");
                }
            }
            if (!switchRenderMode.isPressed()) {
                addKeyTooltip.accept(switchRenderMode, new Color(255, 255, 255, 200),"textures/gui/hotkey.png");
            }
        }
    }
    public static void renderWandTooltip(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        boolean shouldSelect = player.getMainHandStack().getItem() == wand || (selectInSpectator && player.isSpectator());
        if (!shouldSelect || client.options.hudHidden) {
            return;
        }
        generateTooltip();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int x = centerX - 50;
        int y = centerY + 5;
        int lineHeight = 10;

        int maxTextWidth = 0;
        for (ToolTipItem item : hudItems) {
            int textWidth = client.textRenderer.getWidth(item.text);
            maxTextWidth = Math.max(maxTextWidth, textWidth);
        }

        x = centerX - maxTextWidth / 2;
        for (ToolTipItem item : hudItems) {
            if (item.icon != null) {
                GlStateManager._enableBlend();
                context.drawTexture(RenderPipelines.GUI_TEXTURED, item.icon, x, y, 0, 0, 16, 16, 16, 16);
                GlStateManager._disableBlend();
            }

            int textX = x + (item.icon != null ? 20 : 0);
            context.drawText(client.textRenderer, item.text, textX, (int) (y + 4), item.color, true);

            y += lineHeight;
            renderWandModeIcon(context);
        }



    }
    public static void renderWandModeIcon(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        boolean shouldSelect = player.getMainHandStack().getItem() == wand || (selectInSpectator && player.isSpectator());
        if (!shouldSelect) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight;

        int iconWidth = 32;
        int distance = 2;

        int x = 0;
        int y = centerY - 60/* + iconYOffset*/;
        GlStateManager._enableBlend();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of(MOD_ID, wandApplyToMode.getIcon()), x, y, 0, 0, iconWidth, iconWidth, iconWidth, iconWidth);
        GlStateManager._disableBlend();
        context.drawText(client.textRenderer, Text.translatable(wandApplyToMode.getTranslationKey()), x+iconWidth+2, y+(iconWidth/2), 0xFFFFFFE0, true);

        String translationKey = "-";
        Identifier secondIcon = switch (wandApplyToMode) {
            case WandApplyToMode.APPLY_TO_BLOCKS -> {
                translationKey = blockRenderMode.getTranslationKey();
                yield Identifier.of(MOD_ID, blockRenderMode.getIcon());
            }
            case WandApplyToMode.APPLY_TO_ENTITIES -> {
                translationKey = entityRenderMode.getTranslationKey();
                yield Identifier.of(MOD_ID, entityRenderMode.getIcon());
            }
            case WandApplyToMode.APPLY_TO_PARTICLES -> {
                translationKey = particleRenderMode.getTranslationKey();
                yield Identifier.of(MOD_ID, particleRenderMode.getIcon());
            }
        };
        GlStateManager._enableBlend();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, secondIcon, x , y + iconWidth/2, 0, 0, iconWidth, iconWidth, iconWidth, iconWidth);
        GlStateManager._disableBlend();
        context.drawText(client.textRenderer, Text.translatable(translationKey), x+iconWidth, y + iconWidth, 0xFFFFFFE0, true);
    }
}
