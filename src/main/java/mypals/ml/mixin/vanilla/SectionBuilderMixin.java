package mypals.ml.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.VertexSorter;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import mypals.ml.config.LucidityConfig;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(SectionBuilder.class)
public abstract class SectionBuilderMixin {
    @Shadow protected abstract <E extends BlockEntity> void addBlockEntity(SectionBuilder.RenderData data, E blockEntity);

    @Shadow @Final private BlockRenderManager blockRenderManager;

    @Shadow protected abstract BufferBuilder beginBufferBuilding(Map<RenderLayer, BufferBuilder> builders, BlockBufferAllocatorStorage allocatorStorage, RenderLayer layer);

    @WrapMethod(method = "build(Lnet/minecraft/util/math/ChunkSectionPos;Lnet/minecraft/client/render/chunk/ChunkRendererRegion;" +
            "Lcom/mojang/blaze3d/systems/VertexSorter;" +
            "Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;)Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;")
    public SectionBuilder.RenderData filterBuild(
            ChunkSectionPos sectionPos, ChunkRendererRegion renderRegion, VertexSorter vertexSorter, BlockBufferAllocatorStorage allocatorStorage, Operation<SectionBuilder.RenderData> original
    ) {
        LucidityConfig.CONFIG_HANDLER.instance();
        SectionBuilder.RenderData renderData = new SectionBuilder.RenderData();
        BlockPos blockPos = sectionPos.getMinPos();
        BlockPos blockPos2 = blockPos.add(15, 15, 15);
        ChunkOcclusionDataBuilder chunkOcclusionDataBuilder = new ChunkOcclusionDataBuilder();
        MatrixStack matrixStack = new MatrixStack();
        BlockModelRenderer.enableBrightnessCache();
        Map<RenderLayer, BufferBuilder> map = new Reference2ObjectArrayMap<>(RenderLayer.getBlockLayers().size());
        Random random = Random.create();

        for (BlockPos blockPos3 : BlockPos.iterate(blockPos, blockPos2)) {
            BlockState blockState = renderRegion.getBlockState(blockPos3);
            if (blockState.isOpaqueFullCube(renderRegion, blockPos3)) {
                chunkOcclusionDataBuilder.markClosed(blockPos3);
            }

            if (blockState.hasBlockEntity() && shouldRenderBlock(blockState,blockPos3)) {
                BlockEntity blockEntity = renderRegion.getBlockEntity(blockPos3);
                if (blockEntity != null) {
                    this.addBlockEntity(renderData, blockEntity);
                }
            }

            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty() && shouldRenderBlock(blockState,blockPos3)) {
                RenderLayer renderLayer = LucidityConfig.fluidTransparency == 0.01f? RenderLayers.getFluidLayer(fluidState):RenderLayer.getTranslucent();

                BufferBuilder bufferBuilder = this.beginBufferBuilding(map, allocatorStorage, renderLayer);
                this.blockRenderManager.renderFluid(blockPos3, renderRegion, bufferBuilder, blockState, fluidState);
            }

            if (blockState.getRenderType() == BlockRenderType.MODEL && shouldRenderBlock(blockState,blockPos3)) {
                RenderLayer renderLayer = RenderLayers.getBlockLayer(blockState);
                BufferBuilder bufferBuilder = this.beginBufferBuilding(map, allocatorStorage, renderLayer);
                matrixStack.push();
                matrixStack.translate(
                        (float)ChunkSectionPos.getLocalCoord(blockPos3.getX()),
                        (float)ChunkSectionPos.getLocalCoord(blockPos3.getY()),
                        (float)ChunkSectionPos.getLocalCoord(blockPos3.getZ())
                );
                this.blockRenderManager.renderBlock(blockState, blockPos3, renderRegion, matrixStack, bufferBuilder, true, random);
                matrixStack.pop();
            }
        }

        for (Map.Entry<RenderLayer, BufferBuilder> entry : map.entrySet()) {
            RenderLayer renderLayer2 = entry.getKey();
            BuiltBuffer builtBuffer = entry.getValue().endNullable();
            if (builtBuffer != null) {
                if (renderLayer2 == RenderLayer.getTranslucent()) {
                    renderData.translucencySortingData = builtBuffer.sortQuads(allocatorStorage.get(RenderLayer.getTranslucent()), vertexSorter);
                }

                renderData.buffers.put(renderLayer2, builtBuffer);
            }
        }

        BlockModelRenderer.disableBrightnessCache();
        renderData.chunkOcclusionData = chunkOcclusionDataBuilder.build();
        return renderData;
    }
}
