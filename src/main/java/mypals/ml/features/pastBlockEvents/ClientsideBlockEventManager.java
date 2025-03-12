package mypals.ml.features.pastBlockEvents;

import mypals.ml.features.selectiveRendering.AreaBox;
import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.Text;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static mypals.ml.config.LucidityConfig.*;


public class ClientsideBlockEventManager {
    private static ConcurrentHashMap<Long, List<BlockEvent>> blockEventGroups = new ConcurrentHashMap<>();
    public static void onClientTick(){
        removeExpired(renderBlockEventsExpiredTime);
        for(List<BlockEvent> blockEvents : blockEventGroups.values()) {
            for (int i = 0; i < blockEvents.size(); i++) {
                BlockEvent blockEvent = blockEvents.get(i);
                InformationRender.addAreaBox(new AreaBox(blockEvent.pos(),blockEvent.pos(), renderBlockEventsColor,
                        renderBlockEventsColor.getAlpha()/255.0f, false));
                int finalI = i;
                InformationRender.addText(new Text(
                        new ArrayList<String>() {{
                            add("Order:" + finalI);
                            add(net.minecraft.text.Text.translatable(blockEvent.block().getTranslationKey()).getString());
                            add(blockEvent.pos().getX() + ", " + blockEvent.pos().getY() + ", " + blockEvent.pos().getZ());
                            add("Type:" + blockEvent.type());
                            add("Data:" + blockEvent.data());
                        }}, blockEvent.pos().toCenterPos(), 0.01f,
                        new ArrayList<Color>() {{
                            add(Color.white);
                            add(Color.red);
                            add(Color.yellow);
                            add(Color.cyan);
                            add(Color.green);
                        }}, 1, true)
                );
            }
        }
    }
    public static void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data) {
        MinecraftClient.getInstance().execute(()->{
            long time = Util.getMeasuringTimeMs();
            if(blockEventGroups.containsKey(time)){
                blockEventGroups.get(time).add(new BlockEvent(pos,block,type,data));
            }else {
                List<BlockEvent> l = new ArrayList<>();
                l.add(new BlockEvent(pos, block, type, data));
                blockEventGroups.put(time, l);
            }
        });

    }
    public static void removeExpired(double expiry) {
        long time = Util.getMeasuringTimeMs();
        List<Long> toRemoves = new ArrayList<>();
        for(Map.Entry entry : blockEventGroups.entrySet()){
            if((double)(time - (long)entry.getKey()) > expiry){
                toRemoves.add((long)entry.getKey());
            }
        }
        toRemoves.forEach(toRemove -> {
            blockEventGroups.remove(toRemove);
        });
    }
}
