package mypals.ml.mixin.compat;

import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(DefaultFluidRenderer.class)
public class DefaultFluidRendererMixin {
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
}
