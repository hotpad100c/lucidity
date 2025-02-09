package mypals.ml.features.explosionVisualizer.explosoionBehaviors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class WindChargeExplosionBehaviorManager extends ExplosionBehaviorManager{
    private final boolean destroyBlocks;
    private final Optional<RegistryEntryList<Block>> immuneBlocks;
    public WindChargeExplosionBehaviorManager(
            boolean destroyBlocks, Optional<RegistryEntryList<Block>> immuneBlocks
    ) {
        this.destroyBlocks = destroyBlocks;
        this.immuneBlocks = immuneBlocks;
    }
    @Override
    public Optional<Float> getBlastResistance(World world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (this.immuneBlocks.isPresent()) {
            return blockState.isIn(this.immuneBlocks.get()) ? Optional.of(3600000.0F) : Optional.empty();
        } else {
            return super.getBlastResistance(world, pos, blockState, fluidState);
        }
    }
}
