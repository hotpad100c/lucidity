package mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionCastLines.PointsOnLine;

import net.minecraft.util.math.Vec3d;

public class CastPoint {
    public final Vec3d position;
    public final float strength;

    public CastPoint(Vec3d p, float s){
        this.position = p;
        this.strength = s;
    }

    public Vec3d getPosition() {
        return position;
    }
    public float getStrength() {
        return strength;
    }
}
