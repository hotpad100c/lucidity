package mypals.ml.features.explosionVisualizer.data;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ExplosionData {
    private final Vec3d position;
    private final float strength;

    public ExplosionData(Vec3d position, float strength) {
        this.position = position;
        this.strength = strength;
    }

    public Vec3d getPosition() {
        return position;
    }

    public float getStrength() {
        return strength;
    }
    public static class ExplosionAffectedObjects{
        public final List<BlockPos> blocksToDestriy;
        public final List<BlockPos> blocksShouldBeFine;
        public final List<Vec3d> explotionCenters;
        public final List<ExplosionCastLine> blockDestructionRays;
        public final List<EntityToDamage> entityToDamage;
        public final List<EntityToDamage.SamplePointData> samplePointData;

        public ExplosionAffectedObjects(List<BlockPos> blocksToDestriy, List<BlockPos> blocksShouldBeFine, List<EntityToDamage> entityToDamage, List<Vec3d> explotionCenters, List<ExplosionCastLine> blockDestructionRays) {
            this.blockDestructionRays = new ArrayList<ExplosionCastLine>();
            this.explotionCenters = new ArrayList<Vec3d>();
            this.blocksToDestriy = new ArrayList<BlockPos>();
            this.blocksShouldBeFine = new ArrayList<BlockPos>();
            this.entityToDamage = new ArrayList<EntityToDamage>();
            this.samplePointData = new ArrayList<EntityToDamage.SamplePointData>();
        }
        public List<BlockPos> getBlocksToDestriy() {return blocksToDestriy;}
        public List<BlockPos> getBlocksShouldBeFine() {return blocksToDestriy;}
        public List<EntityToDamage> getEntitysToDamage() {
            return entityToDamage;
        }
        public List<EntityToDamage.SamplePointData> getSamplePointData() {return samplePointData;}
        public List<Vec3d> getExplotionCenters() {
            return explotionCenters;
        }
        public List<ExplosionCastLine> getExplotionCastedLines() {
            return blockDestructionRays;
        }

        public static class EntityToDamage {
            public final Entity entityToDamage;
            public final float Damage;
            public final SamplePointData samplePointData;
            public EntityToDamage(Entity entityToDamage, float damage, SamplePointData samplePointData) {
                this.entityToDamage = entityToDamage;
                Damage = damage;
                this.samplePointData = samplePointData;
            }
            public Entity getEntity() {
                return entityToDamage;
            }

            public float getDamage() {
                return Damage;
            }
            public SamplePointData getPointData() {
                return samplePointData;
            }
            public static class SamplePointData {
                public final List<RayCastData> castPointData;

                public SamplePointData(List<RayCastData> castPointData) {
                    this.castPointData = castPointData;
                }
                public List<RayCastData> getCastPointData()
                {
                    return castPointData;
                }

                public static class RayCastData {
                    public Vec3d point;
                    public Vec3d point_hit;
                    public boolean hit_target;
                    public RayCastData(Vec3d point, Vec3d point_hit, boolean hit_target) {
                        this.point = point;
                        this.point_hit =  point_hit;
                        this.hit_target = hit_target;
                    }
                    public Vec3d getPoint()
                    {
                        return point;
                    }
                    public Vec3d getCollitionPoint()
                    {
                        return point_hit;
                    }
                    public boolean getHitTarget()
                    {
                        return hit_target;
                    }
                }
            }
        }
        public static class ExplosionCastLine {
            public final int lineColor;
            public final List<CastPoint> points;

            public ExplosionCastLine(int c, List<CastPoint> pointList){
                this.lineColor = c;
                this.points = pointList;
            }

            public int getLineColor() {
                return lineColor;
            }
            public List<CastPoint> getPoints() {
                return points;
            }

            public static class CastPoint {
                public final Vec3d position;
                public final float strength;

                public CastPoint(Vec3d p, float s){
                    this.position = p;
                    this.strength = s;
                }

                public Vec3d getPosition() {
                    return position;
                }
                public float getStrength() {
                    return strength;
                }
            }
        }
    }
    public class FakeExplosion {
        public final float x, y, z, power;
        public final boolean ignorBlockInside;
        public final String name;

        public FakeExplosion(float x, float y, float z, float power, boolean ignoreBlockInside, String name) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
            this.ignorBlockInside = ignoreBlockInside;
            this.name = name;
        }
    }
}
