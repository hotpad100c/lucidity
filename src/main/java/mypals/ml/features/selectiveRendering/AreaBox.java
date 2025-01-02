package mypals.ml.features.selectiveRendering;

import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class AreaBox {
    public BlockPos minPos;
    public BlockPos maxPos;

    public Color color;
    public AreaBox(BlockPos a, BlockPos b,Color color){
        this.minPos = new BlockPos(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        );
        this.maxPos = new BlockPos(
                Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ())
        );
        this.color = color;
    }
}
