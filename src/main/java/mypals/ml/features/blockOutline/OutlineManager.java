package mypals.ml.features.blockOutline;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.features.selectiveRendering.AreaBox;
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
import static mypals.ml.config.LucidityConfig.selectInSpectator;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.selectedAreas;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;
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


        for (var entry : blockToRenderer.entrySet()) {
            onRenderOutline(entry, delta, camera, matrixStack, Color.orange);
        }
        /*if((MinecraftClient.getInstance().player.getMainHandStack().getItem() == wand || (MinecraftClient.getInstance().player.isSpectator() && selectInSpectator))){
            for (AreaBox selectedArea : selectedAreas) {
                int minX = Math.min(selectedArea.minPos.getX(), selectedArea.maxPos.getX());
                int minY = Math.min(selectedArea.minPos.getY(), selectedArea.maxPos.getY());
                int minZ = Math.min(selectedArea.minPos.getZ(), selectedArea.maxPos.getZ());
                int maxX = Math.max(selectedArea.minPos.getX(), selectedArea.maxPos.getX());
                int maxY = Math.max(selectedArea.minPos.getY(), selectedArea.maxPos.getY());
                int maxZ = Math.max(selectedArea.minPos.getZ(), selectedArea.maxPos.getZ());

                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            BlockPos blockPos = new BlockPos(x, y, z);
                            onRenderOutline(new HashMap.SimpleEntry<>(blockPos, context.world().getBlockState(blockPos)), delta, camera, matrixStack, Color.white);
                        }
                    }
                }
            }
        }*/

    }
    public static void onRenderOutline(Map.Entry<BlockPos, BlockState> entry, float delta, Camera camera, MatrixStack matrixStack, Color color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockRenderManager dispatcher = mc.getBlockRenderManager();
        BlockEntityRenderDispatcher blockEntityRenderer = mc.getBlockEntityRenderDispatcher();
        double x, y, z;
        double lastTickPosX,lastTickPosY,lastTickPosZ;

        BlockPos blockPos = entry.getKey();
        BlockState blockState = entry.getValue();

        lastTickPosX = camera.getPos().getX();
        lastTickPosY = camera.getPos().getY();
        lastTickPosZ = camera.getPos().getZ();

        x = (blockPos.getX() - MathHelper.lerp(delta, lastTickPosX, camera.getPos().getX()));
        y = (blockPos.getY() - MathHelper.lerp(delta, lastTickPosY, camera.getPos().getY()));
        z = (blockPos.getZ() - MathHelper.lerp(delta, lastTickPosZ, camera.getPos().getZ()));

        OutlineVertexConsumerProvider outlineVertexConsumerProvider = mc.worldRenderer.bufferBuilders.getOutlineVertexConsumers();
        matrixStack.push();
        matrixStack.translate(x, y, z);
        matrixStack.scale(1.001f, 1.001f, 1.001f);


        outlineVertexConsumerProvider.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()); // 设置轮廓颜色（紫色）



        if (!blockState.getFluidState().isEmpty()) {
            //System.out.println("Rendering fluid at " + blockPos + " with state " + blockState.getFluidState());
            CustomFluidOutlineRenderer.renderFluidOutline(mc.world,blockPos,
                    outlineVertexConsumerProvider.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)),
                    blockState, blockState.getFluidState(), matrixStack);
        }




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
    public static void resolveBlocks(){
        World world = MinecraftClient.getInstance().world;
        blockToRenderer.clear();
        for(BlockPos pos : targetedBlocks){
            blockToRenderer.put(pos,world.getBlockState(pos));
        }
        OutlineManager.targetedBlocks.clear();
    }

}
