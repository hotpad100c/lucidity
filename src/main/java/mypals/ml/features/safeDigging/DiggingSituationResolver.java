package mypals.ml.features.safeDigging;

import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.CubeShape;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;

import static mypals.ml.rendering.InformationRender.mapAlpha;

public class DiggingSituationResolver {
    public static int WARNING_TIME = 50;
    public static int warningTime = WARNING_TIME;
    public static void resolveBreakingSituation(PlayerEntity player, World world, BlockPos pos) {
        if(world.getBlockState(pos).getBlock() == Blocks.WATER || world.getBlockState(pos).getBlock() == Blocks.LAVA||
                world.getBlockState(pos).getBlock() == Blocks.AIR ||  world.getBlockState(pos).getBlock() == Blocks.VOID_AIR||
        world.getBlockState(pos).getBlock() == Blocks.CAVE_AIR || world.getBlockState(pos).getBlock() == Blocks.STRUCTURE_VOID){return;}
        BlockPos[] offsets = {
                pos.up(),      // 上方
                pos.down(),    // 下方
                pos.north(),   // 北
                pos.south(),   // 南
                pos.east(),    // 东
                pos.west()     // 西
        };

        for (BlockPos offset : offsets) {
            Block block = world.getBlockState(offset).getBlock();

            if (offset.equals(pos.up()) && (block == Blocks.LAVA || block == Blocks.WATER)) {
                drawWarnings(pos, offset);
            }

            if (offset.equals(pos.down()) && (block == Blocks.AIR || block == Blocks.LAVA) && player.getBlockPos().equals(pos.up())) {
                drawWarnings(pos, offset);
            }

            if ((offset.equals(pos.north()) || offset.equals(pos.south()) ||
                    offset.equals(pos.east()) || offset.equals(pos.west())) &&
                    (block == Blocks.LAVA || block == Blocks.WATER)) {
                drawWarnings(pos, offset);
            }
        }
    }

    private static void drawWarnings(BlockPos pos, BlockPos pos2) {
        InformationRender.addCube(new CubeShape(pos, (float) mapAlpha(warningTime,0,WARNING_TIME), Color.ORANGE,false));
        InformationRender.addCube(new CubeShape(pos2,(float) mapAlpha(warningTime,0,WARNING_TIME), Color.RED,true));
    }
}
