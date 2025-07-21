package mypals.ml.rendering.shapes;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

import static mypals.ml.rendering.InformationRender.isIrisShaderUsed;
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

    public ShineMarker(Vec3d pos, Color color, float size, float speed, int lights, long seed, boolean autoAlpha, boolean seeThrough) {
        this.pos = pos;
        this.color = color;
        this.size = size;
        this.speed = speed;
        this.lights = lights;
        this.seed = seed;
        this.autoAlpha = autoAlpha;
        this.seeThrough = seeThrough;
    }

    public void draw(MatrixStack matrixStack, float time, int alpha, Color color) {
        drawSingle(matrixStack, this, time,color);
    }

    private static void drawSingle(MatrixStack matrixStack, ShineMarker marker, float time, Color color) {
        drawMultiple(matrixStack, Collections.singletonList(marker), time, color);
    }
    public static void drawMultiple(MatrixStack matrixStack, java.util.List<ShineMarker> markers, float time, Color color) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera cam = client.gameRenderer.getCamera();
        if (!cam.isReady() || client.player == null || markers.isEmpty()) {
            return;
        }

        matrixStack.push();
        Vec3d cameraPos = cam.getPos();
        float lastTickPosX = (float) cameraPos.getX();
        float lastTickPosY = (float) cameraPos.getY();
        float lastTickPosZ = (float) cameraPos.getZ();



        //RenderLayer.setShader(ShaderProgramKeys.POSITION_COLOR);
        GlStateManager._enableBlend();
        GlStateManager._enableDepthTest();
        
        java.util.List<ShineMarker> opaqueMarkers = markers.stream().filter(m -> !m.seeThrough).collect(Collectors.toList());
        java.util.List<ShineMarker> seeThroughMarkers = markers.stream().filter(m -> m.seeThrough).collect(Collectors.toList());

        if (!opaqueMarkers.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            drawMarkers(matrixStack, opaqueMarkers, time, cameraPos, lastTickPosX, lastTickPosY, lastTickPosZ, buffer);
            GlStateManager._enableDepthTest();
            setShaderColor(client, false, color);
            RenderLayer.getDebugQuads().draw(buffer.end());
        }

        if (!seeThroughMarkers.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            drawMarkers(matrixStack, seeThroughMarkers, time, cameraPos, lastTickPosX, lastTickPosY, lastTickPosZ, buffer);
            GlStateManager._disableDepthTest();
            setShaderColor(client, true, color);
            RenderLayer.getDebugQuads().draw(buffer.end());
            GlStateManager._enableDepthTest();
        }

        GlStateManager._disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.pop();
    }

    private static void drawMarkers(MatrixStack matrixStack, java.util.List<ShineMarker> markers, float time,
                                    Vec3d cameraPos, float lastTickPosX, float lastTickPosY, float lastTickPosZ, BufferBuilder buffer) {
        Random random = new Random();
        Vector3f vector3f = new Vector3f(0, 0, 0);
        Vector3f vector3f2 = new Vector3f();
        Vector3f vector3f3 = new Vector3f();
        Vector3f vector3f4 = new Vector3f();
        Quaternionf quaternionf = new Quaternionf();

        for (ShineMarker marker : markers) {
            random.setSeed(marker.seed);
            float time1 = time * marker.speed * 0.05f;
            float scale = Math.min(marker.size, calculateScale(MinecraftClient.getInstance().getCameraEntity().getPos(), marker.pos)) *
                    (0.9f + random.nextFloat() * 0.2f);
            int alpha = marker.autoAlpha ? (int)(calculateAlpha(MinecraftClient.getInstance().cameraEntity.getPos(),
                    marker.pos, marker.lifeTime)*255) : 255;

            float x = (float) (marker.pos.getX() - MathHelper.lerp(0, lastTickPosX, cameraPos.getX()));
            float y = (float) (marker.pos.getY() - MathHelper.lerp(0, lastTickPosY, cameraPos.getY()));
            float z = (float) (marker.pos.getZ() - MathHelper.lerp(0, lastTickPosZ, cameraPos.getZ()));

            matrixStack.push();
            matrixStack.translate(x, y, z);

            for (int i = 0; i < marker.lights; i++) {
                matrixStack.push();
                quaternionf.rotationXYZ(
                        (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.sin(time1 * 0.1),
                        (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.cos(time1 * 0.1),
                        (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.sin(time1 * 0.2)
                ).rotateXYZ(
                        (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.cos(time1 * 0.15),
                        (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.sin(time1 * 0.15),
                        (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.cos(time1 * 0.2)
                );

                float length = random.nextFloat(0.6f, 1f) * 2.0f;
                float width = random.nextFloat(0.6f, 1f) * 6.0f;

                vector3f2.set(-HALF_SQRT_3 * length, width, -0.5f * length);
                vector3f3.set(HALF_SQRT_3 * length, width, -0.5f * length);
                vector3f4.set(0.0f, width, 0.5f * length);

                matrixStack.multiply(quaternionf);
                matrixStack.scale(scale, scale, scale);

                MatrixStack.Entry positionMatrix = matrixStack.peek();

                buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
                buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
                buffer.vertex(positionMatrix, vector3f2).color(marker.color.getRed(), marker.color.getGreen(), marker.color.getBlue(), 0).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
                buffer.vertex(positionMatrix, vector3f3).color(marker.color.getRed(), marker.color.getGreen(), marker.color.getBlue(), 0).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);

                buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
                buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
                buffer.vertex(positionMatrix, vector3f3).color(marker.color.getRed(), marker.color.getGreen(), marker.color.getBlue(), 0).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
                buffer.vertex(positionMatrix, vector3f4).color(marker.color.getRed(), marker.color.getGreen(), marker.color.getBlue(), 0).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);

                buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
                buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
                buffer.vertex(positionMatrix, vector3f4).color(marker.color.getRed(), marker.color.getGreen(), marker.color.getBlue(), 0).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
                buffer.vertex(positionMatrix, vector3f2).color(marker.color.getRed(), marker.color.getGreen(), marker.color.getBlue(), 0).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
                matrixStack.pop();
            }

            matrixStack.pop();
        }
    }

    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);

    private static void setShaderColor(MinecraftClient client, boolean seeThrough, Color color) {
        if (isIrisShaderUsed()) {
            RenderSystem.setShaderColor((float) color.getRed() /255, (float) color.getGreen() /255, (float) color.getBlue() /255, 1);
        } else {
            RenderSystem.setShaderColor(1, 1, 1, 1.0f);
        }
    }


    public static float calculateScale(Vec3d viewPos, Vec3d pos) {
        double maxDist = 500;
        double dist = Math.min(viewPos.distanceTo(pos), maxDist);
        float f = (float) Math.pow(Math.sin(dist / maxDist * Math.PI), 0.5f);
        return f * 3f;
    }

    public static float calculateAlpha(Vec3d viewPos, Vec3d pos, double lifeTime) {
        double a;
        double maxDist = 5.0;
        double dist = Math.min(viewPos.distanceTo(pos), maxDist);
        if (dist >= maxDist) {
            a = mapAlpha(lifeTime, 0, 30);
        } else {
            a = dist / maxDist;
        }
        return (float) a;
    }

    public static double mapAlpha(double x, float min, float max) {
        if (x < min) x = min;
        if (x > max) x = max;
        return (x - min) / (max - min);
    }
}
