package mypals.ml.features.AdvancedContentRender;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static mypals.ml.rendering.ShapeRender.getVertexConsumer;

public class ImageRenderer {
    public static double lastTickPosX = 0;
    public static double lastTickPosY = 0;
    public static double lastTickPosZ = 0;

    public static void renderPicture(MatrixStack matrixStack, Identifier textureId, Vec3d pos, float scaleFactor, int light, int overlay,float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        AbstractTexture texture = client.getTextureManager().getTexture(textureId);
        if (texture == null) {
            System.out.println("Texture not found: " + textureId);
        }
        // 确保纹理存在且有效
        if (!(texture instanceof NativeImageBackedTexture nativeTexture)) {
            return;
        }

        // 获取纹理的宽高
        int textureWidth = nativeTexture.getImage().getWidth();
        int textureHeight = nativeTexture.getImage().getHeight();

        // 计算目标缩放尺寸
        float scaledWidth = textureWidth * scaleFactor;
        float scaledHeight = textureHeight * scaleFactor;

        Camera camera = client.gameRenderer.getCamera();
        if (!(camera.isReady() && client.player != null)){return;}
        lastTickPosX = camera.getPos().getX();
        lastTickPosY = camera.getPos().getY();
        lastTickPosZ = camera.getPos().getZ();
        float x = (float) (pos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
        float y = (float) (pos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
        float z = (float) (pos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));



        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, textureId);

        matrixStack.translate(x, y, z);
        matrixStack.scale(scaleFactor, scaleFactor, 1);
        matrixStack.push();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);

        buffer.vertex(matrix, 0.0f, 0.0f, 0.0f).color(255, 255, 255, 255).texture(0.0f, 1.0f).light(light).overlay(overlay);
        buffer.vertex(matrix, scaledWidth, 0.0f, 0.0f).color(255, 255, 255, 255).texture(1.0f, 1.0f).light(light).overlay(overlay);
        buffer.vertex(matrix, scaledWidth, scaledHeight, 0.0f).color(255, 255, 255, 255).texture(1.0f, 0.0f).light(light).overlay(overlay);
        buffer.vertex(matrix, 0.0f, scaledHeight, 0.0f).color(255, 255, 255, 255).texture(0.0f, 0.0f).light(light).overlay(overlay);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        matrixStack.pop();
    }



}