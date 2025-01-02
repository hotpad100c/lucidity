package mypals.ml.rendering.glowingMarker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

import static mypals.ml.rendering.ShapeRender.getVertexConsumer;

public class BeamRenderer {
    private static final Identifier GLASS_BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/beacon_beam.png");
    private static final double MAX_HEIGHT = 60.0; // Example height
    private static final double BEAM_RADIUS = 0.5;

    public static void renderBeam(MatrixStack poseStack, float partialTick) {
        Color beamColor = Color.WHITE;
        if (beamColor.equals(Color.WHITE)) return;

        RenderLayer renderType = RenderLayer.getEntityTranslucent(GLASS_BEAM_TEXTURE);
        VertexConsumerProvider.Immediate immediate = getVertexConsumer();
        VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getDebugQuads());

        poseStack.push();
        poseStack.translate(0.0D, 0.0D, 0.0D);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));

        renderGlassBeam(vertexConsumer, poseStack, beamColor);

        poseStack.pop();
        immediate.draw(renderType);
    }

    private static void renderGlassBeam(VertexConsumer vertexConsumer, MatrixStack poseStack, Color color) {
        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        float alpha = 0.6f;

        MatrixStack.Entry matrixEntry = poseStack.peek();
        for (int i = 0; i < 4; i++) {
            float angle1 = (float) (i * Math.PI / 2);
            float angle2 = (float) ((i + 1) * Math.PI / 2);
            float x1 = (float) (Math.cos(angle1) * BEAM_RADIUS);
            float z1 = (float) (Math.sin(angle1) * BEAM_RADIUS);
            float x2 = (float) (Math.cos(angle2) * BEAM_RADIUS);
            float z2 = (float) (Math.sin(angle2) * BEAM_RADIUS);

            vertexConsumer.vertex(matrixEntry.getPositionMatrix(), x1, 0, z1).color(red, green, blue, alpha).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(240).normal(matrixEntry, 0, 1, 0);
            vertexConsumer.vertex(matrixEntry.getPositionMatrix(), x2, 0, z2).color(red, green, blue, alpha).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(240).normal(matrixEntry, 0, 1, 0);
            vertexConsumer.vertex(matrixEntry.getPositionMatrix(), x2, (float) MAX_HEIGHT, z2).color(red, green, blue, alpha).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(240).normal(matrixEntry, 0, 1, 0);
            vertexConsumer.vertex(matrixEntry.getPositionMatrix(), x1, (float) MAX_HEIGHT, z1).color(red, green, blue, alpha).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(240).normal(matrixEntry, 0, 1, 0);
        }
    }

}
