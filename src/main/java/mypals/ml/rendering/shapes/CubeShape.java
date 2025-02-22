package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.awt.*;

import static mypals.ml.rendering.ShapeRender.*;

public class CubeShape {
    public BlockPos pos;
    public float alpha;
    public Color color;

    public boolean seeThrough;
    public CubeShape(BlockPos pos, float alpha, Color color, boolean seeThrough) {
        this.pos = pos;
        this.alpha = alpha;
        this.color = color;
        this.seeThrough = seeThrough;
    }
    public void draw(MatrixStack matrices,float sizeAdd, float tickDelta) {
        draw(matrices, this.pos, sizeAdd, tickDelta, this.color,this.alpha,this.seeThrough);
    }
    public static void draw(MatrixStack matrices, BlockPos pos, float sizeAdd, float tickDelta, Color color, float alpha,boolean seeThrough) {
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

            if(seeThrough)
                RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            float minOffset = -0.001F - sizeAdd;
            float maxOffset = 1.001F + sizeAdd;

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

            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }
}
