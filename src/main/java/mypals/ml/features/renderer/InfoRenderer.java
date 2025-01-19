package mypals.ml.features.renderer;

import mypals.ml.config.LucidityConfig;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.DamagedEntityData.EntityToDamage;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.RayCastPointInfo.RayCastData;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.ExplosionCastLines.PointsOnLine.CastPoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

import static mypals.ml.config.LucidityConfig.*;
import static mypals.ml.features.mathSupport.MathHelp.addAlphaWithDecay;
import static mypals.ml.features.renderer.LineRenderer.renderSingleLine;


public class InfoRenderer {
    public static final String SHOULD_BE_FINE = Formatting.GREEN + "√";
    private static final String WILL_DESTROY = LucidityConfig.CONFIG_HANDLER.instance().BlockDestroyIconColor + "!";

    private static final InfoRenderer INSTANCE = new InfoRenderer();

    public static InfoRenderer getInstance() {
        return INSTANCE;
    }

    public static Set<BlockPos> blocksToDamage = new HashSet<>();

    public static Set<Vec3d> explotionCenters = new HashSet<>();
    public static Set<EntityToDamage> entitysToDamage = new HashSet<>();
    public static Set<SamplePointData> samplePointData = new HashSet<>();

    public static Set<ExplosionCastLine> explosionCastedLines = new HashSet<>();


    @SuppressWarnings("ConstantConditions")
    public static void render(MatrixStack matrixStack, RenderTickCounter counter, VertexConsumer buffer) {
        BlockPos pos = new BlockPos(5, 0, 0);

        if (blocksToDamage != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if( showBlockDestroyInfo) {
                for (BlockPos p : blocksToDamage) {
                    // Render each affected block
                    drawString(matrixStack, p, counter, LucidityConfig.CONFIG_HANDLER.instance().BlockDestroyIcon, LucidityConfig.CONFIG_HANDLER.instance().BlockDestroyIconColor.getRGB(), LucidityConfig.CONFIG_HANDLER.instance().BlockDestroyIconSize, LucidityConfig.CONFIG_HANDLER.instance().BlockDestroyIconSeeThrow);
                }
            }
            if(showDamageInfo) {
                for (EntityToDamage e : entitysToDamage) {
                    if (e.getEntity() instanceof LivingEntity livingEntity) {
                        // 获取当前健康值并将其转换为半颗心表示
                        float health = livingEntity.getHealth() / 2.0f;

                        // 获取高度
                        float height = livingEntity.getHeight();

                        // 计算伤害，并确保它不会被舍入多次
                        float damage = e.getDamage() / 2.0f;

                        // 计算伤害后的剩余健康值，确保不会低于0
                        float remainingHealth = Math.max(health - damage, 0) > 0? Math.max(health - damage, 0) : 0;

                        // 准备显示的文本
                        String s = health + "♡" + " - ≈" + damage + "♡";
                        String s2 = remainingHealth > 0 ? "≈ " + remainingHealth + "♡" : "DIE?";

                        // 根据伤害占最大生命值的比例计算颜色
                        float damageFactor = Math.max(health - damage, 0);
                        int red = (int) (255 * (1 -damageFactor));
                        int green = (int) (255 * damageFactor);
                        int color = (red << 16) | (green << 8);

                        // 绘制文本
                        drawString(matrixStack, e.getEntity().getPos().add(0, height + 0.5, 0), counter, s, color, 0.025F, false);
                        drawString(matrixStack, e.getEntity().getPos().add(0, height, 0), counter, s2, color, 0.025F, false);
                    }


                }
            }
            for (ExplosionCastLine l : explosionCastedLines) {
                int color = l.getLineColor();
                for (CastPoint p : l.getPoints()) {
                    if(LucidityConfig.CONFIG_HANDLER.instance().EnableAlpha)
                        color = addAlphaWithDecay(color, p.getStrength());
                    if(p.getStrength() > 0)
                        drawString(matrixStack, p.getPosition(), counter, LucidityConfig.CONFIG_HANDLER.instance().BlockDetectionRayIcon, color, LucidityConfig.CONFIG_HANDLER.instance().BlockDetectionRayIconSize,  LucidityConfig.CONFIG_HANDLER.instance().BlockDetectionRaySeeThrow);
                }
            }
            for (Vec3d v : explotionCenters) {
                int orangeColor = 16753920;
                String s = "\uD83D\uDCA5";
                drawString(matrixStack, v, counter, s, orangeColor, 0.045F, true);
            }
            if(samplePointData != null && showRayCastInfo)
            {
                for(SamplePointData d : samplePointData) {
                    if(d != null) {
                        for(RayCastData r :  d.getCastPointData()) {
                            Vec3d org = r.point;
                            Vec3d collitionPoint = r.point_hit;
                            boolean hit_target = r.hit_target;
                            if (hit_target) {
                                drawString(matrixStack, org, counter, LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePoion_Danger_Icon, LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePoion_Danger_IconColor.getRGB(), LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePoionIconSize,  LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePointSeeThrow);
                            }
                            else {
                                drawString(matrixStack, org, counter, LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePoion_Safe_Icon, LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePoion_Safe_IconColor.getRGB(), LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePoionIconSize,  LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePointSeeThrow);
                                drawString(matrixStack, collitionPoint, counter, LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePoion_Blocked_Icon, LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePoion_Blocked_IconColor.getRGB(), LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePoionIconSize - 0.005F,  LucidityConfig.CONFIG_HANDLER.instance().EntitySamplePointSeeThrow);
                            }
                        }
                    }
                }

            }
        }
    }
    public static void drawLine(MatrixStack stack, VertexConsumer buffer, Vec3d p1, Vec3d p2, int color, int a)
    {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        renderSingleLine(stack, buffer, (float) p1.x, (float) p1.y, (float) p1.z, (float) p2.x, (float) p2.y, (float) p2.z, r, g, b, a);
    }

    public static void setBlocksToDamage(Set<BlockPos> blocks) {blocksToDamage = blocks;}
    public static void setEntitysToDamage(Set<EntityToDamage> e) {
        entitysToDamage = e;
    }
    public static void setExplotionCenters(Set<Vec3d> v) {
        explotionCenters = v;
    }
    public static void setSamplePointData(Set<SamplePointData> d)
    {
        samplePointData = d;
    }
    public static void setCastedLines(Set<ExplosionCastLine> l)
    {
        explosionCastedLines = l;
    }

    public static void drawString(MatrixStack matrixStack, BlockPos pos, RenderTickCounter countr, String text, int color, float size, boolean seeThrow) {
        // Assuming StringDrawer.drawString is a method to draw text on the screen
        MinecraftClient client = MinecraftClient.getInstance();
        StringRenderer.renderText(matrixStack, countr, pos, text, color, size, seeThrow);
    }
    public static void drawString(MatrixStack matrixStack, Vec3d pos, RenderTickCounter countr, String text, int color, float size, boolean seeThrow) {
        // Assuming StringDrawer.drawString is a method to draw text on the screen
        MinecraftClient client = MinecraftClient.getInstance();
        StringRenderer.renderText(matrixStack, countr, pos, text, color, size, seeThrow);
    }
    public static void drawBox(MatrixStack matrixStack, Vec3d pos,RenderTickCounter countr, float size, int color)
    {
        float alpha = (color >> 24 & 0xFF) / 255.0f;
        float red = (color >> 16 & 0xFF) / 255.0f;
        float green = (color >> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        Vec3d min = new Vec3d(pos.x-size, pos.y-size,pos.z-size);
        Vec3d max = new Vec3d(pos.x+size, pos.y+size,pos.z+size);
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
    }

}
