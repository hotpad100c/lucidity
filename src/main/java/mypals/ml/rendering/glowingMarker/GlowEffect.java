package mypals.ml.rendering.glowingMarker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.time.ZoneOffset;
import java.util.Random;

import static mypals.ml.rendering.ShapeRender.*;

public class GlowEffect {
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);
    public static void renderGlowEffect(MatrixStack matrixStack, Vec3d pos, float size, RenderTickCounter counter, float time, int lights, float rotSpeed, float rotOffset,
                                        int colorR, int colorG, int colorB, int alpha,long seed) {
        Random random = new Random(seed);
        float time1 = time * rotSpeed * 0.3F;
        float time2 = time * 0.1F + random.nextInt();
        VertexConsumerProvider.Immediate immediate = getVertexConsumer();
        VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getDragonRays());
        Camera cam = MinecraftClient.getInstance().gameRenderer.getCamera();
        float scale = Math.min(size,GlowEffect.calculateScale(MinecraftClient.getInstance().getCameraEntity().getPos(),pos))*
                (0.9F + random.nextFloat() * 0.2F);
        float tickDelta = counter.getTickDelta(true);
        lastTickPosX = cam.getPos().getX();
        lastTickPosY = cam.getPos().getY();
        lastTickPosZ = cam.getPos().getZ();
        float x = (float) (pos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, cam.getPos().getX()));
        float y = (float) (pos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, cam.getPos().getY()));
        float z = (float) (pos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, cam.getPos().getZ()));
        
        matrixStack.push();
        matrixStack.translate(x,y,z);
        Quaternionf camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getRotation();
        matrixStack.multiply(camera);
        matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(rotOffset));

        for (int i = 0; i < lights; i++) {
            float length = (float) (7F + Math.sin(time2 + i * 2 + random.nextFloat())) * scale;
            float width = (float) (2F - 0.2F * Math.abs(Math.cos(time2 - i * Math.PI * 0.5F + random.nextFloat()))) * scale;

            matrixStack.push();
            Quaternionf quaternionf = new Quaternionf();
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

            drawVertex(vertexConsumer, matrix4f, positionMatrix, colorR, colorG, colorB, alpha, length, width,0);
            matrixStack.pop();
        }
        immediate.draw();
        matrixStack.pop();


    }


    private static void drawVertex(VertexConsumer vc, Matrix4f matrix4f, MatrixStack.Entry entry, int colorR, int colorG, int colorB, int alpha, float length, float width,float zOffset) {
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



}
