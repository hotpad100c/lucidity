package mypals.ml.features.selectiveRendering;

import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IntersectionResolver {
    public static boolean isOverlapping(AreaBox target, AreaBox cut) {
        return target.minPos.getX() <= cut.maxPos.getX() && target.maxPos.getX() >= cut.minPos.getX() &&
                target.minPos.getY() <= cut.maxPos.getY() && target.maxPos.getY() >= cut.minPos.getY() &&
                target.minPos.getZ() <= cut.maxPos.getZ() && target.maxPos.getZ() >= cut.minPos.getZ();
    }

    public static AreaBox getOverlap(AreaBox target, AreaBox cut) {
        if (!isOverlapping(target, cut)) {
            return null;
        }
        BlockPos overlapMin = new BlockPos(
                Math.max(target.minPos.getX(), cut.minPos.getX()),
                Math.max(target.minPos.getY(), cut.minPos.getY()),
                Math.max(target.minPos.getZ(), cut.minPos.getZ())
        );
        BlockPos overlapMax = new BlockPos(
                Math.min(target.maxPos.getX(), cut.maxPos.getX()),
                Math.min(target.maxPos.getY(), cut.maxPos.getY()),
                Math.min(target.maxPos.getZ(), cut.maxPos.getZ())
        );
        return new AreaBox(overlapMin, overlapMax, Color.RED,0.1f,false);
    }

    /*public static List<AreaBox> cutBox(AreaBox target, AreaBox cut) {
        List<AreaBox> result = new ArrayList<>();
        AreaBox overlap = getOverlap(target, cut);

        if (overlap == null) {
            result.add(target);
            return result;
        }

        if (overlap.minPos.getX() > target.minPos.getX()) {
            result.add(new AreaBox(
                    target.minPos,
                    new BlockPos(overlap.minPos.getX() - 1, target.maxPos.getY(), target.maxPos.getZ())
            ));
        }
        if (overlap.maxPos.getX() < target.maxPos.getX()) {
            result.add(new AreaBox(
                    new BlockPos(overlap.maxPos.getX() + 1, target.minPos.getY(), target.minPos.getZ()),
                    target.maxPos
            ));
        }
        if (overlap.minPos.getY() > target.minPos.getY()) {
            result.add(new AreaBox(
                    target.minPos,
                    new BlockPos(target.maxPos.getX(), overlap.minPos.getY() - 1, target.maxPos.getZ())
            ));
        }
        if (overlap.maxPos.getY() < target.maxPos.getY()) {
            result.add(new AreaBox(
                    new BlockPos(target.minPos.getX(), overlap.maxPos.getY() + 1, target.minPos.getZ()),
                    target.maxPos
            ));
        }
        if (overlap.minPos.getZ() > target.minPos.getZ()) {
            result.add(new AreaBox(
                    target.minPos,
                    new BlockPos(target.maxPos.getX(), target.maxPos.getY(), overlap.minPos.getZ() - 1)
            ));
        }
        if (overlap.maxPos.getZ() < target.maxPos.getZ()) {
            result.add(new AreaBox(
                    new BlockPos(target.minPos.getX(), target.minPos.getY(), overlap.maxPos.getZ() + 1),
                    target.maxPos
            ));
        }

        return result;
    }*/
    public static List<AreaBox> cutBox(AreaBox target, AreaBox cut) {
        List<AreaBox> result = new ArrayList<>();
        AreaBox overlap = getOverlap(target, cut);

        if (overlap == null) {
            result.add(target);
            return result;
        }

        // 分割为最多6个区域，逐维考虑
        if (target.minPos.getX() < overlap.minPos.getX()) {
            result.add(new AreaBox(
                    target.minPos,
                    new BlockPos(overlap.minPos.getX() - 1, target.maxPos.getY(), target.maxPos.getZ())
                    ,target.color,0.1f,false
            ));
        }
        if (target.maxPos.getX() > overlap.maxPos.getX()) {
            result.add(new AreaBox(
                    new BlockPos(overlap.maxPos.getX() + 1, target.minPos.getY(), target.minPos.getZ()),
                    target.maxPos
                    ,target.color,0.1f,false
            ));
        }

        if (target.minPos.getY() < overlap.minPos.getY()) {
            result.add(new AreaBox(
                    new BlockPos(Math.max(target.minPos.getX(), overlap.minPos.getX()), target.minPos.getY(), target.minPos.getZ()),
                    new BlockPos(Math.min(target.maxPos.getX(), overlap.maxPos.getX()), overlap.minPos.getY() - 1, target.maxPos.getZ())
                    ,target.color,0.1f,false
            ));
        }
        if (target.maxPos.getY() > overlap.maxPos.getY()) {
            result.add(new AreaBox(
                    new BlockPos(Math.max(target.minPos.getX(), overlap.minPos.getX()), overlap.maxPos.getY() + 1, target.minPos.getZ()),
                    new BlockPos(Math.min(target.maxPos.getX(), overlap.maxPos.getX()), target.maxPos.getY(), target.maxPos.getZ())
                    ,target.color,0.1f,false
            ));
        }

        if (target.minPos.getZ() < overlap.minPos.getZ()) {
            result.add(new AreaBox(
                    new BlockPos(Math.max(target.minPos.getX(), overlap.minPos.getX()), Math.max(target.minPos.getY(), overlap.minPos.getY()), target.minPos.getZ()),
                    new BlockPos(Math.min(target.maxPos.getX(), overlap.maxPos.getX()), Math.min(target.maxPos.getY(), overlap.maxPos.getY()), overlap.minPos.getZ() - 1)
                    ,target.color,0.1f,false
            ));
        }
        if (target.maxPos.getZ() > overlap.maxPos.getZ()) {
            result.add(new AreaBox(
                    new BlockPos(Math.max(target.minPos.getX(), overlap.minPos.getX()), Math.max(target.minPos.getY(), overlap.minPos.getY()), overlap.maxPos.getZ() + 1),
                    new BlockPos(Math.min(target.maxPos.getX(), overlap.maxPos.getX()), Math.min(target.maxPos.getY(), overlap.maxPos.getY()), target.maxPos.getZ())
                    ,target.color,0.1f,false
            ));
        }

        return result;
    }


}
