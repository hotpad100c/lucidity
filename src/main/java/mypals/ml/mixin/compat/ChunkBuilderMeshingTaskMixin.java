package mypals.ml.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.config.LucidityConfig;
import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(ChunkBuilderMeshingTask.class)


public abstract class ChunkBuilderMeshingTaskMixin{

    @WrapOperation(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V"
            )
    )
    public void filterBlockRender(BlockRenderer instance, BakedModel type, BlockState blockState, BlockPos model, BlockPos state, Operation<Void> original){
        if(shouldRenderBlock(blockState,model)){
            original.call(instance, type, blockState, model, state);
        }
    }
    @WrapOperation(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;" +
                    "Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;" +
                            "render(Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;" +
                            "Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;" +
                            "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;" +
                            "Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/TranslucentGeometryCollector;" +
                            "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V")
    )
    public void filterFluidRender(FluidRenderer instance, LevelSlice levelSlice, BlockState blockState,
                                  FluidState fluidState, BlockPos blockPos, BlockPos modelOffset,
                                  TranslucentGeometryCollector translucentGeometryCollector, ChunkBuildBuffers chunkBuildBuffers, Operation<Void> original){
        if(shouldRenderBlock(blockState,blockPos)){

            original.call(instance, levelSlice, blockState, fluidState, blockPos,modelOffset,translucentGeometryCollector,chunkBuildBuffers);
        }
    }
    @WrapOperation(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;" +
                    "Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/data/BuiltSectionInfo$Builder;addBlockEntity(Lnet/minecraft/block/entity/BlockEntity;Z)V"
            )
    )
    public void filterBlockStateRender(BuiltSectionInfo.Builder builder, BlockEntity blockEntity, boolean b , Operation<Void> original, @Local BlockState blockState){
        if(shouldRenderBlock(blockState,blockEntity.getPos())){
            original.call(builder,blockEntity,b);
        }
    }
}
