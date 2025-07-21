package mypals.ml.mixin.compat;

import net.caffeinemc.mods.sodium.client.render.chunk.
        compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;

import net.minecraft.block.BlockState;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;
@Mixin(value = BlockRenderer.class)
public abstract class BlockRendererMixin{
    
    @Inject(method = "processQuad(Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;)V",
            at = @At("HEAD"), cancellable = true,remap = false)
    private void injectProcessQuad(MutableQuadViewImpl quad, CallbackInfo ci) {
        BlockPos pos = ((AbstractBlockRenderContextAccessor) this).getPos();
        BlockRenderView world = ((AbstractBlockRenderContextAccessor) this).getLevel();

        Direction quadDirection = Direction.byIndex(quad.getFaceNormal());

        BlockPos neighborPos = pos.offset(quadDirection);
        BlockState neighborState = world.getBlockState(neighborPos);

        if (!shouldRenderBlock(neighborState, neighborPos)) {
            for (int i = 0; i < 4; i++) {
                quad.lightmap(i, LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
            }
        }
    }
}