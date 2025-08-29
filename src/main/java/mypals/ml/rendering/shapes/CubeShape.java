package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public static void drawCubes(MatrixStack matrices, Map<BlockPos,CubeShape> cubes, float sizeAdd, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (!camera.isReady() || client.player == null) {
            return;
        }

        matrices.push();
        Vec3d cameraPos = camera.getPos();
        float lastTickPosX = (float) cameraPos.getX();
        float lastTickPosY = (float) cameraPos.getY();
        float lastTickPosZ = (float) cameraPos.getZ();


        //RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager._enableBlend();


        Set<BlockPos> cubePositions = cubes.keySet();

        java.util.List<CubeShape> opaqueCubes = cubes.values().stream().filter(cube -> !cube.seeThrough).collect(Collectors.toList());
        java.util.List<CubeShape> seeThroughCubes = cubes.values().stream().filter(cube -> cube.seeThrough).collect(Collectors.toList());

        if (!opaqueCubes.isEmpty()) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            drawCubes(bufferBuilder, matrices, opaqueCubes, sizeAdd, tickDelta, cameraPos, lastTickPosX, lastTickPosY, lastTickPosZ, cubePositions);
            GlStateManager._enableDepthTest();
            if(IrisApi.getInstance().isShaderPackInUse()){
                RenderLayer.getDebugFilledBox().draw(bufferBuilder.end());
            }else{
                RenderLayer.getDebugQuads().draw(bufferBuilder.end());
            }
        }

        if (!seeThroughCubes.isEmpty()) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            drawCubes(bufferBuilder, matrices, seeThroughCubes, 0, tickDelta, cameraPos, lastTickPosX, lastTickPosY, lastTickPosZ, cubePositions);
            GlStateManager._disableDepthTest();
            if(IrisApi.getInstance().isShaderPackInUse()){
                RenderLayer.getDragonRays().draw(bufferBuilder.end());
            }else{
                RenderLayer.getDebugQuads().draw(bufferBuilder.end());
            }
            GlStateManager._enableDepthTest();
        }

        GlStateManager._disableBlend();
        matrices.pop();
    }

    private static void drawCubes(BufferBuilder bufferBuilder, MatrixStack matrices, java.util.List<CubeShape> cubes, float sizeAdd, float tickDelta,
                                  Vec3d cameraPos, float lastTickPosX, float lastTickPosY, float lastTickPosZ, Set<BlockPos> cubePositions) {
        float minOffset = -0.001f - sizeAdd;
        float maxOffset = 1.001f + sizeAdd;

        for (CubeShape cube : cubes) {
            float x = (float) (cube.pos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, cameraPos.getX()));
            float y = (float) (cube.pos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, cameraPos.getY()));
            float z = (float) (cube.pos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, cameraPos.getZ()));

            matrices.push();
            matrices.translate(x, y, z);
            Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();

            float red = ((cube.color.getRGB() >> 16) & 0xFF) / 255.0f;
            float green = ((cube.color.getRGB() >> 8) & 0xFF) / 255.0f;
            float blue = (cube.color.getRGB() & 0xFF) / 255.0f;

            BlockPos pos = cube.pos;
            boolean hasUp = cubePositions.contains(pos.up());
            boolean hasDown = cubePositions.contains(pos.down());
            boolean hasNorth = cubePositions.contains(pos.north());
            boolean hasSouth = cubePositions.contains(pos.south());
            boolean hasWest = cubePositions.contains(pos.west());
            boolean hasEast = cubePositions.contains(pos.east());

            if (!hasUp) {
                bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, cube.alpha);
            }

            if (!hasDown) {
                bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, cube.alpha);
            }

            if (!hasWest) {
                bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, cube.alpha);
            }

            if (!hasEast) {
                bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, cube.alpha);
            }

            if (!hasNorth) {
                bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, cube.alpha);
            }


            if (!hasSouth) {
                bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, cube.alpha);
                bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, cube.alpha);
            }

            matrices.pop();
        }

    }
    public static void drawSingle(MatrixStack matrices,BlockPos pos, float sizeAdd, float tickDelta,Color color,float alpha, boolean seeThrough) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (!camera.isReady() || client.player == null) {
            return;
        }

        matrices.push();
        Vec3d cameraPos = camera.getPos();
        float x = (float) (pos.getX() - MathHelper.lerp(tickDelta, cameraPos.getX(), cameraPos.getX()));
        float y = (float) (pos.getY() - MathHelper.lerp(tickDelta, cameraPos.getY(), cameraPos.getY()));
        float z = (float) (pos.getZ() - MathHelper.lerp(tickDelta, cameraPos.getZ(), cameraPos.getZ()));

        matrices.translate(x, y, z);
        Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();

        //RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager._enableBlend();

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        float minOffset = -0.001f - sizeAdd;
        float maxOffset = 1.001f + sizeAdd;

        float red = ((color.getRGB() >> 16) & 0xFF) / 255.0f;
        float green = ((color.getRGB() >> 8) & 0xFF) / 255.0f;
        float blue = (color.getRGB() & 0xFF) / 255.0f;

        bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, alpha);

        bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, alpha);

        bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, alpha);

        bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, alpha);

        bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, minOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, minOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, minOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, minOffset).color(red, green, blue, alpha);

        bufferBuilder.vertex(modelViewMatrix, minOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, maxOffset, maxOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, maxOffset, minOffset, maxOffset).color(red, green, blue, alpha);
        bufferBuilder.vertex(modelViewMatrix, minOffset, minOffset, maxOffset).color(red, green, blue, alpha);


        if (seeThrough) GlStateManager._disableDepthTest();
        if(IrisApi.getInstance().isShaderPackInUse()){
            RenderLayer.getDragonRays().draw(bufferBuilder.end());
        }else{
            RenderLayer.getDebugQuads().draw(bufferBuilder.end());
        }
        GlStateManager._enableDepthTest();
        GlStateManager._disableBlend();
        matrices.pop();
    }
}
