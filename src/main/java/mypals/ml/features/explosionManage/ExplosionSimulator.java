package mypals.ml.features.explosionManage;

import mypals.ml.config.LucidityConfig;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.DamagedEntityData.EntityToDamage;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.RayCastPointInfo.RayCastData;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.ExplosionAffectedObjects;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.ExplosionCastLines.PointsOnLine.CastPoint;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.*;

import static mypals.ml.config.LucidityConfig.*;

public class ExplosionSimulator {

    private static boolean ignoreSelf = false;
    private final World world;
    private final double x, y, z;
    private final float power;
    private final ExplosionAffectedObjects affected = new ExplosionAffectedObjects(null,null,null, null, null);

    public ExplosionSimulator(boolean ignoreSelf, World world, float x, float y, float z, float power) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.power = power;
        this.ignoreSelf = ignoreSelf;
    }

    public void simulate() {
        collectBlocksAndDamageEntities();
    }

    public Optional<Float> getBlastResistance(BlockPos pos, BlockState blockState, FluidState fluidState) {
        return blockState.isAir() && fluidState.isEmpty() ? Optional.empty() : Optional.of(Math.max(blockState.getBlock().getBlastResistance(), fluidState.getBlastResistance()));
    }

    private void collectBlocksAndDamageEntities() {
        Set<BlockPos> blocksToDestroy = new HashSet<>();
        SamplePointData sampleData = new SamplePointData(new ArrayList<>());
        float blastRadius = this.power * 2.0F;
        int minX = MathHelper.floor(this.x - blastRadius - 1.0);
        int maxX = MathHelper.floor(this.x + blastRadius + 1.0);
        int minY = MathHelper.floor(this.y - blastRadius - 1.0);
        int maxY = MathHelper.floor(this.y + blastRadius + 1.0);
        int minZ = MathHelper.floor(this.z - blastRadius - 1.0);
        int maxZ = MathHelper.floor(this.z + blastRadius + 1.0);

        List<Entity> entities = this.world.getOtherEntities(null, new Box(minX, minY, minZ, maxX, maxY, maxZ));
        Vec3d explosionCenter = new Vec3d(this.x, this.y,this.z);
        this.affected.explotionCenters.add(explosionCenter);

        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    if (isEdge(x, y, z) && (showBlockDestroyInfo || showExplosionBlockDamageRayInfo)) {
                        processExplosion(ignoreSelf, explosionCenter,entities, x, y, z, blastRadius, blocksToDestroy);
                    }
                }
            }
        }

        this.affected.blocksToDestriy.addAll(blocksToDestroy);
        for (Entity e : entities) {
            if(showDamageInfo || showRayCastInfo) {
                float damage = calculateDamage(explosionCenter, power, e, sampleData);
                this.affected.entityToDamage.add(new EntityToDamage(e, damage, sampleData));
            }

        }
        for(EntityToDamage e : this.affected.entityToDamage)
        {
            this.affected.samplePointData.add(e.getPointData());
        }

    }

    private boolean isEdge(int x, int y, int z) {
        return x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15;
    }

    private void processExplosion(boolean ignoreSourcePos, Vec3d explosionSource, List<Entity> entities, int x, int y, int z, float blastRadius, Set<BlockPos> blocksToDestroy) {

        List<CastPoint> castedPoints = new ArrayList<>();

        int r = (x * 255) / 16;
        int g = (y * 255) / 16;
        int b = (z * 255) / 16;

        // 将红色、绿色和蓝色值组合成 RGB int
        int rgb = (r << 16) | (g << 8) | b;

        double dx = (double) ((float) x / 15.0F * 2.0F - 1.0F);
        double dy = (double) ((float) y / 15.0F * 2.0F - 1.0F);
        double dz = (double) ((float) z / 15.0F * 2.0F - 1.0F);
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= distance;
        dy /= distance;
        dz /= distance;

        float initialBlastStrength = this.power * (0.7F + this.world.random.nextFloat() * 0.6F);
        double currentX = this.x;
        double currentY = this.y;
        double currentZ = this.z;
        int layer = 0;
        for (float blastStrength = initialBlastStrength; blastStrength > 0.0F; blastStrength -= 0.22500001F) {
            BlockPos currentPos = BlockPos.ofFloored(currentX, currentY, currentZ);

            BlockState blockState = this.world.getBlockState(currentPos);
            FluidState fluidState = this.world.getFluidState(currentPos);

            if((inRange(x,y,z) && inLayer(layer)) && showExplosionBlockDamageRayInfo )
                castedPoints.add(new CastPoint(new Vec3d(currentX,currentY,currentZ), blastStrength));

            if (!this.world.isInBuildLimit(currentPos)) {
                break;
            }


            Optional<Float> blastResistance = getBlastResistance(currentPos, blockState, fluidState); // Mocking blast resistance

            if (blastResistance.isPresent()) {
                boolean isSourcePos = currentPos.equals(BlockPos.ofFloored(explosionSource));
                if (isSourcePos && ignoreSourcePos) {
                    blastStrength -=0;
                }
                else{
                    blastStrength -= (blastResistance.get() + 0.3F) * 0.3F;
                }
            }

            if (!currentPos.equals(BlockPos.ofFloored(explosionSource)) && blastStrength > 0.0F && !blockState.isAir() && fluidState.isEmpty()) {
                blocksToDestroy.add(currentPos);
            }

            currentX += dx * 0.30000001192092896;
            currentY += dy * 0.30000001192092896;
            currentZ += dz * 0.30000001192092896;
            layer++;

        }
        affected.blockDestructionRays.add(new ExplosionCastLine(rgb,castedPoints));
    }

    public float calculateDamage(Vec3d pos, float power, Entity entity,SamplePointData sampleData) {
        // 获取爆炸威力，并乘以一个常数因子
        float explosionPower = power * 2.0F;

        // 获取爆炸的位置
        Vec3d explosionPosition = pos;

        // 计算实体与爆炸位置之间的距离，并归一化
        double normalizedDistance = Math.sqrt(entity.squaredDistanceTo(explosionPosition)) / (double) explosionPower;

        BlockPos ignore = new BlockPos((BlockPos.ofFloored(explosionPosition)));
        // 计算暴露度影响，基于归一化距离
        double exposureEffect = (1.0 - normalizedDistance) * (double) getExposure(explosionPosition, entity, ignore, sampleData);

        // 计算最终伤害值
        return (float) ((exposureEffect * exposureEffect + exposureEffect) / 2.0 * 7.0 * (double) explosionPower + 1.0);
    }
    public static float getExposure(Vec3d source, Entity entity, BlockPos ignoreBlock, SamplePointData sampleData) {
        Box boundingBox = entity.getBoundingBox();
        double xStep = 1.0 / ((boundingBox.maxX - boundingBox.minX) * 2.0 + 1.0);
        double yStep = 1.0 / ((boundingBox.maxY - boundingBox.minY) * 2.0 + 1.0);
        double zStep = 1.0 / ((boundingBox.maxZ - boundingBox.minZ) * 2.0 + 1.0);
        double xOffset = (1.0 - Math.floor(1.0 / xStep) * xStep) / 2.0;
        double zOffset = (1.0 - Math.floor(1.0 / zStep) * zStep) / 2.0;

        if (xStep >= 0.0 && yStep >= 0.0 && zStep >= 0.0) {
            int visiblePointsCount = 0;
            int totalPointsCount = 0;

            for (double xFraction = 0.0; xFraction <= 1.0; xFraction += xStep) {
                for (double yFraction = 0.0; yFraction <= 1.0; yFraction += yStep) {
                    for (double zFraction = 0.0; zFraction <= 1.0; zFraction += zStep) {
                        double x = MathHelper.lerp(xFraction, boundingBox.minX, boundingBox.maxX);
                        double y = MathHelper.lerp(yFraction, boundingBox.minY, boundingBox.maxY);
                        double z = MathHelper.lerp(zFraction, boundingBox.minZ, boundingBox.maxZ);

                        Vec3d point = new Vec3d(x + xOffset, y, z + zOffset);

                        HitResult hitResult = entity.getWorld().raycast(new RaycastContext(point, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));

                        if (hitResult.getType() == HitResult.Type.MISS) {
                            visiblePointsCount++;
                            sampleData.castPointData.add(new RayCastData(point, source, true));
                        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                            BlockPos hitPos = ((BlockHitResult) hitResult).getBlockPos();

                            // 判断是否是爆炸源方块
                            if (ignoreBlock.equals(hitPos) && ignoreSelf) {
                                visiblePointsCount++;
                                sampleData.castPointData.add(new RayCastData(point, source, true));
                            }
                            else {
                                sampleData.castPointData.add(new RayCastData(point, hitResult.getPos(), false));
                            }

                        }

                        totalPointsCount++;
                    }
                }
            }

            return (float) visiblePointsCount / (float) totalPointsCount;
        } else {
            return 0.0F;
        }
    }

    public ExplosionAffectedObjects getAffected() {
        return affected;
    }
    public boolean inRange(int x, int y, int z)
    {
        if(LucidityConfig.CONFIG_HANDLER.instance().Invert)
            return !((Xmin <= x && Xmax >= x) && (Ymin <= y && Ymax >= y) && (Zmin <= z && Zmax >= z));
        else
            return (Xmin <= x && Xmax >= x) && (Ymin <= y && Ymax >= y) && (Zmin <= z && Zmax >= z);
    }
    public boolean inLayer(int layer)
    {

        return (LayerMin<= layer && LayerMax >= layer);
    }
}