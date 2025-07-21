package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class LineStrip {
    private static double lastTickPosX,lastTickPosY,lastTickPosZ;
    public ArrayList<Vec3d> points = new ArrayList<Vec3d>();
    public Color color;
    public float alpha;
    public boolean seeThrough;
    public LineStrip(ArrayList<Vec3d> points, Color color, float alpha, boolean seeThrough) {
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
    public static void drawLineStrips(MatrixStack matrixStack, java.util.List<LineStrip> lineStrips) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (!camera.isReady() || client.getEntityRenderDispatcher().gameOptions == null || client.player == null) {
            return;
        }

        Vec3d cameraPos = camera.getPos();
        matrixStack.push();

        //RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager._enableBlend();
        RenderSystem.lineWidth(2f);

        java.util.List<LineStrip> opaqueStrips = lineStrips.stream().filter(strip -> !strip.seeThrough && strip.points.size() > 1)
                .toList();
        java.util.List<LineStrip> seeThroughStrips = lineStrips.stream().filter(strip ->  strip.seeThrough && strip.points.size() > 1).collect(Collectors.toList());

        if (!opaqueStrips.isEmpty()) {

            for (LineStrip strip : opaqueStrips) {
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

                float red = ((strip.color.getRGB() >> 16) & 0xFF) / 255.0f;
                float green = ((strip.color.getRGB() >> 8) & 0xFF) / 255.0f;
                float blue = (strip.color.getRGB() & 0xFF) / 255.0f;

                for (Vec3d point : strip.points) {
                    lastTickPosX = camera.getPos().getX();
                    lastTickPosY = camera.getPos().getY();
                    lastTickPosZ = camera.getPos().getZ();
                    float x = (float) (point.getX() - MathHelper.lerp(0, lastTickPosX, camera.getPos().getX()));
                    float y = (float) (point.getY() - MathHelper.lerp(0, lastTickPosY, camera.getPos().getY()));
                    float z = (float) (point.getZ() - MathHelper.lerp(0, lastTickPosZ, camera.getPos().getZ()));
                    buffer.vertex(matrixStack.peek().getPositionMatrix(), x, y, z)
                            .color(red, green, blue, strip.alpha);
                }
                GlStateManager._enableDepthTest();
                RenderLayer.getDebugLineStrip(.2).draw(buffer.end());
            }


        }


        if (!seeThroughStrips.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

            for (LineStrip strip : seeThroughStrips) {
                float red = ((strip.color.getRGB() >> 16) & 0xFF) / 255.0f;
                float green = ((strip.color.getRGB() >> 8) & 0xFF) / 255.0f;
                float blue = (strip.color.getRGB() & 0xFF) / 255.0f;

                for (Vec3d point : strip.points) {
                    lastTickPosX = camera.getPos().getX();
                    lastTickPosY = camera.getPos().getY();
                    lastTickPosZ = camera.getPos().getZ();
                    float x = (float) (point.getX() - MathHelper.lerp(0, lastTickPosX, camera.getPos().getX()));
                    float y = (float) (point.getY() - MathHelper.lerp(0, lastTickPosY, camera.getPos().getY()));
                    float z = (float) (point.getZ() - MathHelper.lerp(0, lastTickPosZ, camera.getPos().getZ()));
                    buffer.vertex(matrixStack.peek().getPositionMatrix(), x, y, z)
                            .color(red, green, blue, strip.alpha);
                }
            }

            GlStateManager._disableDepthTest();
            RenderLayer.getDebugLineStrip(.2).draw(buffer.end());
            GlStateManager._enableDepthTest();
        }

        GlStateManager._disableBlend();
        matrixStack.pop();
    }
}
