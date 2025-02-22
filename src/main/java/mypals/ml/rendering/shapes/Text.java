package mypals.ml.rendering.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.rendering.ShapeRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import javax.lang.model.type.ArrayType;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static mypals.ml.rendering.ShapeRender.*;

public class Text {
    public ArrayList<String> texts;
    public Vec3d pos;
    public float size;
    public ArrayList<Color> color;
    public float alpha;
    public boolean seeThrough;
    public Text(ArrayList<String> text,Vec3d pos,float size,ArrayList<Color> color,float alpha,boolean seeThrough){
        this.texts = text;
        this.pos = pos;
        this.size = size;
        this.color = color;
        this.alpha = alpha;
        this.seeThrough = seeThrough;
    }
    public void draw(MatrixStack matrices, float size) {
        draw(matrices,this.pos, 0,this.texts,this.color,this.size,this.seeThrough);
    }
    public static void draw(MatrixStack matrices, Vec3d textPos, float tickDelta, ArrayList<String> texts, List<Color> colors, float size, boolean seeThrough) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();

        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            matrices.push();
            double lastTickPosX = camera.getPos().getX();
            double lastTickPosY = camera.getPos().getY();
            double lastTickPosZ = camera.getPos().getZ();
            float x = (float) (textPos.getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
            float y = (float) (textPos.getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
            float z = (float) (textPos.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));

            matrices.translate(x, y, z);
            matrices.multiply(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
            matrices.scale(size, -size, 1);
            Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            RenderSystem.disableDepthTest();

            float totalHeight = 0.0F;
            for (String text : texts) {
                totalHeight += textRenderer.getWrappedLinesHeight(text, Integer.MAX_VALUE) * 1.25F;
            }

            float renderYBase = -totalHeight / 2.0F; // 起始位置，从底部开始
            for (int i = 0; i < texts.size(); i++) {
                float renderX = -textRenderer.getWidth(texts.get(i)) * 0.5F; // 居中
                float renderY = renderYBase + textRenderer.getWrappedLinesHeight(texts.get(i), Integer.MAX_VALUE) * 1.25F * i;
                VertexConsumerProvider.Immediate immediate = getVertexConsumer();
                if(seeThrough)
                    textRenderer.draw(
                            texts.get(i), renderX, renderY, colors.get(i) != null? colors.get(i).getRGB() : Color.white.getRGB(), true,
                            modelViewMatrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0,
                            0xF000F0
                    );
                else
                    textRenderer.draw(
                            texts.get(i), renderX, renderY, colors.get(i) != null? colors.get(i).getRGB() : Color.white.getRGB(), true,
                            modelViewMatrix, immediate, TextRenderer.TextLayerType.NORMAL, 0,
                            0xF000F0
                    );

                immediate.draw();
            }
            matrices.pop();

            RenderSystem.enableDepthTest();
        }
    }
}
