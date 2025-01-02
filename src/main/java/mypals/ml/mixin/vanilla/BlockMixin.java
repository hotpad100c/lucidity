package mypals.ml.mixin.vanilla;

import mypals.ml.config.LucidityConfig;
import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(at = @At("HEAD"),method =
            "shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;)Z",
            cancellable = true
    )
    private static void filterShouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
        LucidityConfig.CONFIG_HANDLER.instance();
        if (!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {
            boolean shouldRender = shouldRenderBlock(state,pos);
            boolean shouldRenderNeighbor = shouldRenderBlock(world.getBlockState(pos.offset(side)),pos.offset(side));

            if (shouldRender && !shouldRenderNeighbor) {
                cir.setReturnValue(true);
            }
            else if (!shouldRender && shouldRenderNeighbor) {
                cir.setReturnValue(false);
            }
        }
            /*int currentBlockId = Registries.BLOCK.getRawId(state.getBlock());
            int neighborBlockId = Registries.BLOCK.getRawId(world.getBlockState(otherPos).getBlock());
            boolean isBlacklistedBlock = isSelectedType(currentBlockId);
            boolean isNeighborBlacklisted = isSelectedType(neighborBlockId);

            if(!invertSelectiveBlockTypes) {
                if (isBlacklistedBlock) {
                    return false;
                }
                else if (isNeighborBlacklisted) {
                    return true;
                }
                else{
                    return original.call(state, world, pos, side, otherPos);
                }
            }else{
                if (!isBlacklistedBlock) {
                    return false;
                }
                else if (!isNeighborBlacklisted) {
                    return true;
                }
                else{
                    return original.call(state, world, pos, side, otherPos);
                }
            }
        }else{
            return original.call(state, world, pos, side, otherPos);
        }*/

    }
}
