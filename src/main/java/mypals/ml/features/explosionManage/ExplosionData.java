package mypals.ml.features.explosionManage;

import net.minecraft.util.math.Vec3d;

public class ExplosionData {
    private final Vec3d position;
    private final float strength;

    public ExplosionData(Vec3d position, float strength) {
        this.position = position;
        this.strength = strength;
    }

    public Vec3d getPosition() {
        return position;
    }

    public float getStrength() {
        return strength;
    }
}
