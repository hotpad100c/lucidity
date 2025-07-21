package mypals.ml.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mypals.ml.config.LucidityConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Debug(export = true)
@Mixin(SectionBuilder.class)
public abstract class SectionBuilderMixin {
    @Shadow protected abstract <E extends BlockEntity> void addBlockEntity(SectionBuilder.RenderData data, E blockEntity);

    @Shadow @Final private BlockRenderManager blockRenderManager;

    @Shadow protected abstract BufferBuilder beginBufferBuilding(Map<RenderLayer, BufferBuilder> builders, BlockBufferAllocatorStorage allocatorStorage, RenderLayer layer);

    /*@WrapOperation(method = "build(Lnet/minecraft/util/math/ChunkSectionPos;" +
            "Lnet/minecraft/client/render/chunk/ChunkRendererRegion;" +
            "Lcom/mojang/blaze3d/systems/VertexSorter;" +
            "Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;)" +
            "Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/BlockRenderManager;" +
                            "renderFluid(Lnet/minecraft/util/math/BlockPos;" +
                            "Lnet/minecraft/world/BlockRenderView;" +
                            "Lnet/minecraft/client/render/VertexConsumer;" +
                            "Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)V"
            )
    )
    private void filterRenderFluid(
            BlockRenderManager instance, BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, Operation<Void> original
    ) {
        if (shouldRenderBlock(blockState,pos)) {
            original.call(instance, pos, world, vertexConsumer, blockState, fluidState);
        }
    }
    @WrapOperation(method = "build(Lnet/minecraft/util/math/ChunkSectionPos;" +
            "Lnet/minecraft/client/render/chunk/ChunkRendererRegion;" +
            "Lcom/mojang/blaze3d/systems/VertexSorter;" +
            "Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;)" +
            "Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/BlockRenderManager;" +
                            "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;" +
                            "Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;" +
                            "Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/List;)V"
            )
    )
    private void filterRenderBlock(
            BlockRenderManager instance, BlockState state, BlockPos pos, BlockRenderView world,
            MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull,
            List<BlockModelPart> parts, Operation<Void> original
    ) {
        if (shouldRenderBlock(state,pos)) {
            original.call(instance, state, pos, world, matrices, vertexConsumer, cull, parts);
        }
    }/*
    @WrapOperation(method = "build(Lnet/minecraft/util/math/ChunkSectionPos;" +
            "Lnet/minecraft/client/render/chunk/ChunkRendererRegion;" +
            "Lcom/mojang/blaze3d/systems/VertexSorter;" +
            "Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;)" +
            "Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/chunk/SectionBuilder;" +
                            "addBlockEntity(Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;" +
                            "Lnet/minecraft/block/entity/BlockEntity;)V"
            )
    )
    private void filterBlockEntities(
            SectionBuilder instance, SectionBuilder.RenderData data,
            BlockEntity blockEntity, Operation<Void> original
    ) {
        LucidityConfig.CONFIG_HANDLER.instance();
        if (shouldRenderBlock(blockEntity.getWorld().getBlockState(blockEntity.getPos()),blockEntity.getPos())) {
            original.call(instance, data, blockEntity);
        }
    }

     */
    @WrapOperation(method = "build(Lnet/minecraft/util/math/ChunkSectionPos;" +
            "Lnet/minecraft/client/render/chunk/ChunkRendererRegion;" +
            "Lcom/mojang/blaze3d/systems/VertexSorter;" +
            "Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;)" +
            "Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/chunk/ChunkRendererRegion;" +
                            "getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"
            )
    )
    private BlockState filterBlocks(
            ChunkRendererRegion instance, BlockPos pos, Operation<BlockState> original
    ) {
        BlockState state = original.call(instance, pos);
        return shouldRenderBlock(state,pos) ? state : Blocks.AIR.getDefaultState();
    }
}