package mypals.ml.features.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Set;


public class StringRenderer {
    public static double lastTickPosX = 0;
    public static double lastTickPosY = 0;
    public static double lastTickPosZ = 0;
    public static void renderText(MatrixStack matrixStack,RenderTickCounter counter,BlockPos pos, String text, int color, float SIZE, boolean seeThrow)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        //Vec3d textPos = new Vec3d(0, 0, 0);
        Vec3d textPos = new Vec3d(pos.toCenterPos().toVector3f());
        drawString(counter, camera, textPos, text, color, SIZE, seeThrow);
    }
    public static void renderText(MatrixStack matrixStack,RenderTickCounter counter,Vec3d pos, String text, int color, float SIZE, boolean seeThrow)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Vec3d textPos = pos;
        drawString(counter, camera, textPos, text, color, SIZE, seeThrow);
    }
    public static void drawString(RenderTickCounter tickCounter, Camera camera, Vec3d textPos, String text, int color, float SIZE, boolean seeThrow) {

        Matrix4fStack modelViewMatrix = new Matrix4fStack(1);
        modelViewMatrix.identity();

        float tickDelta = tickCounter.getTickDelta(false);
        float x = (float) (textPos.x - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
        float y = (float) (textPos.y - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
        float z = (float) (textPos.z - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));
        lastTickPosX = camera.getPos().getX();
        lastTickPosY = camera.getPos().getY();
        lastTickPosZ = camera.getPos().getZ();
        modelViewMatrix.translate(x, y, z);
        modelViewMatrix.rotate(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
        modelViewMatrix.scale(SIZE, -SIZE, SIZE);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float totalWidth = textRenderer.getWidth(text);
        float writtenWidth = 1;
        float renderX = -totalWidth * 0.5F + writtenWidth;

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.disableDepthTest();

        if(seeThrow)
            textRenderer.draw(text, renderX, 0, color, false, modelViewMatrix
                , immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        else
            textRenderer.draw(text, renderX, 0, color, false, modelViewMatrix
                , immediate, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);

        RenderSystem.enableDepthTest();

    }

}
