package mypals.ml.rendering.shapes;

import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.List;

public class Text {
    public List<String> texts;
    public Vec3d pos;
    public float size;
    public List<Color> color;
    public float alpha;
    public boolean seeThrough;
    public Text(List<String> text,Vec3d pos,float size,List<Color> color,float alpha,boolean seeThrough){
        this.texts = text;
        this.pos = pos;
        this.size = size;
        this.color = color;
        this.alpha = alpha;
        this.seeThrough = seeThrough;
    }
}
