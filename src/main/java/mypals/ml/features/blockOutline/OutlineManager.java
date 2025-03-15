package mypals.ml.features.blockOutline;

import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.atlas.AtlasSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static mypals.ml.Lucidity.LOGGER;
import static mypals.ml.rendering.InformationRender.isSodiumUsed;

public class OutlineManager {
    public static Map<BlockPos, BlockState> blockToRenderer = new HashMap<>();
    public static ArrayList<BlockPos> targetedBlocks = new ArrayList<>();

    public static void init() {

    }
    public static void onRenderWorldLast(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockRenderManager dispatcher = mc.getBlockRenderManager();
        BlockEntityRenderDispatcher blockEntityRenderer = mc.getBlockEntityRenderDispatcher();

        Camera camera = context.camera();
        OutlineVertexConsumerProvider outlineVertexConsumerProvider = mc.worldRenderer.bufferBuilders.getOutlineVertexConsumers();
        MatrixStack matrixStack = context.matrixStack();

        if (matrixStack == null) {
            System.err.println("MatrixStack is null, skipping render.");
            return;
        }

        outlineVertexConsumerProvider.setColor(255, 0, 255, 255);

        float delta = context.tickCounter().getTickDelta(false);
        double x, y, z;
        double lastTickPosX,lastTickPosY,lastTickPosZ;

        for (var entry : blockToRenderer.entrySet()) {
            BlockPos blockPos = entry.getKey();
            BlockState blockState = entry.getValue();
            boolean shouldRender = !blockState.isAir();
            if(!shouldRender) continue;
            lastTickPosX = camera.getPos().getX();
            lastTickPosY = camera.getPos().getY();
            lastTickPosZ = camera.getPos().getZ();

            x = (blockPos.getX() - MathHelper.lerp(delta, lastTickPosX, camera.getPos().getX()));
            y = (blockPos.getY() - MathHelper.lerp(delta, lastTickPosY, camera.getPos().getY()));
            z = (blockPos.getZ() - MathHelper.lerp(delta, lastTickPosZ, camera.getPos().getZ()));

            //outlineVertexConsumerProvider.getBuffer(RenderLayers.getFluidLayer(blockState.getFluidState()));
            matrixStack.push();
            matrixStack.translate(x, y, z);

            if (!blockState.getFluidState().isEmpty()) {
                CustomFluidOutlineRenderer.renderFluidOutline(context.world(),
                        blockPos,
                        outlineVertexConsumerProvider.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)),
                        blockState,
                        blockState.getFluidState(),
                        matrixStack);
                /*dispatcher.renderFluid(blockPos, mc.world,
                        outlineVertexConsumerProvider.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)),
                        blockState, blockState.getFluidState());*/
            }


            // 渲染方块
            if (blockState.getRenderType() == BlockRenderType.MODEL) {
                dispatcher.renderBlock(blockState, blockPos, context.world(), matrixStack,
                        outlineVertexConsumerProvider.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)),
                        false, mc.getCameraEntity().getRandom());
            }

            // 渲染方块实体
            if (blockState.getBlock() instanceof BlockWithEntity) {
                BlockEntity blockEntity = context.world().getBlockEntity(blockPos);
                if (blockEntity != null) {
                    blockEntityRenderer.render(blockEntity, delta, matrixStack,
                            layer ->
                                    outlineVertexConsumerProvider.getBuffer(RenderLayer.getOutline(TextureManager.MISSING_IDENTIFIER)));
                }
            }
            matrixStack.pop();
        }
    }
    public static void onRenderOutline(RenderTickCounter tickCounter, Camera camera, Matrix4f matrix4f) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockRenderManager dispatcher = mc.getBlockRenderManager();
        BlockEntityRenderDispatcher blockEntityRenderer = mc.getBlockEntityRenderDispatcher();

        // 获取或创建 MatrixStack
        MatrixStack matrixStack = new MatrixStack(); // 手动创建，因为注入点未直接提供
        matrixStack.multiplyPositionMatrix(matrix4f);
        // 如果需要，可以尝试从 gameRenderer 获取当前的 MatrixStack（视具体实现而定）

        // 获取 OutlineVertexConsumerProvider
        OutlineVertexConsumerProvider outlineVertexConsumerProvider = mc.worldRenderer.bufferBuilders.getOutlineVertexConsumers();

        if (matrixStack == null) {
            System.err.println("MatrixStack is null, skipping render.");
            return;
        }

        outlineVertexConsumerProvider.setColor(255, 0, 255, 255); // 设置轮廓颜色（紫色）

        float delta = tickCounter.getTickDelta(false); // 使用注入的 tickCounter
        double lastTickPosX = camera.getPos().getX();
        double lastTickPosY = camera.getPos().getY();
        double lastTickPosZ = camera.getPos().getZ();

        // 遍历 blockToRenderer
        for (var entry : blockToRenderer.entrySet()) {
            BlockPos blockPos = entry.getKey();
            BlockState blockState = entry.getValue();
            Block block = blockState.getBlock();
            boolean shouldRender = true;
            if (!shouldRender) continue;

            double x = (blockPos.getX() - MathHelper.lerp(delta, lastTickPosX, camera.getPos().getX()));
            double y = (blockPos.getY() - MathHelper.lerp(delta, lastTickPosY, camera.getPos().getY()));
            double z = (blockPos.getZ() - MathHelper.lerp(delta, lastTickPosZ, camera.getPos().getZ()));

            if (!blockState.getFluidState().isEmpty()) {
                //System.out.println("Rendering fluid at " + blockPos + " with state " + blockState.getFluidState());
                dispatcher.renderFluid(blockPos, mc.world,
                        outlineVertexConsumerProvider.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)),
                        blockState, blockState.getFluidState());
            }

            matrixStack.push();
            matrixStack.translate(x, y, z);
            matrixStack.scale(1.001f, 1.001f, 1.001f);


            // 渲染方块
            if (blockState.getRenderType() == BlockRenderType.MODEL) {
                dispatcher.renderBlock(blockState, blockPos, (BlockRenderView) mc.world, matrixStack,
                        outlineVertexConsumerProvider.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)),
                        false, mc.getCameraEntity().getRandom());
            }

            // 渲染方块实体

            if (blockState.getBlock() instanceof BlockWithEntity) {
                BlockEntity blockEntity = mc.world.getBlockEntity(blockPos);
                if (blockEntity != null) {
                    blockEntityRenderer.render(blockEntity, delta, matrixStack, outlineVertexConsumerProvider);
                }
            }
            matrixStack.pop();
        }

    }
    public static void resolveBlocks(){
        World world = MinecraftClient.getInstance().world;
        blockToRenderer.clear();
        for(BlockPos pos : targetedBlocks){
            blockToRenderer.put(pos,world.getBlockState(pos));
        }
        OutlineManager.targetedBlocks.clear();
    }

}
