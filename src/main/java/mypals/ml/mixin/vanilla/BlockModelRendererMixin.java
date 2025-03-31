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
    /*@WrapMethod(method = "renderQuadsSmooth(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Ljava/util/List;[FLjava/util/BitSet;Lnet/minecraft/client/render/block/BlockModelRenderer$AmbientOcclusionCalculator;I)V")
    public void renderQuadsSmooth(
            BlockRenderView world, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, float[] box, BitSet flags, BlockModelRenderer.AmbientOcclusionCalculator ambientOcclusionCalculator, int overlay, Operation<Void> original
    ) {
        for (BakedQuad bakedQuad : quads) {
            Direction quadDirection = bakedQuad.getFace();

            BlockPos neighborPos = pos.offset(quadDirection);
            BlockState neighborState = world.getBlockState(neighborPos);

            getQuadDimensions(world, state, pos, bakedQuad.getVertexData(), quadDirection, box, flags);
            ambientOcclusionCalculator.apply(world, state, pos, quadDirection, box, flags, bakedQuad.hasShade());

            int[] light = ambientOcclusionCalculator.light;

            if (!shouldRenderBlock(neighborState, neighborPos)) {
                light = new int[]{0xF000F0, 0xF000F0, 0xF000F0, 0xF000F0};
            }

            renderQuad(
                    world,
                    state,
                    pos,
                    vertexConsumer,
                    matrices.peek(),
                    bakedQuad,
                    ambientOcclusionCalculator.brightness[0],
                    ambientOcclusionCalculator.brightness[1],
                    ambientOcclusionCalculator.brightness[2],
                    ambientOcclusionCalculator.brightness[3],
                    light[0],
                    light[1],
                    light[2],
                    light[3],
                    overlay
            );
        }
    }
    @WrapMethod(method = "renderQuadsFlat(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;IIZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Ljava/util/List;Ljava/util/BitSet;)V")
    public void renderQuadsFlat(
            BlockRenderView world, BlockState state, BlockPos pos, int light, int overlay, boolean useWorldLight, MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, BitSet flags, Operation<Void> original     // 提供渲染所需的世界视图
    ) {
        for (BakedQuad bakedQuad : quads) {
            if (useWorldLight) {
                getQuadDimensions(world, state, pos, bakedQuad.getVertexData(), bakedQuad.getFace(), null, flags);

                BlockPos blockPos = flags.get(0) ? pos.offset(bakedQuad.getFace()) : pos;
                BlockState neighborState = world.getBlockState(blockPos);

                light = WorldRenderer.getLightmapCoordinates(world, state, blockPos);

                if (shouldRenderBlock(neighborState, blockPos)) {
                    light = 0xF000F0;
                }
            }
            float f = world.getBrightness(bakedQuad.getFace(), bakedQuad.hasShade());
            renderQuad(
                    world, state, pos, vertexConsumer, matrices.peek(), bakedQuad,
                    f, f, f, f,
                    light, light, light, light,
                    overlay
            );
        }
    }*/
    @Unique
    private void renderQuad(
            BlockRenderView world,
            BlockState state,
            BlockPos pos,
            VertexConsumer vertexConsumer,
            MatrixStack.Entry matrixEntry,
            BakedQuad quad,
            float brightness0,
            float brightness1,
            float brightness2,
            float brightness3,
            int light0,
            int light1,
            int light2,
            int light3,
            int overlay
    ) {
        float f;
        float g;
        float h;
        if (quad.hasTint()) {
            int i = colors.getColor(state, world, pos, quad.getTintIndex());
            f = (float)(i >> 16 & 0xFF) / 255.0F;
            g = (float)(i >> 8 & 0xFF) / 255.0F;
            h = (float)(i & 0xFF) / 255.0F;
        } else {
            f = 1.0F;
            g = 1.0F;
            h = 1.0F;
        }

        vertexConsumer.quad(
                matrixEntry, quad, new float[]{brightness0, brightness1, brightness2, brightness3}, f, g, h, 1.0F, new int[]{light0, light1, light2, light3}, overlay, true
        );
    }
    @Unique
    private void getQuadDimensions(BlockRenderView world, BlockState state, BlockPos pos, int[] vertexData, Direction face, @Nullable float[] box, BitSet flags) {
        float f = 32.0F;
        float g = 32.0F;
        float h = 32.0F;
        float i = -32.0F;
        float j = -32.0F;
        float k = -32.0F;

        for (int l = 0; l < 4; l++) {
            float m = Float.intBitsToFloat(vertexData[l * 8]);
            float n = Float.intBitsToFloat(vertexData[l * 8 + 1]);
            float o = Float.intBitsToFloat(vertexData[l * 8 + 2]);
            f = Math.min(f, m);
            g = Math.min(g, n);
            h = Math.min(h, o);
            i = Math.max(i, m);
            j = Math.max(j, n);
            k = Math.max(k, o);
        }

        if (box != null) {
            box[Direction.WEST.getId()] = f;
            box[Direction.EAST.getId()] = i;
            box[Direction.DOWN.getId()] = g;
            box[Direction.UP.getId()] = j;
            box[Direction.NORTH.getId()] = h;
            box[Direction.SOUTH.getId()] = k;
            int l = DIRECTIONS.length;
            box[Direction.WEST.getId() + l] = 1.0F - f;
            box[Direction.EAST.getId() + l] = 1.0F - i;
            box[Direction.DOWN.getId() + l] = 1.0F - g;
            box[Direction.UP.getId() + l] = 1.0F - j;
            box[Direction.NORTH.getId() + l] = 1.0F - h;
            box[Direction.SOUTH.getId() + l] = 1.0F - k;
        }
        switch (face) {
            case DOWN:
                flags.set(1, f >= 1.0E-4F || h >= 1.0E-4F || i <= 0.9999F || k <= 0.9999F);
                flags.set(0, g == j && (g < 1.0E-4F || state.isFullCube(world, pos)));
                break;
            case UP:
                flags.set(1, f >= 1.0E-4F || h >= 1.0E-4F || i <= 0.9999F || k <= 0.9999F);
                flags.set(0, g == j && (j > 0.9999F || state.isFullCube(world, pos)));
                break;
            case NORTH:
                flags.set(1, f >= 1.0E-4F || g >= 1.0E-4F || i <= 0.9999F || j <= 0.9999F);
                flags.set(0, h == k && (h < 1.0E-4F || state.isFullCube(world, pos)));
                break;
            case SOUTH:
                flags.set(1, f >= 1.0E-4F || g >= 1.0E-4F || i <= 0.9999F || j <= 0.9999F);
                flags.set(0, h == k && (k > 0.9999F || state.isFullCube(world, pos)));
                break;
            case WEST:
                flags.set(1, g >= 1.0E-4F || h >= 1.0E-4F || j <= 0.9999F || k <= 0.9999F);
                flags.set(0, f == i && (f < 1.0E-4F || state.isFullCube(world, pos)));
                break;
            case EAST:
                flags.set(1, g >= 1.0E-4F || h >= 1.0E-4F || j <= 0.9999F || k <= 0.9999F);
                flags.set(0, f == i && (i > 0.9999F || state.isFullCube(world, pos)));
        }
    }
}
