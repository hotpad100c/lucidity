package mypals.ml.rendering.shapes;

import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class BoxShape{
    public BlockPos pos;
    public float length;
    public float weigth;
    public float height;
    public float alpha;
    public Color color;
    public BoxShape(BlockPos pos, float length, float weigth, float height,Color color,float alpha){
        this.pos = pos;
        this.length = length;
        this.weigth = weigth;
        this.height = height;
        this.color = color;
        this.alpha = alpha;
    }
}
