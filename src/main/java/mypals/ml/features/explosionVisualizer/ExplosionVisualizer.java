package mypals.ml.features.explosionVisualizer;

import mypals.ml.features.explosionVisualizer.data.ExplosionData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public class ExplosionVisualizer {
    public static Set<BlockPos> blocksToDestroy = new HashSet<>();
    public static Set<Vec3d> explotionCenters = new HashSet<>();
    public static Set<ExplosionData.ExplosionAffectedObjects.EntityToDamage> entitysToDamage = new HashSet<>();

    public static  Set<ExplosionData.ExplosionAffectedObjects.EntityToDamage.SamplePointData> samplePointDatas = new HashSet<>();

    public static  Set<ExplosionData.FakeExplosion> fakeExplosions = new HashSet<>();

    public static Set<ExplosionData.ExplosionAffectedObjects.ExplosionCastLine> explosionCastedLines = new HashSet<>();



}
