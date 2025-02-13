package mypals.ml.features.specialStructureDisplays.treeStructures;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.foliage.CherryFoliagePlacer;

public class TreeGrowingBox {
    public enum PartType {
        TRUNK,
        FOLIAGE,
        ROOT
    }

    private final BlockPos pos1;
    private final BlockPos pos2;
    private final PartType partType;

    public TreeGrowingBox(BlockPos pos1, BlockPos pos2, PartType partType) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.partType = partType;
    }

    public BlockPos getPos1() {
        return pos1;
    }

    public BlockPos getPos2() {
        return pos2;
    }

    public PartType getPartType() {
        return partType;
    }
}