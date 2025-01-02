package mypals.ml.mixin.vanilla;

import net.minecraft.client.render.block.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FluidRenderer.class)
public class FluidRenderMixin {
    /*@Inject(at = @At("RETURN"),method = "isSideCovered(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/Direction;FLnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"
            ,cancellable = true)
    private static void isSideCovered(BlockView world, Direction direction, float height, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> ci) {
        if(enableSelectiveBlockRender) {
            ci.setReturnValue(!shouldRender(world.getBlockState(pos.offset(direction)),pos));
        }
    }*/
}
