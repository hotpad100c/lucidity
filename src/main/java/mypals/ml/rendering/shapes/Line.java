package mypals.ml.rendering.shapes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class Line {
    public Vec3d start;
    public Vec3d end;
    public float alpha;
    public Color color;
    public Line(Vec3d start, Vec3d end, Color color,float alpha) {
        this.start = start;
        this.end = end;
        this.alpha = alpha;
        this.color = color;
    }
}
