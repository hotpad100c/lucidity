package mypals.ml.features.worldEaterHelper;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MineralFinder {
    public static final Map<Block, Color> MINERAL_BLOCKS = new HashMap<>();

    static {
        MINERAL_BLOCKS.put(Blocks.COAL_ORE, Color.BLACK);
        MINERAL_BLOCKS.put(Blocks.IRON_ORE, new Color(216, 216, 216));
        MINERAL_BLOCKS.put(Blocks.GOLD_ORE, Color.YELLOW);
        MINERAL_BLOCKS.put(Blocks.DIAMOND_ORE, new Color(0, 255, 255));
        MINERAL_BLOCKS.put(Blocks.EMERALD_ORE, Color.GREEN);
        MINERAL_BLOCKS.put(Blocks.REDSTONE_ORE, Color.RED);
        MINERAL_BLOCKS.put(Blocks.LAPIS_ORE, Color.BLUE);
        MINERAL_BLOCKS.put(Blocks.COPPER_ORE, Color.ORANGE);
        MINERAL_BLOCKS.put(Blocks.NETHER_QUARTZ_ORE, new Color(255, 240, 220));
        MINERAL_BLOCKS.put(Blocks.NETHER_GOLD_ORE, Color.ORANGE);
        MINERAL_BLOCKS.put(Blocks.ANCIENT_DEBRIS, new Color(128, 64, 32));
        MINERAL_BLOCKS.put(Blocks.OBSIDIAN, new Color(32, 0, 64));
        MINERAL_BLOCKS.put(Blocks.DEEPSLATE_COAL_ORE, new Color(16, 16, 16));
        MINERAL_BLOCKS.put(Blocks.DEEPSLATE_IRON_ORE, new Color(191, 191, 191));
        MINERAL_BLOCKS.put(Blocks.DEEPSLATE_GOLD_ORE, new Color(255, 215, 0));
        MINERAL_BLOCKS.put(Blocks.DEEPSLATE_DIAMOND_ORE, new Color(0, 204, 204));
        MINERAL_BLOCKS.put(Blocks.DEEPSLATE_EMERALD_ORE, new Color(0, 153, 0));
        MINERAL_BLOCKS.put(Blocks.DEEPSLATE_REDSTONE_ORE, new Color(153, 0, 0));
        MINERAL_BLOCKS.put(Blocks.DEEPSLATE_LAPIS_ORE, new Color(0, 0, 153));
        MINERAL_BLOCKS.put(Blocks.DEEPSLATE_COPPER_ORE, Color.ORANGE);
    }

    public static boolean isMineral(Block block) {
        return MINERAL_BLOCKS.containsKey(block);
    }

    public static boolean isExposedMineral(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();

        if (!isMineral(block)) {
            return false;
        }
        for (BlockPos offset : BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
            if (world.getBlockState(offset).isAir() || !world.getBlockState(offset).isSolidBlock(world,offset)) {
                return true;
            }
        }
        return false;
    }
}
