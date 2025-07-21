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