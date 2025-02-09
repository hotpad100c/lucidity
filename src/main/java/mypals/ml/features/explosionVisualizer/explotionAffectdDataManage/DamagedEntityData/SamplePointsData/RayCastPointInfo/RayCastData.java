package mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.SamplePointsData.RayCastPointInfo;

import net.minecraft.util.math.Vec3d;

public class RayCastData {
    public Vec3d point;
    public Vec3d point_hit;
    public boolean hit_target;
    public RayCastData(Vec3d point, Vec3d point_hit, boolean hit_target) {
        this.point = point;
        this.point_hit =  point_hit;
        this.hit_target = hit_target;
    }
    public Vec3d getPoint()
    {
        return point;
    }
    public Vec3d getCollitionPoint()
    {
        return point_hit;
    }
    public boolean getHitTarget()
    {
        return hit_target;
    }
}
