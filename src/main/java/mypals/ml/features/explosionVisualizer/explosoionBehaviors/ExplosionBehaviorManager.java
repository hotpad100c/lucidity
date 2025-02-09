package mypals.ml.features.explosionVisualizer.explosoionBehaviors;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class ExplosionBehaviorManager {
    public Optional<Float> getBlastResistance(World world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        return blockState.isAir() && fluidState.isEmpty()
                ? Optional.empty()
                : Optional.of(Math.max(blockState.getBlock().getBlastResistance(), fluidState.getBlastResistance()));
    }

    public boolean canDestroyBlock(World world, BlockPos pos, BlockState state) {
        return true;
    }

}
