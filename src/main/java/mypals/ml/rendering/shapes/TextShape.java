package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static mypals.ml.rendering.ShapeRender.*;

public class TextShape {
    private static double lastTickPosX,lastTickPosY,lastTickPosZ;
    public ArrayList<String> texts;
    public Vec3d pos;
    public float size;
    public ArrayList<Color> color;
    public float alpha;
    public boolean seeThrough;
    public TextShape(ArrayList<String> text, Vec3d pos, float size, ArrayList<Color> color, float alpha, boolean seeThrough){
        this.texts = text;
        this.pos = pos;
        this.size = size;
        this.color = color;
        this.alpha = alpha;
        this.seeThrough = seeThrough;
    }

    public static void drawMultiple(MatrixStack matrices, List<TextShape> shapes, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (!camera.isReady() || client.getEntityRenderDispatcher().gameOptions == null || client.player == null || shapes.isEmpty()) {
            return;
        }

        matrices.push();
        Vec3d cameraPos = camera.getPos();
        TextRenderer textRenderer = client.textRenderer;

        GlStateManager._enableBlend();

        List<TextShape> opaqueShapes = shapes.stream().filter(shape -> !shape.seeThrough).collect(Collectors.toList());
        List<TextShape> seeThroughShapes = shapes.stream().filter(shape -> shape.seeThrough).collect(Collectors.toList());

        if (!opaqueShapes.isEmpty()) {
            VertexConsumerProvider.Immediate immediate = getVertexConsumer();
            drawShapes(matrices, opaqueShapes, tickDelta, cameraPos, textRenderer, immediate, false);
            GlStateManager._enableDepthTest();
            immediate.draw();
        }

        if (!seeThroughShapes.isEmpty()) {
            VertexConsumerProvider.Immediate immediate = getVertexConsumer();
            drawShapes(matrices, seeThroughShapes, tickDelta, cameraPos, textRenderer, immediate, true);
            GlStateManager._disableDepthTest();
            immediate.draw();
            GlStateManager._enableDepthTest();
        }

        GlStateManager._disableBlend();
        matrices.pop();
    }

    private static void drawShapes(MatrixStack matrices, List<TextShape> shapes, float tickDelta, Vec3d cameraPos,
                                   TextRenderer textRenderer, VertexConsumerProvider immediate, boolean seeThrough) {
        for (TextShape shape : shapes) {
            if (shape.texts.isEmpty()) continue;

            matrices.push();

            float x = (float) (shape.pos.getX() - cameraPos.getX());
            float y = (float) (shape.pos.getY() - cameraPos.getY());
            float z = (float) (shape.pos.getZ() - cameraPos.getZ());

            matrices.translate(x, y, z);
            matrices.multiply(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
            matrices.scale(shape.size, -shape.size, 1);
            Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();

            float[] lineHeights = new float[shape.texts.size()];
            float totalHeight = 0.0f;
            for (int i = 0; i < shape.texts.size(); i++) {
                lineHeights[i] = textRenderer.getWrappedLinesHeight(shape.texts.get(i), Integer.MAX_VALUE) * 1.25f;
                totalHeight += lineHeights[i];
            }

            float renderYBase = -totalHeight / 2.0f;
            for (int i = 0; i < shape.texts.size(); i++) {
                String text = shape.texts.get(i);
                float renderX = -textRenderer.getWidth(text) * 0.5f;
                float renderY = renderYBase + (i > 0 ? lineHeights[i - 1] : 0);
                int colorValue = (shape.color.size() > i && shape.color.get(i) != null) ? shape.color.get(i).getRGB() : Color.WHITE.getRGB();

                textRenderer.draw(
                        text, renderX, renderY, colorValue, true,
                        modelViewMatrix, immediate,
                        seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL,
                        0, 0xF000F0
                );

                if (i > 0) renderYBase += lineHeights[i - 1];
            }

            matrices.pop();
        }
    }
}
