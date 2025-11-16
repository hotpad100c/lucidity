package mypals.ml.features.mobFollowRange;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.config.LucidityConfig;
import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.LineShape;
import mypals.ml.rendering.shapes.OnGroundMarker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mypals.ml.config.LucidityConfig.renderMobChaseRange;

public class MobFollowRangeScanner {

    public static void onClientTick(int scanRadius){
        World world = MinecraftClient.getInstance().world;
        BlockPos centerPos = MinecraftClient.getInstance().player.getBlockPos();
        for(Map.Entry entry : scanNearbyMobFollowRanges(world, centerPos,scanRadius).entrySet()){
            BlockPos surfacePos = (BlockPos) entry.getValue();
            InformationRender.addOnGroundMarker(new OnGroundMarker(surfacePos,Color.ORANGE,0.3f,false));
        }
    }
    public static Map<BlockPos, BlockPos> scanNearbyMobFollowRanges(World world, BlockPos centerPos, int scanRadius) {
        Map<BlockPos, BlockPos> blockSurfaceMap = new HashMap<>();

        Box searchBox = new Box(
                centerPos.add(-scanRadius, -scanRadius, -scanRadius).toCenterPos(),
                centerPos.add(scanRadius, scanRadius, scanRadius).toCenterPos()
        );

        List<MobEntity> mobs = world.getEntitiesByClass(MobEntity.class, searchBox, mob ->
                mob instanceof HostileEntity || mob instanceof IronGolemEntity);

        List<LivingEntity> potentialTargets = world.getEntitiesByClass(LivingEntity.class, searchBox, entity ->
                entity instanceof PlayerEntity || entity instanceof SnowGolemEntity || entity instanceof VillagerEntity || entity instanceof IronGolemEntity);

        for (MobEntity mob : mobs) {
            double followRange = getFollowRangeEstimate(mob);
            if(renderMobChaseRange) {

                BlockPos mobPos = mob.getBlockPos();

                List<BlockPos> circleBlocks = getCircleBlocks(mobPos, followRange);
                for (BlockPos blockPos : circleBlocks) {
                    BlockPos surfacePos = findClosestSurface(world, blockPos);
                    boolean pathfindThrough = mob.getWorld().getBlockState(blockPos).canPathfindThrough(NavigationType.LAND);
                    if (surfacePos != null && pathfindThrough) {
                        blockSurfaceMap.put(blockPos, surfacePos);
                    }
                }
            }

            if (LucidityConfig.renderMobEyeLineConnection) {
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player == null) return blockSurfaceMap;

                for (LivingEntity target : potentialTargets) {
                    HitResult hitResult = canSeeTarget(mob, target);
                    float delta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
                    Vec3d mobEyePos = mob.getLerpedPos(delta).add(0,mob.getEyeHeight(mob.getPose()),0);
                    if(mob.distanceTo(target) < followRange){
                        if(target == player){
                            Vec3d targetPos = getTargetPosition(player);



                            if (hitResult.getType() == HitResult.Type.MISS) {
                                InformationRender.addLine(new LineShape(mobEyePos, targetPos, Color.GREEN, 1f, true));
                            }else {
                                InformationRender.addLine(new LineShape(mobEyePos, hitResult.getPos(), Color.RED, 1, true));
                            }
                        }else {
                            if (mob != target && hitResult.getType() == HitResult.Type.MISS) {
                                InformationRender.addLine(new LineShape(mobEyePos, target.getEyePos(), Color.GREEN, 1, true));
                            } else {
                                InformationRender.addLine(new LineShape(mobEyePos, hitResult.getPos(), Color.RED, 1, true));
                            }
                        }
                    }

                }
            }
        }

        return blockSurfaceMap;
    }
    private static Vec3d getTargetPosition(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        float delta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
        Vec3d eyePos = player.getLerpedPos(delta).add(0,player.getEyeHeight(player.getPose()),0);

        if (client.options.getPerspective().isFirstPerson()) {
            Vec3d viewDirection = player.getRotationVec(1.0F);
            return eyePos.add(viewDirection.multiply(0.5));
        }
        return eyePos;
    }
    public static HitResult canSeeTarget(LivingEntity mob, LivingEntity target) {
        Vec3d mobEyes = mob.getEyePos();
        Vec3d targetPos = target.getEyePos();

        return mob.getWorld().raycast(new RaycastContext(
                mobEyes, targetPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mob));
    }
    private static double getFollowRangeEstimate(MobEntity mob) {
        return mob.getAttributeValue(EntityAttributes.FOLLOW_RANGE);
    }

    private static List<BlockPos> getCircleBlocks(BlockPos center, double radius) {
        List<BlockPos> blocks = new ArrayList<>();
        int r = (int) Math.ceil(radius);

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    blocks.add(center.add(dx, 0, dz));
                }
            }
        }

        return blocks;
    }


    private static BlockPos findClosestSurface(World world, BlockPos mobPos) {
        int startY = mobPos.getY();
        int maxY = Math.min(world.getHeight(), startY + 30); // 限制搜索范围
        int minY = Math.max(world.getBottomY(), startY - 30);

        for (int y = startY; y <= maxY; y++) {
            BlockPos checkPos = new BlockPos(mobPos.getX(), y, mobPos.getZ());
            BlockPos belowPos = checkPos.down();

            if (world.getBlockState(checkPos).isAir() && !world.getBlockState(belowPos).isAir()) {
                return belowPos; // 找到最近的表面
            }
        }

        // 若上方找不到，则向下查找
        for (int y = startY - 1; y >= minY; y--) {
            BlockPos checkPos = new BlockPos(mobPos.getX(), y, mobPos.getZ());
            BlockPos belowPos = checkPos.down();

            if (world.getBlockState(checkPos).isAir() && !world.getBlockState(belowPos).isAir()) {
                return belowPos; 
            }
        }

        return null; 
    }
}

