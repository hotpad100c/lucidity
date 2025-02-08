package mypals.ml.rendering.shapes;

import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

public class MultiPointLine {
    public ArrayList<Vec3d> points = new ArrayList<Vec3d>();
    public Color color;
    public float alpha;
    public MultiPointLine(ArrayList<Vec3d> points, Color color,float alpha) {
        this.points = points;
        this.alpha = alpha;
        this.color = color;
    }
}
