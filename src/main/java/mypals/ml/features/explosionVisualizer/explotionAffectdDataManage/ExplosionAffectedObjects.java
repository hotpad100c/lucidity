package mypals.ml.features.explosionVisualizer.explotionAffectdDataManage;

import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.EntityToDamage;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ExplosionAffectedObjects {
    public final List<BlockPos> blocksToDestriy;
    public final List<BlockPos> blocksShouldBeFine;
    public final List<Vec3d> explotionCenters;
    public final List<ExplosionCastLine> blockDestructionRays;
    public final List<EntityToDamage> entityToDamage;
    public final List<SamplePointData> samplePointData;

    public ExplosionAffectedObjects(List<BlockPos> blocksToDestriy, List<BlockPos> blocksShouldBeFine, List<EntityToDamage> entityToDamage, List<Vec3d> explotionCenters, List<ExplosionCastLine> blockDestructionRays) {
        this.blockDestructionRays = new ArrayList<ExplosionCastLine>();
        this.explotionCenters = new ArrayList<Vec3d>();
        this.blocksToDestriy = new ArrayList<BlockPos>();
        this.blocksShouldBeFine = new ArrayList<BlockPos>();
        this.entityToDamage = new ArrayList<EntityToDamage>();
        this.samplePointData = new ArrayList<SamplePointData>();
    }
    public List<BlockPos> getBlocksToDestriy() {return blocksToDestriy;}
    public List<BlockPos> getBlocksShouldBeFine() {return blocksShouldBeFine;}
    public List<EntityToDamage> getEntitysToDamage() {
        return entityToDamage;
    }
    public List<SamplePointData> getSamplePointData() {return samplePointData;}
    public List<Vec3d> getExplotionCenters() {
        return explotionCenters;
    }
    public List<ExplosionCastLine> getExplotionCastedLines() {
        return blockDestructionRays;
    }

}
