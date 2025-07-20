package mypals.ml.mixin.compat;

import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;


@Mixin(BlockOcclusionCache.class)
public class MixinBlockOcclusionCacheMixin {
    @Inject(at = @At("HEAD"), method = "shouldDrawSide", cancellable = true, remap = false)
    private void filterShouldDrawSide(BlockState state, BlockView reader, BlockPos pos, Direction face,
                                      CallbackInfoReturnable<Boolean> ci) {
        if (!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {

            boolean shouldRender = shouldRenderBlock(state,pos);
            boolean shouldRenderNeighbor = shouldRenderBlock(reader.getBlockState(pos.offset(face)),pos.offset(face));

            if (shouldRender && !shouldRenderNeighbor) {
                ci.setReturnValue(true);
            }
            else if (!shouldRender && shouldRenderNeighbor) {
                ci.setReturnValue(false);
            }
        }
    }
}