package mypals.ml.features.explosionVisualizer.explosoionBehaviors;

import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TntMinecartExplosionBehaviorManager extends EntityExplosionBehaviorManager{
    @Override
    public float getEffectiveExplosionResistance(World world, BlockPos pos, BlockState blockState, float max) {
        return !blockState.isIn(BlockTags.RAILS) && !world.getBlockState(pos.up()).isIn(BlockTags.RAILS)
                ? super.getEffectiveExplosionResistance(world, pos, blockState,max)
                : 0.0F;
    }

    @Override
    public boolean canExplosionDestroyBlock(World world, BlockPos pos, BlockState state) {
        return !state.isIn(BlockTags.RAILS) && !world.getBlockState(pos.up()).isIn(BlockTags.RAILS) && super.canExplosionDestroyBlock(world, pos, state);
    }
}
