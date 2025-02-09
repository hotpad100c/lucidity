package mypals.ml.features.explosionVisualizer.explotionAffectdDataManage;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ExplosionData {

    @Nullable
    private final Entity me;
    private final Vec3d position;
    private final float strength;

    public ExplosionData(Entity me, Vec3d position, float strength) {
        this.me = me;
        this.position = position;
        this.strength = strength;
    }

    public Vec3d getPosition() {
        return position;
    }

    public float getStrength() {
        return strength;
    }
    public Entity getEntity(){return me;}
}
