package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Collections;
import java.util.stream.Collectors;

public class OnGroundMarker {
    public BlockPos pos;
    public Color color;
    public float alpha;
    public boolean seeThrough;

    public OnGroundMarker(BlockPos pos, Color color, float alpha, boolean seeThrough) {
        this.pos = pos;
        this.color = color;
        this.alpha = alpha;
        this.seeThrough = seeThrough;
    }

    public void draw(MatrixStack matrixStack) {
        drawSingle(matrixStack, this);
    }

    private static void drawSingle(MatrixStack matrixStack, OnGroundMarker marker) {
        drawMultiple(matrixStack, Collections.singletonList(marker));
    }

    public static void draw(MatrixStack matrixStack, BlockPos pos, Color color, float alpha, boolean seeThrough) {
        drawMultiple(matrixStack, Collections.singletonList(new OnGroundMarker(pos, color, alpha, seeThrough)));
    }

    public static void drawMultiple(MatrixStack matrixStack, java.util.List<OnGroundMarker> markers) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (!camera.isReady() || client.getEntityRenderDispatcher().gameOptions == null || client.player == null || markers.isEmpty()) {
            return;
        }

        matrixStack.push();
        Vec3d cameraPos = camera.getPos();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.defaultBlendFunc();

        java.util.List<OnGroundMarker> opaqueMarkers = markers.stream().filter(m -> !m.seeThrough).collect(Collectors.toList());
        java.util.List<OnGroundMarker> seeThroughMarkers = markers.stream().filter(m -> m.seeThrough).collect(Collectors.toList());

        if (!opaqueMarkers.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            drawMarkers(matrixStack, opaqueMarkers, cameraPos, buffer);
            RenderSystem.enableDepthTest();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        if (!seeThroughMarkers.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            drawMarkers(matrixStack, seeThroughMarkers, cameraPos, buffer);
            RenderSystem.disableDepthTest();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableDepthTest();
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrixStack.pop();
    }

    // 绘制一组 OnGroundMarker 的核心逻辑
    private static void drawMarkers(MatrixStack matrixStack, java.util.List<OnGroundMarker> markers, Vec3d cameraPos, BufferBuilder buffer) {
        for (OnGroundMarker marker : markers) {
            float size = 0.8f;
            float x = (float) (marker.pos.toCenterPos().x - cameraPos.getX());
            float y = (float) (marker.pos.getY() - cameraPos.getY());
            float z = (float) (marker.pos.toCenterPos().z - cameraPos.getZ());
            float minX = -(size / 2);
            float maxX = (size / 2);
            float minZ = -(size / 2);
            float maxZ = (size / 2);

            float red = marker.color.getRed() / 255f;
            float green = marker.color.getGreen() / 255f;
            float blue = marker.color.getBlue() / 255f;
            float alpha = marker.alpha;

            matrixStack.push();
            matrixStack.translate(x, y, z);
            Matrix4f modelViewMatrix = matrixStack.peek().getPositionMatrix();

            // 绘制四边形
            buffer.vertex(modelViewMatrix, minX, 1.0001f, minZ).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, maxX, 1.0001f, minZ).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, maxX, 1.0001f, maxZ).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, minX, 1.0001f, maxZ).color(red, green, blue, alpha);

            matrixStack.pop();
        }
    }
}
