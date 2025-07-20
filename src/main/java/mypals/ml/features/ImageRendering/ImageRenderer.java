package mypals.ml.features.ImageRendering;

import com.mojang.blaze3d.systems.RenderSystem;

import mypals.ml.features.ImageRendering.configuration.MediaEntry;
import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.*;
import java.io.IOException;
import java.lang.Math;
import java.util.Optional;


import static mypals.ml.Lucidity.MOD_ID;
import static mypals.ml.features.ImageRendering.ImageDataParser.requestIdentifier;

public class ImageRenderer {

    public static void renderPictureWorldSpace(MatrixStack matrixStack, Identifier textureId, Vec3d pos, Vec3d rotation, Vector2d scale, float pixelsPerBlock, int light, int overlay, float tickDelta, boolean disableDepthTest) throws IOException {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (!(camera.isReady() && client.player != null)) return;

        double lastTickPosX = camera.getPos().getX();
        double lastTickPosY = camera.getPos().getY();
        double lastTickPosZ = camera.getPos().getZ();

        float x = (float) (pos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
        float y = (float) (pos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
        float z = (float) (pos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));

        renderPicture(matrixStack,textureId,new Vec3d(x,y,z),rotation,scale,pixelsPerBlock,light,overlay,tickDelta, disableDepthTest);

    }
    public static void renderPictureWorldSpace(MatrixStack matrixStack, MediaEntry mediaEntry, Vec3d pos, Vec3d rotation, Vector2d scale, float pixelsPerBlock, int light, int overlay, float tickDelta, boolean disableDepthTest) throws IOException {
        renderPictureWorldSpace(matrixStack,requestIdentifier(mediaEntry),pos,rotation,scale,pixelsPerBlock,light,overlay,tickDelta,disableDepthTest);
    }

    public static void renderPicture(MatrixStack matrixStack, Identifier textureId, Vec3d pos, Vec3d rotation, Vector2d scale, float pixelsPerBlock, int light, int overlay, float tickDelta, boolean disableDepthTest) throws IOException {
        ImageDataParser.mergeImages();

        MinecraftClient client = MinecraftClient.getInstance();

        float textureWidth = 16;
        float textureHeight = 16;

        ResourceManager resourceManager = client.getResourceManager();
        Optional<Resource> resourceOptional;
        try {
            AbstractTexture texture = client.getTextureManager().getTexture(textureId);
            if (texture instanceof NativeImageBackedTexture) {
                NativeImageBackedTexture nativeTexture = (NativeImageBackedTexture) texture;
                NativeImage image = nativeTexture.getImage();

                textureWidth = image.getWidth() / pixelsPerBlock;
                textureHeight = image.getHeight() / pixelsPerBlock;

                image.close();
            }try {
                Optional<NativeImage> imageOptional = client.getResourceManager()
                        .getResource(textureId)
                        .map(resource -> {
                            try (var inputStream = resource.getInputStream()) {
                                return NativeImage.read(inputStream);
                            } catch (IOException e) {
                                return null;
                            }
                        });
                if (imageOptional.isPresent()) {
                    NativeImage image = imageOptional.get();
                    textureWidth = image.getWidth() / pixelsPerBlock;
                    textureHeight = image.getHeight() / pixelsPerBlock;
                    image.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            resourceOptional = resourceManager.getResource(Identifier.of(MOD_ID,"textures/lost-file.png"));
            NativeImage image = NativeImage.read(resourceOptional.get().getInputStream());

            textureWidth = image.getWidth() / pixelsPerBlock;
            textureHeight = image.getHeight() / pixelsPerBlock;

            image.close();
        }
        float scaledWidth = (float) (textureWidth * scale.x);
        float scaledHeight = (float) (textureHeight * scale.y);

        float centerX = (float) (scaledWidth/2);
        float centerY = (float) (scaledHeight/2);

        Camera camera = client.gameRenderer.getCamera();
        if (!(camera.isReady() && client.player != null)) return;

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.setShaderColor(1,1,1,1);
        if(disableDepthTest)
            RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);

        RenderSystem.disableCull();

        matrixStack.push();

        matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
        matrixStack.multiply(
                new Quaternionf()
                        .rotateX((float)Math.toRadians( rotation.x))
                        .rotateY((float) Math.toRadians(rotation.y))
                        .rotateZ((float) Math.toRadians(rotation.z))
        );

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix, -centerX, -centerY, 0.0f).color(255,255,255,255).texture(0.0f, 1.0f).light(light).overlay(overlay);
        buffer.vertex(matrix, centerX, -centerY, 0.0f).color(255,255,255,255).texture(1.0f, 1.0f).light(light).overlay(overlay);
        buffer.vertex(matrix, centerX, centerY, 0.0f).color(255,255,255,255).texture(1.0f, 0.0f).light(light).overlay(overlay);
        buffer.vertex(matrix, -centerX, centerY, 0.0f).color(255,255,255,255).texture(0.0f, 0.0f).light(light).overlay(overlay);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        matrixStack.pop();
    }
}