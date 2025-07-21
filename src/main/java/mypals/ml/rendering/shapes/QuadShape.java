package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.stream.Collectors;

public class QuadShape {
    public Vec3d c1;
    public Vec3d c2;
    public Color color;
    public float alpha;
    public boolean seeThrough;
    public QuadShape(Vec3d c1,Vec3d c2, Color color, float alpha, boolean seeThrough) {
        this.c1 = c1;
        this.c2 = c2;
        this.color = color;
        this.alpha = alpha;
        this.seeThrough = seeThrough;
    }
    public static void drawMultiple(MatrixStack matrixStack, java.util.List<QuadShape> quads) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (!camera.isReady() || client.getEntityRenderDispatcher().gameOptions == null || client.player == null || quads.isEmpty()) {
            return;
        }

        matrixStack.push();
        Vec3d cameraPos = camera.getPos();

        //RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager._enableBlend();
        GlStateManager._disableCull();


        java.util.List<QuadShape> opaqueQuads = quads.stream().filter(m -> !m.seeThrough).collect(Collectors.toList());
        java.util.List<QuadShape> seeThroughQuads = quads.stream().filter(m -> m.seeThrough).collect(Collectors.toList());

        if (!opaqueQuads.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            drawQuads(matrixStack, opaqueQuads, cameraPos, buffer);
            GlStateManager._enableDepthTest();
            RenderLayer.getDebugQuads().draw(buffer.end());
        }

        if (!seeThroughQuads.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            drawQuads(matrixStack, seeThroughQuads, cameraPos, buffer);
            GlStateManager._disableDepthTest();
            RenderLayer.getDebugQuads().draw(buffer.end());
            GlStateManager._enableDepthTest();
        }

        GlStateManager._enableCull();
        GlStateManager._disableBlend();
        matrixStack.pop();
    }

    private static void drawQuads(MatrixStack matrixStack, java.util.List<QuadShape> quads, Vec3d cameraPos, BufferBuilder buffer) {
        for (QuadShape quad : quads) {
            float x = (float) (quad.c1.x - cameraPos.getX());
            float y = (float) (quad.c1.y - cameraPos.getY());
            float z = (float) (quad.c1.z - cameraPos.getZ());

            float minX = (float) quad.c1.x;
            float maxX = (float) quad.c2.x;
            float minY = (float) quad.c1.y;
            float maxY = (float) quad.c2.y;
            float minZ = (float) quad.c1.z;
            float maxZ = (float) quad.c2.z;

            float red = quad.color.getRed() / 255f;
            float green = quad.color.getGreen() / 255f;
            float blue = quad.color.getBlue() / 255f;
            float alpha = quad.alpha;

            matrixStack.push();
            matrixStack.translate(x, y, z);
            Matrix4f modelViewMatrix = matrixStack.peek().getPositionMatrix();

            buffer.vertex(modelViewMatrix, minX, minY, minZ).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, maxX, minY, minZ).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, maxX, maxY, maxZ).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, minX, maxY, maxZ).color(red, green, blue, alpha);

            matrixStack.pop();
        }
    }
}
