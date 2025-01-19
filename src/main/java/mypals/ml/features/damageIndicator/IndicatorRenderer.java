package mypals.ml.features.damageIndicator;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.config.LucidityConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;

import static mypals.ml.Lucidity.MOD_ID;

public class IndicatorRenderer {
    public static Identifier indicatorTexture = Identifier.of(MOD_ID,"textures/gui/indicator/damage_indicator.png");
    public static void renderIndicators(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.options.hudHidden && client.options.getPerspective().isFirstPerson()) {
            LucidityConfig.CONFIG_HANDLER.instance();
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            float r = LucidityConfig.indicatorColor.getRed() / 255.0F;
            float g = LucidityConfig.indicatorColor.getGreen() / 255.0F;
            float b = LucidityConfig.indicatorColor.getBlue() / 255.0F;

            DamageHandler.indicaors.forEach(indicator -> {
                Vec3d playerPos = client.player.getPos();
                Vec3d damagePos = indicator.isSourceMovable && indicator.source != null
                        ? indicator.source.getAttacker().getPos()
                        : indicator.pos;

                double finalAngle = calculateFinalAngle(client.player.getRotationVec(1.0F), playerPos, damagePos);
                float radians = (float) Math.toRadians(finalAngle);
                float radians2 = (float) Math.toRadians(finalAngle - 90.0D);

                int textureWidth = 100;
                int distanceFromCenter = (int) LucidityConfig.indicatorOffset;

                int x = client.getWindow().getScaledWidth() / 2;
                int y = client.getWindow().getScaledHeight() / 2;
                float indicatorX = x + (float) (distanceFromCenter * Math.cos(radians2));
                float indicatorY = y + (float) (distanceFromCenter * Math.sin(radians2));

                float lifeTimeAlphaFactor = (float) indicator.lifeTime / LucidityConfig.damageIndicatorLifeTime;
                float a = MathHelper.clamp(lifeTimeAlphaFactor, 0.0F, LucidityConfig.indicatorColor.getAlpha() / 255.0F);

                context.setShaderColor(r, g, b, a);

                context.getMatrices().push();
                context.getMatrices().translate(indicatorX, indicatorY, 0.0F);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotation(radians));
                context.getMatrices().translate(-indicatorX, -indicatorY, 0.0F);

                RenderSystem.enableBlend();
                context.drawTexture(indicatorTexture,
                        (int) (indicatorX - textureWidth / 2), (int) (indicatorY - textureWidth / 2), 0, 0, textureWidth, textureWidth, textureWidth, textureWidth);
                RenderSystem.disableBlend();
                context.getMatrices().pop();
                context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            });
        }
    }
    private static double calculateFinalAngle(Vec3d playerForward, Vec3d playerPosition, Vec3d damagePosition) {
        double playerAngle = Math.atan2(playerForward.x, playerForward.z) * 180.0D / Math.PI;
        Vector2d enemyVec = new Vector2d(damagePosition.x - playerPosition.x, damagePosition.z - playerPosition.z);
        double enemyAngle = Math.atan2(enemyVec.x, enemyVec.y) * 180.0D / Math.PI;
        double finalAngle = enemyAngle - playerAngle;
        return -finalAngle;
    }
}
