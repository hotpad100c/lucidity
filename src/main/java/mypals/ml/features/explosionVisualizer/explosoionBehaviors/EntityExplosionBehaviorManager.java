package mypals.ml.features.explosionVisualizer.explosoionBehaviors;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class EntityExplosionBehaviorManager extends ExplosionBehaviorManager {

    @Override
    public Optional<Float> getBlastResistance(World world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        return super.getBlastResistance(world,pos,blockState, fluidState)
                .map(max -> getEffectiveExplosionResistance(world, pos, blockState,max));
    }

    @Override
    public boolean canDestroyBlock(World world, BlockPos pos, BlockState state) {
        return canExplosionDestroyBlock(world, pos, state);
    }


    public float getEffectiveExplosionResistance(World world, BlockPos pos, BlockState blockState, float max)
    {
        return max;
    };

    public boolean canExplosionDestroyBlock(World world, BlockPos pos, BlockState state) {
        return true;
    }


}
