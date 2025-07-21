package mypals.ml.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.Lucidity;
import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;


@Mixin(AbstractBlockRenderContext.class)
public class AbstractBlockRenderContextMixin {
    @Shadow protected BlockState state;

    @Shadow protected BlockPos pos;

    @Shadow protected BlockRenderView level;

    @Shadow private boolean enableCulling;

    @WrapMethod(method = "Lnet/caffeinemc/mods/sodium/client/render/frapi/render/AbstractBlockRenderContext;" +
            "isFaceCulled(Lnet/minecraft/util/math/Direction;)Z", remap = false)
    private boolean filterShouldDrawSide(Direction face, Operation<Boolean> original) {
        if(face != null) {
            if (!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {
                BlockPos pos = this.pos.toImmutable();
                boolean shouldRender = shouldRenderBlock(state,pos);
                boolean shouldRenderNeighbor = shouldRenderBlock(level.getBlockState(pos.offset(face)),pos.offset(face));

                if(pos.equals(new BlockPos(8, -58, 4)) && Objects.equals(pos.offset(face), new BlockPos(7, -58, 4))) {
                    Lucidity.LOGGER.info("Checking rendering for pos: {}, face: {}, shouldRender: {}, shouldRenderNeighbor: {}", pos, face, shouldRender, shouldRenderNeighbor);

                }

                if (shouldRender && !shouldRenderNeighbor) {
                    return false; // Do not cull if the block should be rendered but its neighbor should not
                }

                else if(!shouldRender && !shouldRenderNeighbor) {
                    return true; // Cull if neither the block nor its neighbor should be rendered
                }
                else if (!shouldRender && shouldRenderNeighbor) {
                    return true; // Cull if the block should not be rendered but its neighbor should
                }
                else {
                    return original.call(face); // Default behavior if both should be rendered
                }
            }else {
                return original.call(face);
            }
        }else{
            return original.call(face);
        }

    }
}