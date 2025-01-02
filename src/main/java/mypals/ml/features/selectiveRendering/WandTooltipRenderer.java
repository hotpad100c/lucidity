package mypals.ml.features.selectiveRendering;

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
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;

public class WandTooltipRenderer {
    public static void renderWandTooltip(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if(client.player.getInventory() == null) return;

        // 获取当前选中的物品
        ItemStack stack = client.player.getInventory().getMainHandStack();
        if (!stack.isOf(wand)) return;

        // 定义提示内容
        String[] tooltips = {
                net.minecraft.text.Text.translatable("config.lucidity.wand.how_to_use_lc").getString(),
                net.minecraft.text.Text.translatable("config.lucidity.wand.how_to_use_ctrl_lc").getString(),
                net.minecraft.text.Text.translatable("config.lucidity.wand.how_to_use_shift_lc").getString(),
                net.minecraft.text.Text.translatable("config.lucidity.wand.how_to_use_rc").getString(),
                net.minecraft.text.Text.translatable("config.lucidity.wand.how_to_use_ctrl_space").getString()
        };


        // 获取屏幕尺寸和物品栏位置
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // 渲染背景框和文字
        //renderTooltip(context, client, tooltips, tooltipX, tooltipY);
    }

    private static void renderTooltip(DrawContext context, MinecraftClient client, String[] lines, int x, int y) {
        // 计算 tooltip 的宽度和高度
        int width = 0;
        for (String line : lines) {
            int lineWidth = client.textRenderer.getWidth(line);
            if (lineWidth > width) {
                width = lineWidth;
            }
        }
        int height = lines.length * 10; // 每行高度约为10像素

        //TooltipBackgroundRenderer.render(context,x - 3, y - 3, x + width + 3, y + height + 3, 5);
        context.fill(x - 5, y - 3, x + width + 5, y + height + 3, 0x80000000);
        // 渲染背景框

        // 渲染文字
        for (int i = 0; i < lines.length; i++) {
            context.drawText(client.textRenderer, lines[i], x, y + i * 10, 0xFFFFFF,true); // 白色文字
        }
    }
}
