package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import mypals.ml.rendering.ShapeRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class LineShape {
    private static double lastTickPosX,lastTickPosY,lastTickPosZ;
    public Vec3d start;
    public Vec3d end;
    public float alpha;
    public Color color;
    public boolean seeThrough;
    public LineShape(Vec3d start, Vec3d end, Color color, float alpha, boolean seeThrough) {
        this.start = start;
        this.end = end;
        this.alpha = alpha;
        this.color = color;
        this.seeThrough = seeThrough;
    }
    public static void drawLines(MatrixStack matrixStack, List<LineShape> lines) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (!camera.isReady() || client.getEntityRenderDispatcher().gameOptions == null || client.player == null) {
            return;
        }

        Vec3d cameraPos = camera.getPos();
        matrixStack.push();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager._enableBlend();
        RenderSystem.lineWidth(5f);

        List<LineShape> opaqueLines = lines.stream().filter(line -> !line.seeThrough).collect(Collectors.toList());
        List<LineShape> seeThroughLines = lines.stream().filter(line -> line.seeThrough).collect(Collectors.toList());

        if (!opaqueLines.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            for (LineShape line : opaqueLines) {
                lastTickPosX = camera.getPos().getX();
                lastTickPosY = camera.getPos().getY();
                lastTickPosZ = camera.getPos().getZ();
                float x = (float) (line.start.getX() - MathHelper.lerp(0, lastTickPosX, camera.getPos().getX()));
                float y = (float) (line.start.getY() - MathHelper.lerp(0, lastTickPosY, camera.getPos().getY()));
                float z = (float) (line.start.getZ() - MathHelper.lerp(0, lastTickPosZ, camera.getPos().getZ()));

                float red = ((line.color.getRGB() >> 16) & 0xFF) / 255.0f;
                float green = ((line.color.getRGB() >> 8) & 0xFF) / 255.0f;
                float blue = (line.color.getRGB() & 0xFF) / 255.0f;

                matrixStack.push();
                matrixStack.translate(x, y, z);
                Matrix4f modelViewMatrix = matrixStack.peek().getPositionMatrix();

                buffer.vertex(modelViewMatrix, 0.0f, 0.0f, 0.0f)
                        .color(red, green, blue, line.alpha);
                buffer.vertex(modelViewMatrix,
                                (float) (line.end.x - line.start.x),
                                (float) (line.end.y - line.start.y),
                                (float) (line.end.z - line.start.z))
                        .color(red, green, blue, line.alpha);

                matrixStack.pop();
            }

            GlStateManager._enableDepthTest();
            RenderLayer.getDebugLineStrip(2f).draw(buffer.end());
        }

        if (!seeThroughLines.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            for (LineShape line : seeThroughLines) {
                double x = line.start.getX() - cameraPos.getX();
                double y = line.start.getY() - cameraPos.getY();
                double z = line.start.getZ() - cameraPos.getZ();

                float red = ((line.color.getRGB() >> 16) & 0xFF) / 255.0f;
                float green = ((line.color.getRGB() >> 8) & 0xFF) / 255.0f;
                float blue = (line.color.getRGB() & 0xFF) / 255.0f;

                matrixStack.push();
                matrixStack.translate(x, y, z);
                Matrix4f modelViewMatrix = matrixStack.peek().getPositionMatrix();

                buffer.vertex(modelViewMatrix, 0.0f, 0.0f, 0.0f)
                        .color(red, green, blue, line.alpha);
                buffer.vertex(modelViewMatrix,
                                (float) (line.end.x - line.start.x),
                                (float) (line.end.y - line.start.y),
                                (float) (line.end.z - line.start.z))
                        .color(red, green, blue, line.alpha);

                matrixStack.pop();
            }

            GlStateManager._disableDepthTest();
            RenderLayer.getDebugLineStrip(2f).draw(buffer.end());
            GlStateManager._enableDepthTest();
        }

        // 清理渲染状态
        GlStateManager._disableBlend();
        matrixStack.pop();
    }
    public static void draw(MatrixStack matrixStack, Vec3d start, Vec3d end, Color color, float alpha, boolean seeThrough) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            double x =  (start.getX() - camera.getPos().getX());
            double y =  (start.getY() - camera.getPos().getY());
            double z =  (start.getZ() - camera.getPos().getZ());
            float red = ((color.getRGB() >> 16) & 0xFF) / 255.0f;
            float green = ((color.getRGB() >> 8) & 0xFF) / 255.0f;
            float blue = (color.getRGB() & 0xFF) / 255.0f;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            matrixStack.push();
            matrixStack.translate(x, y, z);
            Matrix4f modelViewMatrix = matrixStack.peek().getPositionMatrix();

            buffer.vertex(modelViewMatrix, 0.0F, 0.0F, 0.0F)
                    .color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, (float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z))
                    .color(red, green, blue, alpha);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager._enableBlend();
            RenderSystem.lineWidth(2f);
            if(seeThrough)
                GlStateManager._disableDepthTest();
            RenderLayer.getDebugLineStrip(2f).draw(buffer.end());
            GlStateManager._enableDepthTest();
            GlStateManager._disableBlend();
            matrixStack.pop();
        }
    }
}
