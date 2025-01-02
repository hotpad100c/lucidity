package mypals.ml.rendering.shapes;

import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class CubeShape {
    public BlockPos pos;
    public float alpha;
    public Color color;

    public boolean seeThrough;
    public CubeShape(BlockPos pos, float alpha, Color color, boolean seeThrough) {
        this.pos = pos;
        this.alpha = alpha;
        this.color = color;
        this.seeThrough = seeThrough;
    }
}
