package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.awt.*;

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
    public void draw(MatrixStack matrixStack){
        draw(matrixStack, this.pos, 0.8f, this.color.getRed() / 255f, this.color.getGreen() / 255f, this.color.getBlue() / 255f, this.alpha, this.seeThrough);
    }
    public static void draw(MatrixStack matrixStack, BlockPos pos, Color color, float alpha, boolean seeThrough){
        draw(matrixStack, pos, 0.8f, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha,seeThrough);
    }

    public static void draw(MatrixStack matrixStack, BlockPos pos, float size, float red, float green, float blue, float alpha, boolean seeThrough) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            float x = (float) (pos.toCenterPos().x - camera.getPos().getX());
            float y = (float) (pos.getY() - camera.getPos().getY());
            float z = (float) (pos.toCenterPos().z - camera.getPos().getZ());
            float minX = (float) -(size / 2);
            float maxX = (float) (size / 2);
            float minZ = (float) -(size / 2);
            float maxZ = (float) (size / 2);

            matrixStack.push();
            matrixStack.translate(x, y, z);
            Matrix4f modelViewMatrix = matrixStack.peek().getPositionMatrix();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            buffer.vertex(modelViewMatrix, minX, 1.0001f, minZ).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, maxX, 1.0001f, minZ).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, maxX, 1.0001f, maxZ).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, minX, 1.0001f, maxZ).color(red, green, blue, alpha);

            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.defaultBlendFunc();
            if(seeThrough)
                RenderSystem.disableDepthTest();

            BufferRenderer.drawWithGlobalProgram(buffer.end());

            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            matrixStack.pop();
        }
    }
}
