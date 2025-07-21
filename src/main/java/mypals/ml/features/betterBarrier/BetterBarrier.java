package mypals.ml.features.betterBarrier;

import mypals.ml.config.LucidityConfig;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import static mypals.ml.Lucidity.updateChunks;
import static mypals.ml.config.LucidityConfig.forceRenderTechnicalBlocks;

public class BetterBarrier {
    private static boolean betterBarrier = false;
    private static boolean betterStructureVoid = false;
    private static boolean betterLight = false;
    public static void checkForBetterRenderersEnabled(){
        if(shouldRenderBetterBarrier() != betterBarrier){
            betterBarrier = shouldRenderBetterBarrier();
            updateChunks(MinecraftClient.getInstance());
        }
        if(shouldRenderBetterStructureVoid() != betterStructureVoid){
            betterStructureVoid = shouldRenderBetterStructureVoid();
            updateChunks(MinecraftClient.getInstance());
        }
        if(shouldRenderBetterLight() != betterLight){
            betterLight = shouldRenderBetterLight();
            updateChunks(MinecraftClient.getInstance());
        }
    }
    public static boolean shouldRenderBetterBarrier(){
        if(MinecraftClient.getInstance() == null) return false;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return false;
        ItemStack stack = player.getMainHandStack();
        ItemStack stack2 = player.getOffHandStack();
        return ((stack.getItem().equals(Items.BARRIER) || stack2.getItem().equals(Items.BARRIER)) && LucidityConfig.betterBarrier) || forceRenderTechnicalBlocks;
    }
    public static boolean shouldRenderBetterStructureVoid(){
        if(MinecraftClient.getInstance() == null) return false;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return false;
        ItemStack stack = player.getMainHandStack();
        ItemStack stack2 = player.getOffHandStack();
        return ((stack.getItem().equals(Items.STRUCTURE_VOID) || stack2.getItem().equals(Items.STRUCTURE_VOID)) && LucidityConfig.betterStructureVoid) || forceRenderTechnicalBlocks;
    }
    public static boolean shouldRenderBetterLight(){
        if(MinecraftClient.getInstance() == null) return false;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return false;
        ItemStack stack = player.getMainHandStack();
        ItemStack stack2 = player.getOffHandStack();
        return ((stack.getItem().equals(Items.LIGHT) || stack2.getItem().equals(Items.LIGHT) && LucidityConfig.betterLight)) || forceRenderTechnicalBlocks;
    }
    public static void init(){
        BlockRenderLayerMap.putBlock(Blocks.BARRIER, BlockRenderLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(Blocks.STRUCTURE_VOID, BlockRenderLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(Blocks.LIGHT, BlockRenderLayer.TRANSLUCENT);
    }
}
