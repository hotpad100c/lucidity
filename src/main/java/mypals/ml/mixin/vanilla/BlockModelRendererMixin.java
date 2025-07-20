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
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;
import static net.minecraft.world.RedstoneView.DIRECTIONS;

@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {
    @Shadow @Final private BlockColors colors;
    @WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/Block;shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z"),
            method = {
                    "renderSmooth(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V",
                    "renderFlat(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V"})
    private boolean onRenderSmoothOrFlat(BlockState state, BlockState otherState,
                                         Direction side, Operation<Boolean> original, @Local(argsOnly = true) BlockRenderView world, @Local(argsOnly = true) BlockPos pos)
    {
        LucidityConfig.CONFIG_HANDLER.instance();
        if (!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {
            boolean shouldRender = shouldRenderBlock(state,pos);
            boolean shouldRenderNeighbor = shouldRenderBlock(world.getBlockState(pos.offset(side)),pos.offset(side));

            if (shouldRender && !shouldRenderNeighbor) {
                return true;
            }
            else if (!shouldRender && shouldRenderNeighbor) {
                return false;
            }
        }
        return Block.shouldDrawSide(state, world.getBlockState(pos.offset(side)), side);
    }
}