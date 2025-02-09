package mypals.ml.features.worldEaterHelper;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.*;
import java.util.List;

import static mypals.ml.config.LucidityConfig.selectedBlocksToHighLight;

public class MineralFinder {
    public static Map<Block, Color> MINERAL_BLOCKS = new HashMap<>();
    public static final List<String> DEFAULT_SELECTED = Arrays.asList(
            "minecraft:coal_ore;#000000",
            "minecraft:iron_ore;#D8D8D8",
            "minecraft:gold_ore;#FFD700",
            "minecraft:diamond_ore;#00FFFF",
            "minecraft:emerald_ore;#009900",
            "minecraft:redstone_ore;#FF0000",
            "minecraft:lapis_ore;#0000FF",
            "minecraft:copper_ore;#FFA500",
            "minecraft:nether_quartz_ore;#FFF0DC",
            "minecraft:nether_gold_ore;#FFA500",
            "minecraft:ancient_debris;#804020",
            "minecraft:obsidian;#200040",
            "minecraft:deepslate_coal_ore;#101010",
            "minecraft:deepslate_iron_ore;#BFBFBF",
            "minecraft:deepslate_gold_ore;#FFD700",
            "minecraft:deepslate_diamond_ore;#00CCCC",
            "minecraft:deepslate_emerald_ore;#009900",
            "minecraft:deepslate_redstone_ore;#990000",
            "minecraft:deepslate_lapis_ore;#000099",
            "minecraft:deepslate_copper_ore;#FFA500"
    );

    public static void parseSelectedBlocks() {
        MINERAL_BLOCKS.clear();
        for (String entry : selectedBlocksToHighLight) {
            String[] parts = entry.split(";");
            if (parts.length != 2) continue;

            String blockId = parts[0].trim();
            if (!blockId.contains(":")) {
                blockId = "minecraft:" + blockId;
            }
            String hexColor = parts[1].trim();

            // 解析颜色
            Color color = parseHexColor(hexColor);
            if (color == null) continue;

            // 解析方块
            Optional<Block> block = Registries.BLOCK.getOrEmpty(Identifier.of(blockId));
            if (block.isEmpty()) continue; // 确保是有效的方块

            MINERAL_BLOCKS.put(block.get(), color);
        }
    }

    private static Color parseHexColor(String hex) {
        try {
            if (!hex.startsWith("#") || hex.length() != 7) {
                System.err.println("Bad HexColor: " + hex);
                return new Color(255, 255, 255);
            }


            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);

            return new Color(r, g, b);
        } catch (Exception e) {
            System.err.println("parse HexColor faild: " + hex);
            return new Color(255, 255, 255);
        }

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
