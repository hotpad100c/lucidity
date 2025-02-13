package mypals.ml.features.specialStructureDisplays.treeStructures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class TreeSearcher {
    public static List<TreeGrowingBox> searchAndMergeTreeBoxes(ClientWorld world, BlockPos centerPos) {
        List<TreeGrowingBox> mergedBoxes = new ArrayList<>();

        // 遍历 10x10 区域
        int radius = 15; // 10x10 区域的半径
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos checkPos = centerPos.add(dx, dy, dz);
                    BlockState state = world.getBlockState(checkPos);
                    Block block = state.getBlock();

                    // 检测树苗，并硬编码树的生长范围
                    if (block == Blocks.OAK_SAPLING) {
                        mergedBoxes.addAll(TreeGrowingBoxCalculator.getStaticTreeBox(checkPos, "OAK"));
                    } else if (block == Blocks.BIRCH_SAPLING) {
                        mergedBoxes.addAll(TreeGrowingBoxCalculator.getStaticTreeBox(checkPos, "BIRCH"));
                    } else if (block == Blocks.SPRUCE_SAPLING) {
                        mergedBoxes.addAll(TreeGrowingBoxCalculator.getStaticTreeBox(checkPos, "SPRUCE"));
                    } else if (block == Blocks.JUNGLE_SAPLING) {
                        mergedBoxes.addAll(TreeGrowingBoxCalculator.getStaticTreeBox(checkPos, "JUNGLE_TREE"));
                    } else if (block == Blocks.ACACIA_SAPLING) {
                        mergedBoxes.addAll(TreeGrowingBoxCalculator.getStaticTreeBox(checkPos, "ACACIA"));
                    } else if (block == Blocks.DARK_OAK_SAPLING) {
                        mergedBoxes.addAll(TreeGrowingBoxCalculator.getStaticTreeBox(checkPos, "DARK_OAK"));
                    } else if (block == Blocks.CHERRY_SAPLING) {
                        mergedBoxes.addAll(TreeGrowingBoxCalculator.getStaticTreeBox(checkPos, "CHERRY"));
                    } else if (block == Blocks.MANGROVE_PROPAGULE) {
                        mergedBoxes.addAll(TreeGrowingBoxCalculator.getStaticTreeBox(checkPos, "MANGROVE"));
                    }
                }
            }
        }


        return mergedBoxes;
    }
}
