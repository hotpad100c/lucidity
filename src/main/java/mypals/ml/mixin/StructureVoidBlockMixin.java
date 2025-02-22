package mypals.ml.mixin;

import mypals.ml.features.betterBarrier.BetterBarrier;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureVoidBlock.class)
public class StructureVoidBlockMixin extends Block {
    public StructureVoidBlockMixin(Settings settings) {
        super(settings);
    }
    private static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape SHAPE_FULL = Block.createCuboidShape(0, 0, 0, 16, 16, 16);

    @Inject(at = @At("HEAD"), method = "getRenderType", cancellable = true)
    public void getRenderType(BlockState state, CallbackInfoReturnable<BlockRenderType> cir) {
        cir.setReturnValue(BetterBarrier.shouldRenderBetterStructureVoid()?BlockRenderType.MODEL:BlockRenderType.INVISIBLE);
    }
    @Inject(at = @At("HEAD"), method = "getOutlineShape", cancellable = true)
    protected void getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        cir.setReturnValue(BetterBarrier.shouldRenderBetterStructureVoid()?SHAPE_FULL:SHAPE);
    }
    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return stateFrom.isOf(this) || super.isSideInvisible(state, stateFrom, direction);
    }
    @Override
    public AbstractBlock.Settings getSettings() {
        return this.settings.nonOpaque();
    }
}
