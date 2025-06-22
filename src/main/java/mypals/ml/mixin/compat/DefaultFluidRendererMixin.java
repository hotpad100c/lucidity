package mypals.ml.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mypals.ml.config.LucidityConfig;
import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(DefaultFluidRenderer.class)
public class DefaultFluidRendererMixin {
    @Shadow @Final private int[] quadColors;

    @Inject(at = @At("RETURN"),method = "isFullBlockFluidOccluded(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Z"
            ,cancellable = true)
    private void isFullBlockFluidOccluded(BlockRenderView world, BlockPos pos, Direction dir, BlockState blockState, FluidState fluid, CallbackInfoReturnable<Boolean> cir) {
        if(!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {
            if(!shouldRenderBlock(world.getBlockState(pos.offset(dir)),pos.offset(dir)))
                cir.setReturnValue(false);
        }
    }
    @Inject(at = @At("RETURN"),method = "isSideExposed(Lnet/minecraft/world/BlockRenderView;IIILnet/minecraft/util/math/Direction;F)Z"
            ,cancellable = true)
    private void isSideExposed(BlockRenderView world, int x, int y, int z, Direction dir, float height, CallbackInfoReturnable<Boolean> cir) {
        if(!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {
            BlockPos pos = new BlockPos(x, y, z);
            if(!shouldRenderBlock(world.getBlockState(pos.offset(dir)),pos.offset(dir)))
                cir.setReturnValue(true);
        }
    }
    @WrapOperation(
            method = "writeQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V"
            )
    )
    private void wrapVertex(ChunkMeshBufferBuilder instance, ChunkVertexEncoder.Vertex[] vertices, Material material, Operation<Void> original) {
        float alpha = LucidityConfig.fluidTransparency;
        if(alpha == 1.01f){
            original.call(instance,vertices, material);
        }else{
            if (material.isTranslucent()) {
                for (int i = 0; i < 4; i++) {
                    int color = this.quadColors[i];
                    int r = color & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = (color >> 16) & 0xFF;
                    int a = (int) (alpha * 255.0F) & 0xFF;
                    this.quadColors[i] = (a << 24) | (b << 16) | (g << 8) | r;
                    vertices[i].color = this.quadColors[i];
                }
            }
            original.call(instance,vertices, material);
        }
    }
}
