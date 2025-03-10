package mypals.ml.features.ImageRendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import mypals.ml.features.ImageRendering.configuration.ImageEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.*;

import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static mypals.ml.Lucidity.MOD_ID;
import static mypals.ml.features.ImageRendering.ImageDataParser.requestIdentifier;

public class ImageRenderer {
    public static PictureShader pictureShader = PictureShader.PosColTexLight;
    static Map<RenderLayer, BufferBuilder> BufferBuilderMap = new Reference2ObjectArrayMap<>(RenderLayer.getBlockLayers().size());

    public enum PictureShader {
        PosColTexLight(GameRenderer::getPositionColorTexLightmapProgram),
        RenderTypeCutout(GameRenderer::getRenderTypeCutoutProgram),
        PosTex(GameRenderer::getPositionTexProgram),
        PosTexCol(GameRenderer::getPositionTexColorProgram);

        PictureShader(Supplier<ShaderProgram> program) {
            this.program = program;
        }
        public final Supplier<ShaderProgram> program;
    }
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
    public static void renderPictureWorldSpace(MatrixStack matrixStack, ImageEntry imageEntry, Vec3d pos, Vec3d rotation, Vector2d scale, float pixelsPerBlock, int light, int overlay, float tickDelta, boolean disableDepthTest) throws IOException {

        renderPictureWorldSpace(matrixStack,requestIdentifier(imageEntry),pos,rotation,scale,pixelsPerBlock,light,overlay,tickDelta,disableDepthTest);

    }
    public static void renderPicture(MatrixStack matrixStack, ImageEntry imageEntry, Vec3d pos, Vec3d rotation, Vector2d scale, float pixelsPerBlock, int light, int overlay, float tickDelta, boolean disableDepthTest) throws IOException {

        renderPicture(matrixStack,requestIdentifier(imageEntry),pos,rotation,scale,pixelsPerBlock,light,overlay,tickDelta,disableDepthTest);

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

        //RenderSystem.setShader(isIrisShaderUsed()?ImageRenderer.pictureShader.program : GameRenderer::getPositionColorTexLightmapProgram);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);

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
        //matrixStack.scale(scale.x, scale.y, 1);
        matrixStack.multiply(
                new Quaternionf()
                        .rotateX((float)Math.toRadians( rotation.x))
                        .rotateY((float) Math.toRadians(rotation.y))
                        .rotateZ((float) Math.toRadians(rotation.z))
        );

        Vector3f normal = new Vector3f(0, 0, 1);

        // 旋转矩阵 (例如：绕Y轴旋转45度)
        Matrix3f rotationMatrix = new Matrix3f().rotateX((float)Math.toRadians( rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z));

        // 计算旋转后的法线
        Vector3f rotatedNormal = rotationMatrix.transform(normal);


        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        //BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        //BufferBuilder buffer = beginBufferBuilding(map,MinecraftClient.getInstance().getBufferBuilders().getBlockBufferBuilders(),RenderLayer.getSolid());
        buffer.vertex(matrix, -centerX, -centerY, 0.0f).color(255,255,255,255).texture(0.0f, 1.0f).light(light).overlay(overlay);
        buffer.vertex(matrix, centerX, -centerY, 0.0f).color(255,255,255,255).texture(1.0f, 1.0f).light(light).overlay(overlay);
        buffer.vertex(matrix, centerX, centerY, 0.0f).color(255,255,255,255).texture(1.0f, 0.0f).light(light).overlay(overlay);
        buffer.vertex(matrix, -centerX, centerY, 0.0f).color(255,255,255,255).texture(0.0f, 0.0f).light(light).overlay(overlay);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        matrixStack.pop();
    }/*private static BufferBuilder beginBufferBuilding(Map<RenderLayer, BufferBuilder> builders, BlockBufferAllocatorStorage allocatorStorage, RenderLayer layer) {
        BufferBuilder bufferBuilder = (BufferBuilder)builders.get(layer);
        if (bufferBuilder == null) {
            BufferAllocator bufferAllocator = Tessellator.getInstance().allocator;
            bufferBuilder = new BufferBuilder(bufferAllocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
            builders.put(layer, bufferBuilder);
        }

        return bufferBuilder;
    }*/
    static VertexSorter getBlockBufferAllocatorStorage(Vec3d pos) {
        Vec3d vec3d = MinecraftClient.getInstance().player.getCameraPosVec(0);
        return VertexSorter.byDistance(
                (float)(vec3d.x - (float)pos.getX()), (float)(vec3d.y - (double)pos.getY()), (float)(vec3d.z - (double)pos.getZ())
        );
    }

}