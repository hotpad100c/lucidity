package mypals.ml.features.renderMobSpawn;

import mypals.ml.config.LucidityConfig;
import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.CubeShape;
import mypals.ml.rendering.shapes.TextShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SpaceScanner {
    public static void findExposedTopSurfaces(World world, BlockPos center, int range) {

        int minX = center.getX()-range;
        int maxX = center.getX()+range;
        int minY = center.getY()-range;
        int maxY = center.getY()+range;
        int minZ = center.getZ()-range;
        int maxZ = center.getZ()+range;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    ArrayList<String> mobs = PossibleSpawnChecker.getPossibleSpawnsAt((ClientWorld) world, pos);
                    if(!mobs.isEmpty()) {
                        InformationRender.addCube(new CubeShape(pos,0.05f,Color.yellow,true));
                        InformationRender.addText(new TextShape(mobs, pos.toCenterPos(), LucidityConfig.renderMobSpawnSize, assignRainbowColors(mobs), 1, true));
                    }
                    /*if (!state.isAir() && (!above.isSolidBlock(world,pos.up()) || above.isAir() )) {
                        List<String> mobs = PossibleSpawnChecker.getPossibleSpawnsAt((ClientWorld) world, pos.up());
                        if(!mobs.isEmpty()) {
                            InformationRender.addText(new Text(mobs, pos.toCenterPos(), LucidityConfig.renderMobSpawnSize, assignRainbowColors(mobs), 1, true));
                            InformationRender.addCube(new CubeShape(pos,0.05f,Color.yellow,true));
                        }
                    }*/
                }
            }
        }
    }
    public static void addSpawnDataToInformationRenderer(){
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        findExposedTopSurfaces(world, client.player.getBlockPos(), LucidityConfig.renderMobSpawnRange);
    }
    public static ArrayList<Color> assignRainbowColors(List<String> items) {
        ArrayList<Color> result = new ArrayList<>(items.size());
        int n = items.size();
        if (n == 0) {
            return result;
        }

        for (int i = 0; i < n; i++) {

            float fraction = (float) i / (float) n;

            int rgb = Color.HSBtoRGB(fraction, 1.0f, 1.0f);

            Color color = new Color(rgb);
            result.add(color);
        }
        return result;
    }

}
