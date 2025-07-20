package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;

import static mypals.ml.rendering.ShapeRender.getVertexConsumer;

public class BoxShape{
    public Vec3d pos;
    public float length;
    public float weigth;
    public float height;
    public float alpha;
    public Color color;
    public boolean seeThrough;
    public BoxShape(Vec3d pos, float length, float weigth, float height,Color color,float alpha,boolean seeThrough){
        this.pos = pos;
        this.length = length;
        this.weigth = weigth;
        this.height = height;
        this.color = color;
        this.alpha = alpha;
        this.seeThrough = seeThrough;
    }
    public void draw(MatrixStack matrices) {
        draw(matrices, this.pos, this.length, this.weigth, this.height,0, this.color, this.alpha, this.seeThrough);
    }
    public static void draw(MatrixStack matrices, Vec3d pos, float length, float width, float height, float tickDelta, Color color, float alpha,boolean seeThrough) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            matrices.push();
            double lastTickPosX = camera.getPos().getX();
            double lastTickPosY = camera.getPos().getY();
            double lastTickPosZ = camera.getPos().getZ();

            float x = (float) (pos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
            float y = (float) (pos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
            float z = (float) (pos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));

            matrices.translate(x, y, z);
            Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            float xMin = -length / 2;
            float xMax = length / 2;
            float yMin = -height / 2;
            float yMax = height / 2;
            float zMin = -width / 2;
            float zMax = width / 2;

            float red = ((color.getRGB() >> 16) & 0xFF) / 255.0f;
            float green = ((color.getRGB() >> 8) & 0xFF) / 255.0f;
            float blue = (color.getRGB() & 0xFF) / 255.0f;


            buffer.vertex(modelViewMatrix, xMin, yMax, zMin).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMax, zMin).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMax, zMax).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMin, yMax, zMax).color(red, green, blue, alpha);


            buffer.vertex(modelViewMatrix, xMin, yMin, zMax).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMin, zMax).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMin, zMin).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMin, yMin, zMin).color(red, green, blue, alpha);


            buffer.vertex(modelViewMatrix, xMin, yMax, zMin).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMin, yMax, zMax).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMin, yMin, zMax).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMin, yMin, zMin).color(red, green, blue, alpha);


            buffer.vertex(modelViewMatrix, xMax, yMin, zMin).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMin, zMax).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMax, zMax).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMax, zMin).color(red, green, blue, alpha);


            buffer.vertex(modelViewMatrix, xMin, yMin, zMin).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMin, zMin).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMax, zMin).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMin, yMax, zMin).color(red, green, blue, alpha);


            buffer.vertex(modelViewMatrix, xMin, yMax, zMax).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMax, zMax).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMax, yMin, zMax).color(red, green, blue, alpha);
            buffer.vertex(modelViewMatrix, xMin, yMin, zMax).color(red, green, blue, alpha);


            if(seeThrough)
                RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.disableCull();

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            matrices.pop();
        }
    }
}
