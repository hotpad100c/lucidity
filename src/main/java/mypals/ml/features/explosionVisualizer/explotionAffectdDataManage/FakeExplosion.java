package mypals.ml.features.explosionVisualizer.explotionAffectdDataManage;

public class FakeExplosion {
    public final float x, y, z, power;
    public final boolean ignorBlockInside;
    public final String name;

    public FakeExplosion(float x, float y, float z, float power, boolean ignoreBlockInside, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.power = power;
        this.ignorBlockInside = ignoreBlockInside;
        this.name = name;
    }
}
