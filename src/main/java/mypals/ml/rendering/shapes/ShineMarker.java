package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.*;
import java.util.Random;

import static mypals.ml.rendering.ShapeRender.*;

public class ShineMarker {
    public Vec3d pos;
    public Boolean autoAlpha;
    public Color color;
    public float size;
    public long seed;
    public int lights;
    public float speed;
    public int lifeTime;
    public boolean seeThrough;
    public ShineMarker(Vec3d pos, Color color, float size, float speed, int lights,long seed,boolean autoAlpha,boolean seeThrough) {
        this.pos = pos;
        this.color = color;
        this.size = size;
        this.speed = speed;
        this.lights = lights;
        this.seed = seed;
        this.autoAlpha = autoAlpha;
        this.seeThrough = seeThrough;
    }
    public void draw(MatrixStack matrixStack,float time,int alpha,boolean seeThrough) {
        draw(matrixStack, this.pos, this.size, 0, time, this.lights, this.speed,2,
                this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.autoAlpha?alpha:255, this.seed,seeThrough);
    }
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);
    public static void draw(MatrixStack matrixStack, Vec3d pos, float size, float tickDelta, float time, int lights, float rotSpeed, float rotOffset,
                                        int colorR, int colorG, int colorB, int alpha, long seed,boolean seeThrough) {
        Random random = new Random(seed);
        float time1 = time * rotSpeed * 0.3F;
        float time2 = time * 0.1F + random.nextInt();
        //VertexConsumerProvider.Immediate immediate = getVertexConsumer();
        //VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getDragonRays());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        Camera cam = MinecraftClient.getInstance().gameRenderer.getCamera();
        float scale = Math.min(size, calculateScale(MinecraftClient.getInstance().getCameraEntity().getPos(),pos))*
                (0.9F + random.nextFloat() * 0.2F);
        lastTickPosX = cam.getPos().getX();
        lastTickPosY = cam.getPos().getY();
        lastTickPosZ = cam.getPos().getZ();
        float x = (float) (pos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, cam.getPos().getX()));
        float y = (float) (pos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, cam.getPos().getY()));
        float z = (float) (pos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, cam.getPos().getZ()));

        matrixStack.push();
        matrixStack.translate(x,y,z);
        Quaternionf camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getRotation();
        //matrixStack.multiply(camera);
        //matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(rotOffset));

        if(seeThrough)
            RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        for (int i = 0; i < lights; i++) {
            float length = (float) (7F + Math.sin(time2 + i * 2 + random.nextFloat())) * scale;
            float width = (float) (2F - 0.2F * Math.abs(Math.cos(time2 - i * Math.PI * 0.5F + random.nextFloat()))) * scale;

            matrixStack.push();
            Quaternionf quaternionf = new Quaternionf();
            //matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(rotOffset));
            quaternionf
                    .rotationXYZ(
                            random.nextFloat() * (float) (Math.PI * 2),
                            random.nextFloat() * (float) (Math.PI * 2),
                            random.nextFloat() * (float) (Math.PI * 2)
                    ).rotateXYZ(
                            random.nextFloat() * (float) (Math.PI * 2),
                            random.nextFloat() * (float) (Math.PI * 2),
                            random.nextFloat() * (float) (Math.PI * 2)
                    );

            matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(time1 - (i / (float) lights * 360)));
            matrixStack.multiply(quaternionf);
            MatrixStack.Entry positionMatrix = matrixStack.peek();
            Matrix4f matrix4f = positionMatrix.getPositionMatrix();

            drawVertex(buffer, matrix4f, positionMatrix, colorR, colorG, colorB, alpha, length, width,0);
            matrixStack.pop();
        }

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        if(seeThrough)
            RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        matrixStack.pop();
    }


    private static void drawVertex(BufferBuilder vc, Matrix4f matrix4f, MatrixStack.Entry entry, int colorR, int colorG, int colorB, int alpha, float length, float width,float zOffset) {
        float u = 0.1F;
        float v = 0.1F;
        // Origin Vertex
        vc.vertex(matrix4f, 0.0F, 0.0F, 0.0F)
                .color(255, 255, 255, alpha)
                .texture(u, v)
                .overlay(0)
                .light(240)
                .normal(entry, 0.0F, 1.0F, 0.0F);

        // Left Corner Vertex
        vc.vertex(matrix4f, -HALF_SQRT_3 * width, length, zOffset)
                .color(colorR, colorG, colorB, 0)
                .texture(u - 0.5F, v + 1.0F)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(240)
                .normal(entry, 0.0F, -1.0F, 0.0F);

        // Right Corner Vertex
        vc.vertex(matrix4f, HALF_SQRT_3 * width, length, zOffset)
                .color(colorR, colorG, colorB, 0)
                .texture(u + 0.5F, v + 1.0F)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(240)
                .normal(entry, 0.0F, -1.0F, 0.0F);
    }
    public static float calculateScale(Vec3d viewPos,Vec3d pos) {
        double maxDist = 200;
        double dist = Math.min(viewPos.distanceTo(pos), maxDist);
        float f = (float) Math.pow(Math.sin(dist / maxDist * Math.PI), 0.5F);
        return f * 3F;
    }
    public static float calculateAlpha(Vec3d viewPos,Vec3d pos,double lifeTime){
        double a;
        double maxDist = 5.0;
        double dist = Math.min(viewPos.distanceTo(pos), maxDist);
        if (dist >= maxDist) {
            a = mapAlpha(lifeTime, 0, 30);
        } else {
            a = dist / maxDist;
        }
        return (float)a;
    }
    public static double mapAlpha(double x, float min, float max) {
        if (x < min) x = min;
        if (x > max) x = max;

        return (x - min) / (max - min);
    }
}
