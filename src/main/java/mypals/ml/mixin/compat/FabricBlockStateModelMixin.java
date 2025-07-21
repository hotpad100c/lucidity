package mypals.ml.mixin.compat;


import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Predicate;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;
import static net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer.STANDARD_MATERIAL;

@Mixin(BlockStateModel.class)
public interface FabricBlockStateModelMixin extends FabricBlockStateModel {
    /*@Override
    default void emitQuads(QuadEmitter emitter, BlockRenderView blockView,
                           BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
        BlockPos posFixed = pos.toImmutable();
        final List<BlockModelPart> parts = ((BlockStateModel) this).getParts(random);
        final int partCount = parts.size();
        for (int i = 0; i < partCount; i++) {
            if (!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {
                emitQuads(parts.get(i), emitter, cullTest, blockView, posFixed, state);
            }else parts.get(i).emitQuads(emitter, cullTest);
        }

    }

    @Unique
    private static void emitQuads(BlockModelPart part, QuadEmitter emitter, Predicate<@Nullable Direction> cullTest, BlockRenderView blockView,
                                  BlockPos pos, BlockState state) {
        // This does not exactly match vanilla, but doing so requires hiding state all over the FRAPI impl.
        RenderMaterial NO_AO_MATERIAL = Renderer.get().materialFinder().shadeMode(ShadeMode.VANILLA).ambientOcclusion(TriState.FALSE).find();

        final RenderMaterial defaultMaterial = part.useAmbientOcclusion() ? STANDARD_MATERIAL : NO_AO_MATERIAL;

        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            final Direction cullFace = ModelHelper.faceFromIndex(i);


            boolean shouldRender = shouldRenderBlock(state,pos);
            Predicate<Direction> cull = cullTest;
            boolean shouldRenderNeighbor = shouldRenderBlock(
                    blockView.getBlockState(pos.offset(cullFace)),pos.offset(cullFace));
            if(shouldRender && !shouldRenderNeighbor) {

            } else if (!shouldRender && shouldRenderNeighbor) {
                continue;
            }
            else if (cullTest.test(cullFace)) {
                continue;
            }

            final List<BakedQuad> quads = part.getQuads(cullFace);
            final int quadCount = quads.size();

            for (int j = 0; j < quadCount; j++) {
                final BakedQuad q = quads.get(j);
                emitter.fromVanilla(q, defaultMaterial, cullFace);
                emitter.emit();
            }
        }
    }*/
}
