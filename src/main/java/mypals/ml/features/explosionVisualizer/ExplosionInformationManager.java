package mypals.ml.features.explosionVisualizer;

import mypals.ml.config.LucidityConfig;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.EntityToDamage;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.SamplePointsData.RayCastPointInfo.RayCastData;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionCastLines.PointsOnLine.CastPoint;
import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.CubeShape;
import mypals.ml.rendering.shapes.LineShape;
import mypals.ml.rendering.shapes.TextShape;
import net.minecraft.block.AirBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.*;
import java.util.List;

import static mypals.ml.config.LucidityConfig.*;

public class ExplosionInformationManager {
    public static Set<BlockPos> blocksToDestroy = new HashSet<>();
    public static Set<BlockPos> blocksCantDestroy = new HashSet<>();

    public static Set<Vec3d> explotionCenters = new HashSet<>();
    public static Set<EntityToDamage> entitysToDamage = new HashSet<>();
    public static Set<SamplePointData> samplePointData = new HashSet<>();

    public static Set<ExplosionCastLine> explosionCastedLines = new HashSet<>();
    public static void resolveExplosionInformations() {
        BlockPos pos = new BlockPos(5, 0, 0);

        if (blocksToDestroy != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if(showBlockDestroyInfo) {
                for (BlockPos p : blocksToDestroy) {
                    // Render each affected block
                    InformationRender.addText(new TextShape(
                            new ArrayList<>(Collections.singletonList(BlockDestroyIcon)),
                            p.toCenterPos(),
                            LucidityConfig.BlockDestroyIconSize,
                            new ArrayList<>(Collections.singletonList(BlockDestroyIconColor)),
                            1,
                            LucidityConfig.BlockDestroyIconSeeThrow
                    ));
                    InformationRender.addCube(new CubeShape(p,0.2f,LucidityConfig.BlockDestroyIconColor,LucidityConfig.BlockDestroyIconSeeThrow));
                }
                for (BlockPos p: blocksCantDestroy){
                    InformationRender.addText(new TextShape(
                            new ArrayList<>(Collections.singletonList(BlockDestroyIcon)),
                            p.toCenterPos(),
                            LucidityConfig.BlockDestroyIconSize,
                            new ArrayList<>(List.of(invertColor(BlockDestroyIconColor))),
                            1,
                            LucidityConfig.BlockDestroyIconSeeThrow
                    ));
                    InformationRender.addCube(new CubeShape(p,0.2f,invertColor(LucidityConfig.BlockDestroyIconColor),LucidityConfig.BlockDestroyIconSeeThrow));

                }
            }
            if(showDamageInfo) {
                for (EntityToDamage e : entitysToDamage) {
                    if (e.getEntity() instanceof LivingEntity livingEntity) {
                        float health = livingEntity.getHealth() / 2.0f;

                        float height = livingEntity.getHeight();

                        float damage = e.getDamage() / 2.0f;
                        float remainingHealth = Math.max(health - damage, 0) > 0? Math.max(health - damage, 0) : 0;

                        String s = health + "♡" + " - ≈" + damage + "♡";
                        String s2 = remainingHealth > 0 ? "≈ " + remainingHealth + "♡" : "DIE?";

                        float damageFactor = Math.max(health - damage, 0);
                        int red = (int) (255 * (1 -damageFactor));
                        int green = (int) (255 * damageFactor);
                        int color = (red << 16) | (green << 8);

                        InformationRender.addText(new TextShape(
                                new ArrayList<>(List.of(s)),
                                e.getEntity().getPos().add(0, height + 0.5, 0),
                                0.015F,
                                new ArrayList<>(List.of(new Color(color))),
                                1,
                                false
                        ));
                        InformationRender.addText(new TextShape(
                                new ArrayList<>(List.of(s2)),
                                e.getEntity().getPos().add(0, height, 0),
                                0.015F,
                                new ArrayList<>(List.of(new Color(color))),
                                1,
                                false
                        ));
                    }


                }
            }
            for (ExplosionCastLine l : explosionCastedLines) {
                int color = l.getLineColor();
                for (int i = 0;i<l.getPoints().size()-1;i++) {
                    CastPoint p = l.getPoints().get(i);
                    CastPoint p_last = l.getPoints().get(Math.max(i - 1, 0));
                    if(LucidityConfig.EnableAlpha)
                        color = mapAlpha(color, l.getOrgBlastStrength(), p.getStrength());
                    Color c = new Color(color, true);
                    if(p.getStrength() > 0 && !(MinecraftClient.getInstance().world.getBlockState(BlockPos.ofFloored(p.getPosition())).getBlock() instanceof AirBlock))
                        InformationRender.addText(new TextShape(
                                new ArrayList<>(Collections.singletonList(BlockDetectionRayIcon)),
                                p.getPosition(),
                                LucidityConfig.BlockDetectionRayIconSize,
                                new ArrayList<>(List.of(c)),
                                1,
                                LucidityConfig.BlockDetectionRaySeeThrow
                        ));
                }
                if(!l.getPoints().isEmpty())
                    InformationRender.addLine(new LineShape(l.getPoints().getFirst().getPosition(),
                            l.getPoints().getLast().getPosition(),new Color(color),LucidityConfig.EnableAlpha ?0.2f:1f, BlockDetectionRaySeeThrow));
            }
            for (Vec3d p : explotionCenters) {
                String s = "⦿";
                InformationRender.addText(new TextShape(
                        new ArrayList<>(List.of(s)),
                        p,
                        0.045F,
                        new ArrayList<>(List.of(LucidityConfig.Single_Color)),
                        1,
                        true
                ));
            }
            if(samplePointData != null && showRayCastInfo) {
                for(SamplePointData d : samplePointData) {
                    if(d != null) {
                        for(RayCastData r :  d.getCastPointData()) {
                            Vec3d org = r.point;
                            Vec3d collitionPoint = r.point_hit;
                            boolean hit_target = r.hit_target;
                            if (hit_target) {
                                InformationRender.addText(new TextShape(
                                        new ArrayList<>(Collections.singletonList(EntitySamplePoion_Safe_Icon)),
                                        org,
                                        LucidityConfig.EntitySamplePoionIconSize,
                                        new ArrayList<>(Collections.singletonList(EntitySamplePoion_Safe_IconColor)),
                                        1,
                                                LucidityConfig.EntitySamplePointSeeThrow
                                ));
                                InformationRender.addLine(new LineShape(org, r.point_hit,LucidityConfig.EntitySamplePoion_Safe_IconColor,0.5f,LucidityConfig.EntitySamplePointSeeThrow));

                            }
                            else {

                                InformationRender.addText(new TextShape(
                                        new ArrayList<>(Collections.singletonList(EntitySamplePoion_Danger_Icon)),
                                        org,
                                        LucidityConfig.EntitySamplePoionIconSize,
                                        new ArrayList<>(Collections.singletonList(EntitySamplePoion_Danger_IconColor)),
                                        1,
                                        LucidityConfig.EntitySamplePointSeeThrow
                                ));
                                InformationRender.addText(new TextShape(
                                        new ArrayList<>(Collections.singletonList(EntitySamplePoion_Blocked_Icon)),
                                        collitionPoint,
                                        LucidityConfig.EntitySamplePoionIconSize-0.005F,
                                        new ArrayList<>(Collections.singletonList(EntitySamplePoion_Blocked_IconColor)),
                                        1,
                                        LucidityConfig.EntitySamplePointSeeThrow
                                ));
                                InformationRender.addLine(new LineShape(org, collitionPoint,LucidityConfig.EntitySamplePoion_Blocked_IconColor,0.5f,LucidityConfig.EntitySamplePointSeeThrow));
                            }
                        }
                    }
                }

            }

        }

    }
    public static int addAlphaWithDecay(int rgb, double value) {
        double k = 0.5;

        int alpha = (int) (255 * (1 - Math.exp(-k * value)));

        if (alpha < 0) alpha = 0;
        if (alpha > 255) alpha = 255;


        int rgba = (alpha << 24) | (rgb & 0xFFFFFF);

        return rgba;
    }
    public static int mapAlpha(int rgb, double max, double v) {

        v = Math.max(0, Math.min(v, max));

        int alpha = (int) ((v / (float) max) * 255);

        int rgba = (alpha << 24) | (rgb & 0xFFFFFF);
        return rgba;
    }
    public static Color invertColor(Color color) {
        int r = 255 - color.getRed();
        int g = 255 - color.getGreen();
        int b = 255 - color.getBlue();
        return new Color(r, g, b);
    }
    public static void setBlocksToDestroy(Set<BlockPos> blocks) {
        blocksToDestroy = blocks;}
    public static void setEntitysToDamage(Set<EntityToDamage> e) {
        entitysToDamage = e;
    }
    public static void setBlocksCantDamage(Set<BlockPos> blocks) {
        blocksCantDestroy = blocks;
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

}
