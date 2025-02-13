package mypals.ml.features.specialStructureDisplays.treeStructures;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.root.MangroveRootPlacer;
import net.minecraft.world.gen.root.RootPlacer;
import net.minecraft.world.gen.trunk.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TreeGrowingBoxCalculator {
    public static List<TreeGrowingBox> getStaticTreeBox( BlockPos pos, String treeType) {
        List<TreeGrowingBox> boxes = new ArrayList<>();
        World world = MinecraftClient.getInstance().world;
        // **定义树的参数**
        int baseHeight, firstRandomHeight, secondRandomHeight;
        int foliageRadius, maxFoliageHeight = 0;
        boolean isLargeTrunk = false;

        switch (treeType) {
            case "OAK" -> {
                baseHeight = 4;
                firstRandomHeight = 2;
                secondRandomHeight = 0;
                foliageRadius = 2;
            }
            case "BIRCH" -> {
                baseHeight = 5;
                firstRandomHeight = 2;
                secondRandomHeight = 0;
                foliageRadius = 2;
            }
            case "SPRUCE" -> {
                baseHeight = 5;
                firstRandomHeight = 2;
                secondRandomHeight = 1;
                foliageRadius = 2;
            }
            case "JUNGLE_TREE" -> {
                baseHeight = 4;
                firstRandomHeight = 8;
                secondRandomHeight = 0;
                foliageRadius = 3;
            }
            case "MEGA_JUNGLE_TREE" -> {
                baseHeight = 10;
                firstRandomHeight = 2;
                secondRandomHeight = 19;
                foliageRadius = 3;
                isLargeTrunk = isSapling2x2(world, pos, Blocks.JUNGLE_SAPLING);
            }
            case "ACACIA" -> {
                baseHeight = 5;
                firstRandomHeight = 2;
                secondRandomHeight = 2;
                foliageRadius = 2;
            }
            case "DARK_OAK" -> {
                baseHeight = 6;
                firstRandomHeight = 2;
                secondRandomHeight = 1;
                foliageRadius = 2;
                isLargeTrunk = isSapling2x2(world, pos, Blocks.DARK_OAK_SAPLING);
            }
            case "CHERRY" -> {
                baseHeight = 7;
                firstRandomHeight = 1;
                secondRandomHeight = 0;
                foliageRadius = 5; // 樱花树叶子更大
                maxFoliageHeight = 16; // 叶子高度最大值
            }
            case "MANGROVE" -> {
                baseHeight = 4;
                firstRandomHeight = 1;
                secondRandomHeight = 9;
                foliageRadius = 3;
            }
            default -> {
                baseHeight = 4;
                firstRandomHeight = 2;
                secondRandomHeight = 0;
                foliageRadius = 2;
            }
        }

        int maxTrunkHeight = baseHeight + firstRandomHeight + secondRandomHeight;

        // **计算树干 Box**
        BlockPos trunkPos1 = pos;
        BlockPos trunkPos2 = isLargeTrunk ? pos.add(1, maxTrunkHeight, 1) : pos.add(0, maxTrunkHeight, 0);
        boxes.add(new TreeGrowingBox(trunkPos1, trunkPos2, TreeGrowingBox.PartType.TRUNK));

        // **计算叶子 Box**
        int foliageBottom = maxTrunkHeight - 3;
        if (treeType.equals("CHERRY")) {
            foliageBottom -= 2; // 樱花树叶子向下扩展
        }
        BlockPos foliagePos1 = pos.add(-foliageRadius, foliageBottom, -foliageRadius);
        BlockPos foliagePos2 = pos.add(foliageRadius, maxTrunkHeight, foliageRadius);
        boxes.add(new TreeGrowingBox(foliagePos1, foliagePos2, TreeGrowingBox.PartType.FOLIAGE));

        return boxes;
    }

    /**
     * **检查是否是 2x2 树苗**
     */
    private static boolean isSapling2x2(World world, BlockPos pos, Block saplingType) {
        return world.getBlockState(pos).isOf(saplingType) &&
                world.getBlockState(pos.add(1, 0, 0)).isOf(saplingType) &&
                world.getBlockState(pos.add(0, 0, 1)).isOf(saplingType) &&
                world.getBlockState(pos.add(1, 0, 1)).isOf(saplingType);
    }
    public static List<TreeGrowingBox> getTreeGrowingBoxes(ClientWorld world, BlockPos pos, RegistryEntry<ConfiguredFeature<?, ?>> registryEntry) {
        List<TreeGrowingBox> boxes = new ArrayList<>();

        if (registryEntry != null && registryEntry.value().config() instanceof TreeFeatureConfig treeConfig) {
            TrunkPlacer trunkPlacer = treeConfig.trunkPlacer;

            // 获取树干最大高度
            int baseHeight = trunkPlacer.baseHeight;
            int firstRandomHeight = trunkPlacer.firstRandomHeight;
            int secondRandomHeight = trunkPlacer.secondRandomHeight;
            int maxTrunkHeight = baseHeight + firstRandomHeight + secondRandomHeight;

            // 计算树干宽度
            int trunkWidth = 1;
            if (trunkPlacer instanceof LargeOakTrunkPlacer ||
                    trunkPlacer instanceof MegaJungleTrunkPlacer ||
                    trunkPlacer instanceof DarkOakTrunkPlacer) {
                trunkWidth = 2; // 2x2 树干
            } else if (trunkPlacer instanceof GiantTrunkPlacer) {
                trunkWidth = 4; // 4x4 树干
            }

            // 添加树干盒子
            BlockPos trunkPos1 = pos;
            BlockPos trunkPos2 = pos.add(trunkWidth - 1, maxTrunkHeight, trunkWidth - 1);
            boxes.add(new TreeGrowingBox(trunkPos1, trunkPos2, TreeGrowingBox.PartType.TRUNK));

            // 计算树叶范围（使用 `getMax()` 获取 `radius` 和 `offset`）
            int maxFoliageRadius = treeConfig.foliagePlacer.radius.getMax();
            int maxOffset = treeConfig.foliagePlacer.offset.getMax();

            BlockPos foliagePos1 = pos.add(-maxFoliageRadius, maxTrunkHeight - 3 - maxOffset, -maxFoliageRadius);
            BlockPos foliagePos2 = pos.add(maxFoliageRadius, maxTrunkHeight + 2 - maxOffset, maxFoliageRadius);
            boxes.add(new TreeGrowingBox(foliagePos1, foliagePos2, TreeGrowingBox.PartType.FOLIAGE));

            // 计算树根（仅适用于红树）
            Optional<RootPlacer> rootPlacerOptional = treeConfig.rootPlacer;
            if (rootPlacerOptional.isPresent()) {
                RootPlacer rootPlacer = rootPlacerOptional.get();
                if (rootPlacer instanceof MangroveRootPlacer mangroveRootPlacer) {
                    int maxRootLength = mangroveRootPlacer.mangroveRootPlacement.maxRootLength();
                    int maxRootWidth = mangroveRootPlacer.mangroveRootPlacement.maxRootWidth();

                    BlockPos rootPos1 = pos.add(-maxRootWidth, -maxRootLength, -maxRootWidth);
                    BlockPos rootPos2 = pos.add(maxRootWidth, 0, maxRootWidth);
                    boxes.add(new TreeGrowingBox(rootPos1, rootPos2, TreeGrowingBox.PartType.ROOT));
                }
            }
        }

        return boxes;
    }
}

