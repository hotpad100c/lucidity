package mypals.ml.features.specialStructureDisplays.treeStructures;

import mypals.ml.features.selectiveRendering.AreaBox;
import mypals.ml.rendering.InformationRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.List;

public class TreeSearchManager {
    public static void onClientTick() {
        // 获取玩家所在的世界
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return;
        }

        // 获取玩家所在的位置
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();

        // 搜索树的盒子
        List<TreeGrowingBox> treeBoxes = TreeSearcher.searchAndMergeTreeBoxes(world, playerPos);

        // 绘制树的盒子
        for (TreeGrowingBox treeBox : treeBoxes) {
            BlockPos pos1 = treeBox.getPos1();
            BlockPos pos2 = treeBox.getPos2();
            Color color = Color.GREEN;
            if(treeBox.getPartType() == TreeGrowingBox.PartType.TRUNK) {
                color = new Color(139, 69, 19);
            }else if (treeBox.getPartType() == TreeGrowingBox.PartType.FOLIAGE){
                color = Color.GREEN;
            }else{

            }
            AreaBox box = new AreaBox(pos1,pos2,color,0.2f,false);
            // 绘制盒子
            InformationRender.addAreaBox(box);
        }
    }
}
