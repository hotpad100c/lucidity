package mypals.ml.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.config.LucidityConfig;
import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModelRenderer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;
@Debug(export = true)
@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {
    @WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/BlockModelRenderer;shouldDrawFace(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;ZLnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;)Z"),
            method = {
                    "renderSmooth(Lnet/minecraft/world/BlockRenderView;Ljava/util/List;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZI)V",
                    "renderFlat(Lnet/minecraft/world/BlockRenderView;Ljava/util/List;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZI)V"})
    private boolean shouldDrawSmoothOrFlat(BlockRenderView blockRenderView, BlockState state, boolean cull,
                                         Direction side, BlockPos pos, Operation<Boolean> original)
    {
        BlockPos posFixed = pos.toImmutable();
        if (!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {

            boolean shouldRender = shouldRenderBlock(state,posFixed);
            boolean shouldRenderNeighbor = shouldRenderBlock(
                    blockRenderView.getBlockState(posFixed.offset(side)),posFixed.offset(side));

            if (shouldRender && !shouldRenderNeighbor) {
                return true;
            }
            else if (!shouldRender && shouldRenderNeighbor) {
                return false;
            }
        }
        return original.call(blockRenderView, state, cull, side, posFixed);
    }
}