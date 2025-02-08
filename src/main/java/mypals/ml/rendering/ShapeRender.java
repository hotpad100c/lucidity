package mypals.ml.rendering;
import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.features.selectiveRendering.WandActionsManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static mypals.ml.Lucidity.getPlayerLookedBlock;
import static mypals.ml.features.selectiveRendering.WandActionsManager.deleteMode;


public class ShapeRender {
    public static double lastTickPosX = 0;
    public static double lastTickPosY = 0;
    public static double lastTickPosZ = 0;

    public static void renderText(MatrixStack matrixStack, RenderTickCounter counter, BlockPos pos, String text, int color, float SIZE, boolean seeThrow)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Vec3d textPos = new Vec3d(pos.toCenterPos().toVector3f());
        drawString(matrixStack, counter, camera, textPos, text, color, SIZE, seeThrow);
    }
    public static void renderSelectionBox(MatrixStack matrices, Camera camera,float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        BlockPos origin = WandActionsManager.pos1;

        HitResult result = client.cameraEntity.raycast(player.getAbilities().creativeMode ? 5.0F : 4.5F, 0, false);
        BlockPos size = WandActionsManager.pos2 != null ? WandActionsManager.pos2 : (result.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) result).getBlockPos() : BlockPos.ofFloored(result.getPos()));
        if(size != null){
            ShapeRender.drawCubeSeeThrough(matrices, size, 0.01f, tickDelta, Color.blue, 0.2f);
        }
        if(origin != null){
            ShapeRender.drawCubeSeeThrough(matrices, origin, 0.01f, tickDelta, Color.red, 0.2f);
        }
        size = size.subtract(origin);

        origin = origin.add(size.getX() < 0 ? 1 : 0, size.getY() < 0 ? 1 : 0, size.getZ() < 0 ? 1 : 0);
        size = size.add(size.getX() >= 0 ? 1 : -1, size.getY() >= 0 ? 1 : -1, size.getZ() >= 0 ? 1 : -1);

        lastTickPosX = camera.getPos().getX();
        lastTickPosY = camera.getPos().getY();
        lastTickPosZ = camera.getPos().getZ();
        float x = (float) (origin.getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
        float y = (float) (origin.getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
        float z = (float) (origin.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));

        matrices.push();

        VertexConsumer consumer = client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getLines());
        matrices.translate(x, y, z);

        WorldRenderer.drawBox(matrices, consumer, 0, 0, 0, size.getX(), size.getY(), size.getZ(), 1, 1, 1, 1, 0, 0, 0);

        matrices.pop();
    }
    public static VertexConsumerProvider.Immediate getVertexConsumer() {
        return MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
    }
    public static void drawStringList(MatrixStack matrices, Vec3d textPos, float tickDelta, float line, ArrayList<String> texts, List<Color> colors, float size) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();

        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            matrices.push();
            lastTickPosX = camera.getPos().getX();
            lastTickPosY = camera.getPos().getY();
            lastTickPosZ = camera.getPos().getZ();
            float x = (float) (textPos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
            float y = (float) (textPos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
            float z = (float) (textPos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));

            matrices.translate(x, y, z);
            matrices.multiply(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
            matrices.scale(size, -size, 1);
            Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            RenderSystem.disableDepthTest();

            float totalHeight = 0.0F;
            for (String text : texts) {
                totalHeight += textRenderer.getWrappedLinesHeight(text, Integer.MAX_VALUE) * 1.25F;
            }

            float renderYBase = -totalHeight / 2.0F; // 起始位置，从底部开始
            for (int i = 0; i < texts.size(); i++) {
                float renderX = -textRenderer.getWidth(texts.get(i)) * 0.5F; // 居中
                float renderY = renderYBase + textRenderer.getWrappedLinesHeight(texts.get(i), Integer.MAX_VALUE) * 1.25F * i;
                VertexConsumerProvider.Immediate immediate = getVertexConsumer();
                textRenderer.draw(
                        texts.get(i), renderX, renderY, colors.get(i) != null? colors.get(i).getRGB() : Color.white.getRGB(), true,
                        modelViewMatrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0,
                        0xF000F0
                );
                immediate.draw();
            }
            matrices.pop();

            RenderSystem.enableDepthTest();
        }
    }
    public static void drawCube(MatrixStack matrices, BlockPos pos, float size, float tickDelta, Color color,float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            matrices.push();
            lastTickPosX = camera.getPos().getX();
            lastTickPosY = camera.getPos().getY();
            lastTickPosZ = camera.getPos().getZ();
            float x = (float) (pos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
            float y = (float) (pos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
            float z = (float) (pos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));

            matrices.translate(x, y, z);
            Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();
            RenderSystem.disableDepthTest();

            VertexConsumerProvider.Immediate immediate = getVertexConsumer();
            VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getDebugQuads());

            float minOffset = -0.001F - size;
            float maxOffset = 1.001F + size;

            float red = ((color.getRGB() >> 16) & 0xFF) / 255.0f;
            float green = ((color.getRGB() >> 8) & 0xFF) / 255.0f;
            float blue = (color.getRGB() & 0xFF) / 255.0f;

            vertexConsumer.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, alpha);

            vertexConsumer.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, alpha);

            vertexConsumer.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, alpha);

            vertexConsumer.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, alpha);

            vertexConsumer.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, alpha);

            vertexConsumer.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, alpha);


            immediate.draw();
            matrices.pop();

            RenderSystem.enableDepthTest();
        }
    }
    public static void drawArea(MatrixStack matrices, BlockPos minPos, BlockPos maxPos, float tickDelta, Color color, float alpha) {
        double midX = ((maxPos.getX() + 1f) + (minPos.getX())) / 2.0;
        double midY = ((maxPos.getY() + 1f) + (minPos.getY())) / 2.0;
        double midZ = ((maxPos.getZ() + 1f) + (minPos.getZ())) / 2.0;
        float length = Math.abs(maxPos.getX() - minPos.getX());
        float width = Math.abs(maxPos.getZ() - minPos.getZ());
        float height = Math.abs(maxPos.getY() - minPos.getY());
        Vec3d midpos = new Vec3d(midX, midY, midZ);

        Vec3d v1 = new Vec3d(minPos.getX(), minPos.getY(), minPos.getZ()); // 底面左下
        Vec3d v2 = new Vec3d(maxPos.getX()+1, minPos.getY(), minPos.getZ()); // 底面右下
        Vec3d v3 = new Vec3d(maxPos.getX()+1, minPos.getY(), maxPos.getZ()+1); // 底面右上
        Vec3d v4 = new Vec3d(minPos.getX(), minPos.getY(), maxPos.getZ()+1); // 底面左上

        Vec3d v5 = new Vec3d(minPos.getX(), maxPos.getY()+1, minPos.getZ()); // 顶面左下
        Vec3d v6 = new Vec3d(maxPos.getX()+1, maxPos.getY()+1, minPos.getZ()); // 顶面右下
        Vec3d v7 = new Vec3d(maxPos.getX()+1, maxPos.getY()+1, maxPos.getZ()+1); // 顶面右上
        Vec3d v8 = new Vec3d(minPos.getX(), maxPos.getY()+1, maxPos.getZ()+1); // 顶面左上

        // 绘制底面四条边
        ShapeRender.drawLine(matrices, v1, v2, tickDelta, color, 1);
        ShapeRender.drawLine(matrices, v2, v3, tickDelta, color, 1);
        ShapeRender.drawLine(matrices, v3, v4, tickDelta, color, 1);
        ShapeRender.drawLine(matrices, v4, v1, tickDelta, color, 1);

        // 绘制顶面四条边
        ShapeRender.drawLine(matrices, v5, v6, tickDelta, color, 1);
        ShapeRender.drawLine(matrices, v6, v7, tickDelta, color, 1);
        ShapeRender.drawLine(matrices, v7, v8, tickDelta, color, 1);
        ShapeRender.drawLine(matrices, v8, v5, tickDelta, color, 1);

        // 绘制四条竖线
        ShapeRender.drawLine(matrices, v1, v5, tickDelta, color, 1);
        ShapeRender.drawLine(matrices, v2, v6, tickDelta, color, 1);
        ShapeRender.drawLine(matrices, v3, v7, tickDelta, color, 1);
        ShapeRender.drawLine(matrices, v4, v8, tickDelta, color, 1);


        ShapeRender.drawCube(matrices,midpos,length+1.01f,width+1.01f,height+1.01f,tickDelta,color,alpha);
    }
    public static void drawCubeSeeThrough(MatrixStack matrices, BlockPos pos, float size, float tickDelta, Color color, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();

        if (camera.isReady() && client.player != null) {
            matrices.push();

            lastTickPosX = camera.getPos().getX();
            lastTickPosY = camera.getPos().getY();
            lastTickPosZ = camera.getPos().getZ();
            float x = (float) (pos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
            float y = (float) (pos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
            float z = (float) (pos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));

            matrices.translate(x, y, z);
            Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            float minOffset = -0.001F - size;
            float maxOffset = 1.001F + size;

            float red = ((color.getRGB() >> 16) & 0xFF) / 255.0f;
            float green = ((color.getRGB() >> 8) & 0xFF) / 255.0f;
            float blue = (color.getRGB() & 0xFF) / 255.0f;

            bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, alpha);

            bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, alpha);

            bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, alpha);

            bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, alpha);

            bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, alpha);

            bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, alpha);
            bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, alpha);

            matrices.pop();
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }
    public static void drawLine(MatrixStack matrixStack, Vec3d start, Vec3d end, float tickDelta, Color color,float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            lastTickPosX = camera.getPos().getX();
            lastTickPosY = camera.getPos().getY();
            lastTickPosZ = camera.getPos().getZ();
            float x = (float) (start.getX() - camera.getPos().getX());
            float y = (float) (start.getY() - camera.getPos().getY());
            float z = (float) (start.getZ() - camera.getPos().getZ());
            float red = ((color.getRGB() >> 16) & 0xFF) / 255.0f;
            float green = ((color.getRGB() >> 8) & 0xFF) / 255.0f;
            float blue = (color.getRGB() & 0xFF) / 255.0f;

            float normalX = 0.0F;
            float normalY = 1.0F;
            float normalZ = 0.0F;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            matrixStack.push();
            matrixStack.translate(x, y, z);
            Matrix4f modelViewMatrix = matrixStack.peek().getPositionMatrix();
            RenderSystem.disableDepthTest();

            buffer.vertex(modelViewMatrix, 0.0F, 0.0F, 0.0F)
                    .color(red, green, blue, alpha)
                    .normal(normalX, normalY, normalZ);
            buffer.vertex(modelViewMatrix, (float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z))
                    .color(red, green, blue, alpha)
                    .normal(normalX, normalY, normalZ);

            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableDepthTest();
            matrixStack.pop();
        }
    }
    public static void drawMultiPointLine(MatrixStack matrixStack, ArrayList<Vec3d> points, float tickDelta, Color color,float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d start = points.get(i);
            Vec3d end = points.get(i + 1);
            drawLine(matrixStack, start, end, tickDelta, color, alpha);
        }
    }
    public static void drawFrame(MatrixStack matrices, BlockPos pos, float length, float width, float height, float tickDelta, Color color, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();

        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            double camX = camera.getPos().getX();
            double camY = camera.getPos().getY();
            double camZ = camera.getPos().getZ();

            float red = ((color.getRGB() >> 16) & 0xFF) / 255.0f;
            float green = ((color.getRGB() >> 8) & 0xFF) / 255.0f;
            float blue = (color.getRGB() & 0xFF) / 255.0f;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            // Translate to the position
            matrices.push();
            matrices.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);
            Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();

            // Define the eight corners of the cube
            float x1 = 0;
            float y1 = 0;
            float z1 = 0;

            float x2 = length;
            float y2 = height;
            float z2 = width;

            // Draw 12 edges of the cube
            // Bottom face
            drawEdge(buffer, modelViewMatrix, x1, y1, z1, x2, y1, z1, red, green, blue, alpha); // Front edge
            drawEdge(buffer, modelViewMatrix, x1, y1, z2, x2, y1, z2, red, green, blue, alpha); // Back edge
            drawEdge(buffer, modelViewMatrix, x1, y1, z1, x1, y1, z2, red, green, blue, alpha); // Left edge
            drawEdge(buffer, modelViewMatrix, x2, y1, z1, x2, y1, z2, red, green, blue, alpha); // Right edge

            // Top face
            drawEdge(buffer, modelViewMatrix, x1, y2, z1, x2, y2, z1, red, green, blue, alpha); // Front edge
            drawEdge(buffer, modelViewMatrix, x1, y2, z2, x2, y2, z2, red, green, blue, alpha); // Back edge
            drawEdge(buffer, modelViewMatrix, x1, y2, z1, x1, y2, z2, red, green, blue, alpha); // Left edge
            drawEdge(buffer, modelViewMatrix, x2, y2, z1, x2, y2, z2, red, green, blue, alpha); // Right edge

            // Vertical edges
            drawEdge(buffer, modelViewMatrix, x1, y1, z1, x1, y2, z1, red, green, blue, alpha); // Front-left
            drawEdge(buffer, modelViewMatrix, x2, y1, z1, x2, y2, z1, red, green, blue, alpha); // Front-right
            drawEdge(buffer, modelViewMatrix, x1, y1, z2, x1, y2, z2, red, green, blue, alpha); // Back-left
            drawEdge(buffer, modelViewMatrix, x2, y1, z2, x2, y2, z2, red, green, blue, alpha); // Back-right

            BufferRenderer.drawWithGlobalProgram(buffer.end());

            matrices.pop();
            RenderSystem.enableDepthTest();
        }
    }

    private static void drawEdge(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha);
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha);
    }


    public static void drawCube(MatrixStack matrices, Vec3d pos, float length, float width, float height, float tickDelta, Color color, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            matrices.push();
            lastTickPosX = camera.getPos().getX();
            lastTickPosY = camera.getPos().getY();
            lastTickPosZ = camera.getPos().getZ();

            float x = (float) (pos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
            float y = (float) (pos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
            float z = (float) (pos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));

            matrices.translate(x, y, z);
            Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();
            RenderSystem.disableDepthTest();

            VertexConsumerProvider.Immediate immediate = getVertexConsumer();
            VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getDebugQuads());

            float xMin = -length / 2;
            float xMax = length / 2;
            float yMin = -height / 2;
            float yMax = height / 2;
            float zMin = -width / 2;
            float zMax = width / 2;

            float red = ((color.getRGB() >> 16) & 0xFF) / 255.0f;
            float green = ((color.getRGB() >> 8) & 0xFF) / 255.0f;
            float blue = (color.getRGB() & 0xFF) / 255.0f;


            vertexConsumer.vertex(modelViewMatrix, xMin, yMax, zMin).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMax, zMin).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMax, zMax).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMin, yMax, zMax).color(red, green, blue, alpha);


            vertexConsumer.vertex(modelViewMatrix, xMin, yMin, zMax).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMin, zMax).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMin, zMin).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMin, yMin, zMin).color(red, green, blue, alpha);


            vertexConsumer.vertex(modelViewMatrix, xMin, yMax, zMin).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMin, yMax, zMax).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMin, yMin, zMax).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMin, yMin, zMin).color(red, green, blue, alpha);


            vertexConsumer.vertex(modelViewMatrix, xMax, yMin, zMin).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMin, zMax).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMax, zMax).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMax, zMin).color(red, green, blue, alpha);


            vertexConsumer.vertex(modelViewMatrix, xMin, yMin, zMin).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMin, zMin).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMax, zMin).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMin, yMax, zMin).color(red, green, blue, alpha);


            vertexConsumer.vertex(modelViewMatrix, xMin, yMax, zMax).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMax, zMax).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMax, yMin, zMax).color(red, green, blue, alpha);
            vertexConsumer.vertex(modelViewMatrix, xMin, yMin, zMax).color(red, green, blue, alpha);

            immediate.draw();
            matrices.pop();

            RenderSystem.enableDepthTest();
        }
    }


    public static void drawString(MatrixStack matrixStack,RenderTickCounter tickCounter, Camera camera, Vec3d textPos, String text, int color, float SIZE, boolean seeThrow) {

        Matrix4fStack modelViewMatrix = new Matrix4fStack(1);
        modelViewMatrix.identity();

        float tickDelta = tickCounter.getTickDelta(true);
        float x = (float) (textPos.x - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
        float y = (float) (textPos.y - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
        float z = (float) (textPos.z - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));
        lastTickPosX = camera.getPos().getX();
        lastTickPosY = camera.getPos().getY();
        lastTickPosZ = camera.getPos().getZ();
        modelViewMatrix.translate(x, y, z);
        modelViewMatrix.rotate(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
        modelViewMatrix.scale(SIZE, -SIZE, 1);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float totalWidth = textRenderer.getWidth(text);
        float writtenWidth = 1;
        float renderX = -totalWidth * 0.5F + writtenWidth;

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.disableDepthTest();

        if(seeThrow)
            textRenderer.draw(text, renderX, 0, color, false, modelViewMatrix
                    , immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        else
            textRenderer.draw(text, renderX, 0, color, false, modelViewMatrix
                    , immediate, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        immediate.draw();
        RenderSystem.enableDepthTest();

    }
}
