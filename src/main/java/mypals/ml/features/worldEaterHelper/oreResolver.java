package mypals.ml.features.worldEaterHelper;

import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.ShineMarker;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.awt.*;
import java.util.Random;

public class oreResolver {
    public static void scanForMineralsOptimized(int radius) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        Random random = new Random();
        if (world == null || client.player == null) {
            return;
        }

        BlockPos playerPos = client.player.getBlockPos();

        for (int x = playerPos.getX() - radius; x <= playerPos.getX() + radius; x++) {
            for (int z = playerPos.getZ() - radius; z <= playerPos.getZ() + radius; z++) {
                int yMax = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);

                for (int y = yMax; y >= yMax - 1; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    if (MineralFinder.isExposedMineral(world, pos)) {
                        InformationRender.addShineMarker(new ShineMarker(pos.toCenterPos(),MineralFinder.MINERAL_BLOCKS.get(block),0.4f,
                                random.nextInt(1,3), random.nextInt(5,7),pos.hashCode()),30);
                    }
                }
            }
        }
    }
    public static int[] decodeColor(int color) {
        // 提取每个分量
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        return new int[]{red, green, blue, alpha};
    }

}
