package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.rendering.ShapeRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;

public class LineShape {
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
    public static void draw(MatrixStack matrixStack, Vec3d start, Vec3d end, Color color, float alpha, boolean seeThrough) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
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

            buffer.vertex(modelViewMatrix, 0.0F, 0.0F, 0.0F)
                    .color(red, green, blue, alpha)
                    .normal(normalX, normalY, normalZ);
            buffer.vertex(modelViewMatrix, (float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z))
                    .color(red, green, blue, alpha)
                    .normal(normalX, normalY, normalZ);

            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(2f);
            RenderSystem.getShaderLineWidth();
            if(seeThrough)
                RenderSystem.disableDepthTest();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            matrixStack.pop();
        }
    }
}
