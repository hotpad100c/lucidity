package mypals.ml.rendering.shapes;

import com.sun.jna.platform.win32.WinDef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

public class MultiPointLine {
    public ArrayList<Vec3d> points = new ArrayList<Vec3d>();
    public Color color;
    public float alpha;
    public boolean seeThrough;
    public MultiPointLine(ArrayList<Vec3d> points, Color color, float alpha, boolean seeThrough) {
        this.points = points;
        this.alpha = alpha;
        this.color = color;
        this.seeThrough = seeThrough;
    }
    public void draw(MatrixStack matrixStack,ArrayList<Vec3d> points, @Nullable Color color, @Nullable Float alpha,boolean seeThrough) {
        MinecraftClient client = MinecraftClient.getInstance();
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d start = points.get(i);
            Vec3d end = points.get(i + 1);
            LineShape.draw(matrixStack, start, end, color == null?this.color:color, alpha == null?this.alpha:alpha,seeThrough);
        }
    }
}
