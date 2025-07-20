package mypals.ml.features.blockOutline;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.BitSet;
import java.util.List;



public class CustomBlockOutlineRenderer {
    /*public static void render(BlockRenderView world, BakedModel model, BlockState state,
                       BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer,
                       boolean cull, Random random, long seed,
                              int overlay, BlockRenderManager blockRenderManager) {
        Vec3d vec3d = state.getModelOffset(world, pos);
        matrices.translate(vec3d.x, vec3d.y, vec3d.z);

        try {
            renderFlat(world, model, state, pos,
                    matrices, vertexConsumer, cull, random, seed, overlay,blockRenderManager);

        } catch (Throwable throwable) {
            //throwable.printStackTrace();
        }
    }
    public static void renderFlat(BlockRenderView world, BakedModel model,
                                  BlockState state, BlockPos pos, MatrixStack matrices,
                                  VertexConsumer vertexConsumer, boolean cull, Random random,
                                  long seed, int overlay, BlockRenderManager blockRenderManager) {
        BitSet bitSet = new BitSet(3);
        BlockPos.Mutable mutable = pos.mutableCopy();

        for(Direction direction : DIRECTIONS) {
            random.setSeed(seed);
            List<BakedQuad> list = model.getQuads(state, direction, random);
            if (!list.isEmpty()) {
                mutable.set(pos, direction);
                boolean shouldRender = Block.shouldDrawSide(state, world, pos, direction, mutable)
                        || !OutlineManager.blockToRenderer.containsKey(pos.offset(direction));
                if (shouldRender) {
                    blockRenderManager.getModelRenderer().renderQuadsFlat(world, state, pos, 0, overlay, false, matrices, vertexConsumer, list, bitSet);
                }
            }
        }

        random.setSeed(seed);
        List<BakedQuad> list2 = model.getQuads(state, (Direction)null, random);
        if (!list2.isEmpty()) {
            blockRenderManager.getModelRenderer().renderQuadsFlat(world, state, pos, -1, overlay, true, matrices, vertexConsumer, list2, bitSet);
        }

    }
    public static boolean shouldDraw(BlockPos pos){
        ClientWorld world = MinecraftClient.getInstance().world;
        for(Direction direction : DIRECTIONS) {
            BlockPos offset = pos.offset(direction);
            boolean shouldRender = !Block.isShapeFullCube(world.getBlockState(offset).getCollisionShape(world,offset))
                    || !OutlineManager.blockToRenderer.containsKey(pos.offset(direction));
            if (shouldRender) {
                return true;
            }
        }
        return false;
    }*/
}
