package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Random;

import static mypals.ml.rendering.ShapeRender.*;

public class ShineMarker {
    public Vec3d pos;
    public Boolean autoAlpha;
    public Color color;
    public float size;
    public long seed;
    public int lights;
    public float speed;
    public int lifeTime;
    public boolean seeThrough;
    public ShineMarker(Vec3d pos, Color color, float size, float speed, int lights,long seed,boolean autoAlpha,boolean seeThrough) {
        this.pos = pos;
        this.color = color;
        this.size = size;
        this.speed = speed;
        this.lights = lights;
        this.seed = seed;
        this.autoAlpha = autoAlpha;
        this.seeThrough = seeThrough;
    }
    public void draw(MatrixStack matrixStack,float time,int alpha,boolean seeThrough) {
        draw(matrixStack, this.pos, this.size, 0, time, this.lights, this.speed,2,
                this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.autoAlpha?alpha:255, this.seed,seeThrough);
    }
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);
    public static void draw(MatrixStack matrixStack, Vec3d pos, float size, float tickDelta, float time, int lights, float rotSpeed, float rotOffset,
                                        int colorR, int colorG, int colorB, int alpha, long seed,boolean seeThrough) {
        Random random = new Random(seed);
        float time1 = time * rotSpeed * 0.05F;

        Camera cam = MinecraftClient.getInstance().gameRenderer.getCamera();
        float scale = Math.min(size, calculateScale(MinecraftClient.getInstance().getCameraEntity().getPos(),pos))*
                (0.9F + random.nextFloat() * 0.2F);
        lastTickPosX = cam.getPos().getX();
        lastTickPosY = cam.getPos().getY();
        lastTickPosZ = cam.getPos().getZ();
        float x = (float) (pos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, cam.getPos().getX()));
        float y = (float) (pos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, cam.getPos().getY()));
        float z = (float) (pos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, cam.getPos().getZ()));

        matrixStack.push();
        matrixStack.translate(x,y,z);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        Vector3f vector3f = new Vector3f(0,0,0);
        Vector3f vector3f2 = new Vector3f();
        Vector3f vector3f3 = new Vector3f();
        Vector3f vector3f4 = new Vector3f();
        Quaternionf quaternionf = new Quaternionf();



        for (int i = 0; i < lights; i++) {
            matrixStack.push();
            quaternionf.rotationXYZ(
                    (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.sin(time1 * 0.1),
                    (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.cos(time1 * 0.1),
                    (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.sin(time1 * 0.2)
            ).rotateXYZ(
                    (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.cos(time1 * 0.15),
                    (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.sin(time1 * 0.15),
                    (random.nextFloat() * (float) (Math.PI * 2)) + (float) Math.cos(time1 * 0.2)
            );
            float length = random.nextFloat(0.6f,1f) * 2.0F;
            float width = random.nextFloat(0.6f,1f) * 6.0F;

            vector3f2.set(-HALF_SQRT_3 * length, width, -0.5F * length);
            vector3f3.set(HALF_SQRT_3 * length, width, -0.5F * length);
            vector3f4.set(0.0F, width, 0.5F* length);

            matrixStack.multiply(quaternionf);
            matrixStack.scale(scale,scale,scale);

            MatrixStack.Entry positionMatrix = matrixStack.peek();

            buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha);
            buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha);
            buffer.vertex(positionMatrix, vector3f2).color(colorR, colorG, colorB, 0);
            buffer.vertex(positionMatrix, vector3f3).color(colorR, colorG, colorB, 0);

            buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha);
            buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha);
            buffer.vertex(positionMatrix, vector3f3).color(colorR, colorG, colorB, 0);
            buffer.vertex(positionMatrix, vector3f4).color(colorR, colorG, colorB, 0);

            buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha);
            buffer.vertex(positionMatrix, vector3f).color(255, 255, 255, alpha);
            buffer.vertex(positionMatrix, vector3f4).color(colorR, colorG, colorB, 0);
            buffer.vertex(positionMatrix, vector3f2).color(colorR, colorG, colorB, 0);

            matrixStack.pop();
        }

        if(seeThrough)
            RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrixStack.pop();
    }


    public static float calculateScale(Vec3d viewPos,Vec3d pos) {
        double maxDist = 500;
        double dist = Math.min(viewPos.distanceTo(pos), maxDist);
        float f = (float) Math.pow(Math.sin(dist / maxDist * Math.PI), 0.5F);
        return f * 3F;
    }
    public static float calculateAlpha(Vec3d viewPos,Vec3d pos,double lifeTime){
        double a;
        double maxDist = 5.0;
        double dist = Math.min(viewPos.distanceTo(pos), maxDist);
        if (dist >= maxDist) {
            a = mapAlpha(lifeTime, 0, 30);
        } else {
            a = dist / maxDist;
        }
        return (float)a;
    }
    public static double mapAlpha(double x, float min, float max) {
        if (x < min) x = min;
        if (x > max) x = max;

        return (x - min) / (max - min);
    }
}
