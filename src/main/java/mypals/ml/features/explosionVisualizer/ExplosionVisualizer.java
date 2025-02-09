package mypals.ml.features.explosionVisualizer;

import mypals.ml.config.LucidityConfig;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.EntityToDamage;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionAffectedObjects;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionData;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.FakeExplosion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mypals.ml.config.LucidityConfig.enableExplosionVisualizer;
import static mypals.ml.features.explosionVisualizer.simulate.ExplosionSimulateManager.*;
import static mypals.ml.features.trajectory.TrajectoryManager.showInfo;

public class ExplosionVisualizer {
    public static Set<BlockPos> blocksToDestroy = new HashSet<>();
    public static Set<BlockPos> blocksCantDestroy = new HashSet<>();
    public static Set<Vec3d> explotionCenters = new HashSet<>();
    public static Set<EntityToDamage> entitysToDamage = new HashSet<>();

    public static  Set<SamplePointData> samplePointDatas = new HashSet<>();

    public static  Set<FakeExplosion> fakeExplosions = new HashSet<>();

    public static Set<ExplosionCastLine> explosionCastedLines = new HashSet<>();
    public static void FixRangeIssue()
    {
        LucidityConfig.CONFIG_HANDLER.instance();
        if(LucidityConfig.Xmax < LucidityConfig.Xmin)
        {
            LucidityConfig.Xmax = LucidityConfig.Xmin;
        }
        if(LucidityConfig.Ymax < LucidityConfig.Ymin)
        {
            LucidityConfig.Ymax = LucidityConfig.Ymin;
        }
        if(LucidityConfig.Zmax < LucidityConfig.Zmin)
        {
            LucidityConfig.Zmax = LucidityConfig.Zmin;
        }
        if(LucidityConfig.LayerMax < LucidityConfig.LayerMin)
        {
            LucidityConfig.LayerMax = LucidityConfig.LayerMin + 1;
        }
    }

    public static void tick(MinecraftClient client) {
        assert MinecraftClient.getInstance() != null;
        if (enableExplosionVisualizer) {
            try {
                explosionCastedLines.clear();
                blocksToDestroy.clear();
                blocksCantDestroy.clear();
                entitysToDamage.clear();
                explotionCenters.clear();
                samplePointDatas.clear();
                if (client.world != null && client.player != null) {
                    World world = client.world;
                    BlockPos playerPos = client.player.getBlockPos();


                    List<ExplosionData> exBlockPos = ExplosiveObjectFinder.findExplosiveBlocksInRange(world, playerPos);
                    List<ExplosionData> exEntityPos = ExplosiveObjectFinder.findExplosiveEntitysInRange(world, playerPos);

                    for (ExplosionData explotion : exBlockPos) {
                        Vec3d p_d = new Vec3d(explotion.getPosition().toVector3f());
                        Vec3i p_i = new Vec3i((int) p_d.x, (int) p_d.y, (int) p_d.z);
                        ExplosionAffectedObjects EAO = simulateExplosiveBlocks(world, new BlockPos(p_i), explotion.getStrength());
                        explosionCastedLines.addAll(EAO.getExplotionCastedLines());
                        blocksToDestroy.addAll(EAO.getBlocksToDestriy());
                        blocksCantDestroy.addAll(EAO.getBlocksShouldBeFine());
                        entitysToDamage.addAll(EAO.getEntitysToDamage());
                        explotionCenters.addAll(EAO.getExplotionCenters());
                        samplePointDatas.addAll(EAO.getSamplePointData());

                    }
                    for (ExplosionData explosion : exEntityPos) {
                        ExplosionAffectedObjects EAO = simulateExplosiveEntitys(explosion.getEntity(), world, explosion.getPosition(), explosion.getStrength());
                        explosionCastedLines.addAll(EAO.getExplotionCastedLines());
                        blocksToDestroy.addAll(EAO.getBlocksToDestriy());
                        blocksCantDestroy.addAll(EAO.getBlocksShouldBeFine());
                        entitysToDamage.addAll(EAO.getEntitysToDamage());
                        explotionCenters.addAll(EAO.getExplotionCenters());
                        samplePointDatas.addAll(EAO.getSamplePointData());
                    }
                    for (FakeExplosion fe : fakeExplosions) {
                        ExplosionAffectedObjects EAO = simulateFakeExplosions(world, new Vec3d(fe.x, fe.y, fe.z), fe.power, fe.ignorBlockInside);
                        explosionCastedLines.addAll(EAO.getExplotionCastedLines());
                        blocksToDestroy.addAll(EAO.getBlocksToDestriy());
                        blocksCantDestroy.addAll(EAO.getBlocksShouldBeFine());
                        entitysToDamage.addAll(EAO.getEntitysToDamage());
                        explotionCenters.addAll(EAO.getExplotionCenters());
                        samplePointDatas.addAll(EAO.getSamplePointData());
                    }
                    ExplosionInformationManager.setCastedLines(explosionCastedLines);
                    ExplosionInformationManager.setBlocksToDestroy(blocksToDestroy);
                    ExplosionInformationManager.setBlocksCantDamage(blocksCantDestroy);
                    ExplosionInformationManager.setEntitysToDamage(entitysToDamage);
                    ExplosionInformationManager.setExplotionCenters(explotionCenters);
                    ExplosionInformationManager.setSamplePointData(samplePointDatas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ExplosionInformationManager.resolveExplosionInformations();
    }

}
