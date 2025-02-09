package mypals.ml.features.explosionVisualizer.explosoionBehaviors;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WindChargeTrigger {
    public static boolean canTriggerBlocks(BlockPos pos, World world) {
        Block block = world.getBlockState(pos).getBlock();
        BlockState blockState = world.getBlockState(pos);
        if (block instanceof CandleBlock) {
            return true;
        }

        else if (block instanceof DoorBlock) {
            return true;
        }

        else if (block instanceof ButtonBlock) {
            return true;
        }

        else if (block instanceof FenceGateBlock) {
            return true;
        }

        else if (block instanceof LeverBlock) {
            return true;
        }

        else if (block instanceof TrapdoorBlock) {
            return true;
        }

        else {
            return false;
        }
    }
}
