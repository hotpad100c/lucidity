package mypals.ml.features.explosionVisualizer.explosoionBehaviors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.ref.Reference;
import java.util.Optional;

public class WindChargeExplosionBehaviorManager extends ExplosionBehaviorManager{
    private final boolean destroyBlocks;
    public WindChargeExplosionBehaviorManager(
            boolean destroyBlocks
    ) {
        this.destroyBlocks = destroyBlocks;
    }
    @Override
    public Optional<Float> getBlastResistance(World world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        return blockState.isIn(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS) ? Optional.of(3600000.0F) : Optional.empty();
    }
}