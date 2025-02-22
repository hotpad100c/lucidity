package mypals.ml.mixin;

import mypals.ml.features.betterBarrier.BetterBarrier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.text.html.BlockView;

@Mixin(BarrierBlock.class)
public class BarrierBlockMixin extends Block {
    public BarrierBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "getRenderType", cancellable = true)
    public void getRenderType(BlockState state, CallbackInfoReturnable<BlockRenderType> cir) {
        //cir.setReturnValue(SettingsKeyInit.toggle.getValue()?BlockRenderType.MODEL:BlockRenderType.INVISIBLE);
        cir.setReturnValue(BetterBarrier.shouldRenderBetterBarrier()?BlockRenderType.MODEL:BlockRenderType.INVISIBLE);
    }
    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return stateFrom.isOf(this) ? true : super.isSideInvisible(state, stateFrom, direction);
    }
}
