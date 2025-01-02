package mypals.ml.rendering.shapes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class ShineMarker {
    public Vec3d pos;
    public Color color;
    public float size;
    public long seed;
    public int lights;
    public float speed;
    public int lifeTime;
    public ShineMarker(Vec3d pos, Color color, float size, float speed, int lights,long seed) {
        this.pos = pos;
        this.color = color;
        this.size = size;
        this.speed = speed;
        this.lights = lights;
        this.seed = seed;
    }

}
