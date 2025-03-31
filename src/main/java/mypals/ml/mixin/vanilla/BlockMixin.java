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
            "Lnet/minecraft/block/Block;shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z",
            cancellable = true
    )
    private static void filterShouldDrawSide(BlockState state, BlockState otherState, Direction side, CallbackInfoReturnable<Boolean> cir) {
        /*LucidityConfig.CONFIG_HANDLER.instance();
        if (!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {
            boolean shouldRender = shouldRenderBlock(state,pos);
            boolean shouldRenderNeighbor = shouldRenderBlock(world.getBlockState(pos.offset(side)),pos.offset(side));

            if (shouldRender && !shouldRenderNeighbor) {
                cir.setReturnValue(true);
            }
            else if (!shouldRender && shouldRenderNeighbor) {
                cir.setReturnValue(false);
            }
        }*/
    }
}
