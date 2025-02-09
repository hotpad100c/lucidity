package mypals.ml.features.selectiveRendering;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.rendering.ShapeRender;
import mypals.ml.rendering.shapes.BoxShape;
import mypals.ml.rendering.shapes.LineShape;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
    public void draw(MatrixStack matrices, float alpha,boolean seeThrough) {
        draw(matrices, this.minPos, this.maxPos,0, this.color, alpha, seeThrough);
    }
    public void draw(MatrixStack matrices, Color color, float alpha,boolean seeThrough) {
        draw(matrices, this.minPos, this.maxPos,0, color, alpha, seeThrough);
    }
    public static void draw(MatrixStack matrices, BlockPos minPos, BlockPos maxPos, float tickDelta, Color color, float alpha, boolean seeThrough) {
        double midX = ((maxPos.getX() + 1f) + (minPos.getX())) / 2.0;
        double midY = ((maxPos.getY() + 1f) + (minPos.getY())) / 2.0;
        double midZ = ((maxPos.getZ() + 1f) + (minPos.getZ())) / 2.0;
        float length = Math.abs(maxPos.getX() - minPos.getX());
        float width = Math.abs(maxPos.getZ() - minPos.getZ());
        float height = Math.abs(maxPos.getY() - minPos.getY());
        Vec3d midpos = new Vec3d(midX, midY, midZ);

        Vec3d v1 = new Vec3d(minPos.getX(), minPos.getY(), minPos.getZ()); // 底面左下
        Vec3d v2 = new Vec3d(maxPos.getX()+1, minPos.getY(), minPos.getZ()); // 底面右下
        Vec3d v3 = new Vec3d(maxPos.getX()+1, minPos.getY(), maxPos.getZ()+1); // 底面右上
        Vec3d v4 = new Vec3d(minPos.getX(), minPos.getY(), maxPos.getZ()+1); // 底面左上

        Vec3d v5 = new Vec3d(minPos.getX(), maxPos.getY()+1, minPos.getZ()); // 顶面左下
        Vec3d v6 = new Vec3d(maxPos.getX()+1, maxPos.getY()+1, minPos.getZ()); // 顶面右下
        Vec3d v7 = new Vec3d(maxPos.getX()+1, maxPos.getY()+1, maxPos.getZ()+1); // 顶面右上
        Vec3d v8 = new Vec3d(minPos.getX(), maxPos.getY()+1, maxPos.getZ()+1); // 顶面左上


        // 绘制底面四条边
        LineShape.draw(matrices,  v1, v2,  color, 1,true);
        LineShape.draw(matrices,  v2, v3,  color, 1,true);
        LineShape.draw(matrices,  v3, v4,  color, 1,true);
        LineShape.draw(matrices,  v4, v1,  color, 1,true);

        // 绘制顶面四条边
        LineShape.draw(matrices,  v5, v6, color, 1,true);
        LineShape.draw(matrices,  v6, v7, color, 1,true);
        LineShape.draw(matrices,  v7, v8, color, 1,true);
        LineShape.draw(matrices,  v8, v5, color, 1,true);

        // 绘制四条竖线
        LineShape.draw(matrices,  v1, v5, color, 1,true);
        LineShape.draw(matrices,  v2, v6, color, 1,true);
        LineShape.draw(matrices,  v3, v7, color, 1,true);
        LineShape.draw(matrices,  v4, v8, color, 1,true);

        BoxShape.draw(matrices,midpos,length+1.01f,width+1.01f,height+1.01f,tickDelta,color,alpha,false);
    }
}
