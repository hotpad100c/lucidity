package mypals.ml.features.OreFinder;

import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.ShineMarker;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.awt.*;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static mypals.ml.config.LucidityConfig.oreHighlightRange;
import static mypals.ml.features.OreFinder.MineralFinder.isExposedMineral;

public class OreResolver {
    public static ConcurrentHashMap<BlockPos,Color> recordedOres = new ConcurrentHashMap<>();
    public static void onClientTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        Random random = new Random();
        if (world == null || client.player == null) {
            return;
        }

        BlockPos playerPos = client.player.getBlockPos();

        for(Map.Entry<BlockPos,Color> entry: recordedOres.entrySet()){
            Vec3d pos = entry.getKey().toImmutable().toCenterPos();
            if(isInDistance(BlockPos.ofFloored(pos),oreHighlightRange))
            InformationRender.addShineMarker(new ShineMarker(pos,entry.getValue(),0.4f,
                    random.nextInt(1,3), random.nextInt(5,7),entry.getKey().hashCode(),true,false),30);

        }

        /*for (int x = playerPos.getX() - radius; x <= playerPos.getX() + radius; x++) {
            for (int z = playerPos.getZ() - radius; z <= playerPos.getZ() + radius; z++) {
                int yMax = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);

                for (int y = yMax; y >= yMax - 1; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    if (MineralFinder.isExposedMineral(world, pos)) {
                        InformationRender.addShineMarker(new ShineMarker(pos.toCenterPos(),MineralFinder.MINERAL_BLOCKS.get(block),0.4f,
                                random.nextInt(1,3), random.nextInt(5,7),pos.hashCode(),true,false),30);
                    }
                }
            }
        }*/
    }
    public static int[] decodeColor(int color) {
        // 提取每个分量
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        return new int[]{red, green, blue, alpha};
    }
    public static boolean isInDistance(BlockPos blockPos, double maxDistance) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        double playerX = player.getX();
        double playerZ = player.getZ();

        double blockX = blockPos.getX() + 0.5; // 中心位置
        double blockZ = blockPos.getZ() + 0.5; // 中心位置

        double distance = Math.sqrt(
                Math.pow(playerX - blockX, 2) +
                        Math.pow(playerZ - blockZ, 2)
        );

        return distance <= maxDistance;
    }

    public static void tryAddToRecordedOreListOrRemove(BlockPos pos) {
        if (isExposedMineral(MinecraftClient.getInstance().world, pos)) {
            Block block = MinecraftClient.getInstance().world.getBlockState(pos).getBlock();
            boolean haveOreNearBy = false;
            Color color = MineralFinder.MINERAL_BLOCKS.get(block);
            for (Direction direction2 : Direction.values()) {
                if (OreResolver.recordedOres.containsKey(pos.offset(direction2)) && OreResolver.recordedOres.get(pos.offset(direction2)).equals(color)) {
                    haveOreNearBy = true;
                }
            }
            if (!haveOreNearBy)
                OreResolver.recordedOres.put(pos, color);
        } else {
            OreResolver.recordedOres.remove(pos);
        }
    }
}
