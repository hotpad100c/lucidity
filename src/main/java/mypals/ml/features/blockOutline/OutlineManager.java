package mypals.ml.features.blockOutline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.rendering.ShapeRender;
import mypals.ml.rendering.shapes.CubeShape;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static mypals.ml.Lucidity.LOGGER;
import static mypals.ml.Lucidity.MOD_ID;

public class OutlineManager {
    private static PostEffectProcessor shaderEffect;
    private static int framebufferWidth = -1;
    private static int framebufferHeight = -1;
    private static final Tessellator tessellator = new Tessellator(256);
    //private static final Identifier SHADER_RESOURCE = Identifier.ofVanilla( "shaders/post/entity_outline.json");
    //private static final Identifier SHADER_RESOURCE = Identifier.of(MOD_ID, "shaders/post/outline.json");
    private static final Identifier SHADER_RESOURCE = Identifier.of(MOD_ID,"shaders/post/outline.json");
    private static Map<BlockPos, BlockState> blockToRenderer = new HashMap<>();
    public static ArrayList<BlockPos> targetedBlocks = new ArrayList<>();

    public static void init() {
        if (shaderEffect != null) {
            shaderEffect.close();
        }

        framebufferWidth = framebufferHeight = -1;

        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            Framebuffer framebuffer = mc.getFramebuffer();
            TextureManager textureManager = mc.getTextureManager();
            ResourceManager resourceManager = mc.getResourceManager();

            shaderEffect = new PostEffectProcessor(textureManager, resourceManager, framebuffer, SHADER_RESOURCE);
        } catch (IOException e) {
            LOGGER.warn("Failed to load shader: {}", SHADER_RESOURCE, e);
            shaderEffect = null;
        }
    }
    public static void onRenderWorldLast(MatrixStack matrixStack){
        if(shaderEffect == null)return;
        if (blockToRenderer.isEmpty()) return;

        MinecraftClient mc = MinecraftClient.getInstance();

        Window mainWindow = mc.getWindow();
        int width = mainWindow.getFramebufferWidth();
        int height = mainWindow.getFramebufferHeight();
        if (width != framebufferWidth || height != framebufferHeight) {
            framebufferWidth = width;
            framebufferHeight = height;
            shaderEffect.setupDimensions(width, height);
        }



        BlockRenderManager dispatcher = mc.getBlockRenderManager();
        Camera view = mc.gameRenderer.getCamera();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        /*RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 1.0F);

        for (PostEffectPass pass : shaderEffect.passes) {
            if (pass.getName().equals("block_outline")) {
                JsonEffectShaderProgram program = pass.getProgram();
                Uniform colorUniform = program.getUniformByName("ColorModulator");

                if (colorUniform != null) {
                    colorUniform.set(1.0F, 0.0F, 0.0F, 1.0F);
                }
            }
        }*/



        Framebuffer framebuffer = shaderEffect.getSecondaryTarget("final");
        framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        //framebuffer.setClearColor(1,1,1,1);
        framebuffer.beginWrite(false);

        // render to framebuffer
        for (var entry : blockToRenderer.entrySet()) {
            var blockPos = entry.getKey();
            var blockState = entry.getValue();
            var model = dispatcher.getModel(blockState);

            /*matrixStack.push();
            matrixStack.translate(-view.getPos().x, -view.getPos().y, -view.getPos().z);
            matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());



           dispatcher.getModelRenderer().render(
                    matrixStack.peek(), bufferBuilder, blockState, model,
                    1.0F, 1.0F,1.0F, LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV);

            matrixStack.pop();*/
            CubeShape.draw(matrixStack,blockPos,0f,0, Color.RED,1,true);
        }
        //BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        //BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        shaderEffect.render(0);

        mc.getFramebuffer().beginWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1,1,1,1);
        framebuffer.draw(width, height, false);
        //clean up
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

    }
    public static void resolveBlocks(){
        World world = MinecraftClient.getInstance().world;
        blockToRenderer.clear();
        for(BlockPos pos : targetedBlocks){
            blockToRenderer.put(pos,world.getBlockState(pos));
        }
    }

}
