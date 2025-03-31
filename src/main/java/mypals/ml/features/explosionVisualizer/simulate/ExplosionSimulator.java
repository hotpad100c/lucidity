package mypals.ml.features.explosionVisualizer.simulate;

import mypals.ml.config.LucidityConfig;
import mypals.ml.features.explosionVisualizer.explosoionBehaviors.*;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.*;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.EntityToDamage;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.SamplePointsData.RayCastPointInfo.RayCastData;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionAffectedObjects;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionCastLines.PointsOnLine.CastPoint;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.BreezeWindChargeEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static mypals.ml.config.LucidityConfig.*;

public class ExplosionSimulator {

    @Nullable
    private static Entity thisEntity;
    private static boolean ignorSelf = false;
    private final World world;
    private final double x, y, z;
    private final float power;
    private final ExplosionAffectedObjects affected = new ExplosionAffectedObjects(null,null,null, null, null);

    public ExplosionSimulator(Entity thisEntity, boolean ignorSelf, World world, float x, float y, float z, float power) {
        this.thisEntity = thisEntity;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.power = power;
        this.ignorSelf = ignorSelf;
    }

    public void simulate() {
        collectBlocksAndDamageEntities();
    }

    public Optional<Float> getBlastResistance(BlockPos pos, BlockState blockState, FluidState fluidState) {
        return blockState.isAir() && fluidState.isEmpty() ? Optional.empty() : Optional.of(Math.max(blockState.getBlock().getBlastResistance(), fluidState.getBlastResistance()));
    }

    private void collectBlocksAndDamageEntities() {
        Set<BlockPos> blocksToDestroy = new HashSet<>();
        Set<BlockPos> blocksShouldBeFine = new HashSet<>();
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
                        processExplosion(thisEntity, ignorSelf, explosionCenter,entities, x, y, z, blastRadius, blocksToDestroy, blocksShouldBeFine);
                    }
                }
            }
        }

        this.affected.blocksToDestriy.addAll(blocksToDestroy);
        this.affected.blocksShouldBeFine.addAll(blocksShouldBeFine);
        for (Entity e : entities) {
            if(showDamageInfo || showRayCastInfo) {
                if(e != thisEntity){
                    float damage = calculateDamage(explosionCenter, power, e, sampleData);
                    this.affected.entityToDamage.add(new EntityToDamage(e, damage, sampleData));
                }
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

    private int getColorForRays(Vec3i p,int colorWith)
    {
        LucidityConfig.CONFIG_HANDLER.instance();
        int rgb = 0;
        switch(colorWith){
            case 0:
                int r = (p.getX() * 255) / 16;
                int g = (p.getY() * 255) / 16;
                int b = (p.getZ() * 255) / 16;
                // 将红色、绿色和蓝色值组合成 RGB int
                rgb = (r << 16) | (g << 8) | b;
                break;
            case 1:
                if(p.getX() == 0)
                    rgb = LucidityConfig.Colored_FRONT.getRGB();

                else if(p.getX() == 15)
                    rgb = LucidityConfig.Colored_BACK.getRGB();

                else if(p.getY() == 0)
                    rgb = LucidityConfig.Colored_DOWN.getRGB();

                else if(p.getY() == 15)
                    rgb = LucidityConfig.Colored_UP.getRGB();

                else if(p.getZ() == 0)
                    rgb = LucidityConfig.Colored_LEFT.getRGB();

                else if(p.getZ() == 15)
                    rgb = LucidityConfig.Colored_RIGHT.getRGB();

                break;
            case 2:
                rgb = LucidityConfig.Single_Color.getRGB();
                break;
        }
        return rgb;
    }
    public static Text getColoringTypeForRays(int colorWith)
    {
        Text type = Text.literal("UNKNOWN!");
        switch (colorWith){
            case 0:
                type = Text.translatable("config.option.type.direction_based");
                break;
            case 1:
                type = Text.translatable("config.option.type.face_based");
                break;
            case 2:
                type = Text.translatable("config.option.type.single_color");
                break;
        }
        return type;
    }
    private void processExplosion(Entity thisEntity, boolean ignorScourcePos, Vec3d explotionScource, List<Entity> entities, int x, int y, int z, float blastRadius, Set<BlockPos> blocksToDestroy, Set<BlockPos> blocksShouldBeFine) {

        List<CastPoint> castedPoints = new ArrayList<>();

        LucidityConfig.CONFIG_HANDLER.instance();
        int rgb = getColorForRays(new Vec3i(x, y ,z), LucidityConfig.ColorType);

        double dx = (float) x / 15.0F * 2.0F - 1.0F;
        double dy = (float) y / 15.0F * 2.0F - 1.0F;
        double dz = (float) z / 15.0F * 2.0F - 1.0F;
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

            if((inRange(x,y,z) && inLayer(layer)) && showExplosionBlockDamageRayInfo &&
                    !(new Vec3d(explotionScource.x, explotionScource.y, explotionScource.z).equals(new Vec3d(currentX, currentY, currentZ)))
                    && !(ignorScourcePos && currentPos.equals(BlockPos.ofFloored(explotionScource))))
                castedPoints.add(new CastPoint(new Vec3d(currentX,currentY,currentZ), blastStrength));

            if (!this.world.isInBuildLimit(currentPos)) {
                break;
            }

            Optional<Float> blastResistance = Optional.of(0F);
            //----------------

            if(thisEntity!=null)
            {
                if(thisEntity instanceof TntMinecartEntity)
                {
                    TntMinecartExplosionBehaviorManager EManager = new TntMinecartExplosionBehaviorManager();
                    blastResistance = EManager.getBlastResistance(world, currentPos,world.getBlockState(currentPos), world.getFluidState(currentPos));
                }
                else if(thisEntity instanceof WitherSkullEntity)
                {
                    WitherSkullExplosionBehaviorManager EManager = new WitherSkullExplosionBehaviorManager();
                    EManager.witherSkull = (WitherSkullEntity)thisEntity;
                    blastResistance = EManager.getBlastResistance(world, currentPos,world.getBlockState(currentPos), world.getFluidState(currentPos));
                }
                else if(thisEntity instanceof WindChargeEntity || thisEntity instanceof BreezeWindChargeEntity){
                    WindChargeExplosionBehaviorManager EManager = new WindChargeExplosionBehaviorManager(true);
                    blastResistance = EManager.getBlastResistance(world, currentPos,world.getBlockState(currentPos), world.getFluidState(currentPos));
                }
                else{
                    EntityExplosionBehaviorManager EManager = new EntityExplosionBehaviorManager();
                    blastResistance = EManager.getBlastResistance(world, currentPos,world.getBlockState(currentPos), world.getFluidState(currentPos));
                }

            }else{
                ExplosionBehaviorManager EManager = new ExplosionBehaviorManager();
                blastResistance = EManager.getBlastResistance(world, currentPos,world.getBlockState(currentPos), world.getFluidState(currentPos));
            }

            //---------------

            if (blastResistance.isPresent()) {
                boolean isSourcePos = currentPos.equals(BlockPos.ofFloored(explotionScource));
                if (isSourcePos && ignorScourcePos) {
                    blastStrength -= 0F;
                }
                else{
                    blastStrength -= (blastResistance.get() + 0.3F) * 0.3F;
                }
            }
            //!currentPos.equals(BlockPos.ofFloored(explotionScource)) &&
            if (blastStrength > 0.0F && !blockState.isAir() && fluidState.isEmpty()) {
                //Can destroy!
                if(thisEntity != null)
                {
                    if(thisEntity instanceof TntMinecartEntity) {
                        //For TntMine cart entities
                        TntMinecartExplosionBehaviorManager EManager = new TntMinecartExplosionBehaviorManager();
                        if (EManager.canExplosionDestroyBlock(world, currentPos, world.getBlockState(currentPos))) {
                            //Can destroy
                            blocksToDestroy.add(currentPos);
                        } else {
                            // Should destroy but cant :(
                            blocksShouldBeFine.add(currentPos);
                        }
                    }
                    else if(thisEntity instanceof WindChargeEntity || thisEntity instanceof BreezeWindChargeEntity){
                        if(WindChargeTrigger.canTriggerBlocks(currentPos,world))
                        {
                            blocksToDestroy.add(currentPos);
                        }else{
                            blocksShouldBeFine.add(currentPos);
                        }
                    } else{
                        if(!(ignorScourcePos && currentPos.equals(BlockPos.ofFloored(explotionScource))))
                            //For other entities
                            blocksToDestroy.add(currentPos);
                    }
                }else {
                    if(!(ignorScourcePos && currentPos.equals(BlockPos.ofFloored(explotionScource))))
                        //For blosks
                        blocksToDestroy.add(currentPos);
                }
            }

            currentX += dx * 0.30000001192092896;
            currentY += dy * 0.30000001192092896;
            currentZ += dz * 0.30000001192092896;
            layer++;

        }
        affected.blockDestructionRays.add(new ExplosionCastLine(initialBlastStrength,rgb,castedPoints));
    }

    public float calculateDamage(Vec3d pos, float power, Entity entity,SamplePointData sampleData) {
        // 获取爆炸威力，并乘以一个常数因子
        float explosionPower = power * 2.0F;

        // 获取爆炸的位置
        Vec3d explosionPosition = pos;

        double normalizedDistance = Math.sqrt(entity.squaredDistanceTo(explosionPosition)) / (double) explosionPower;

        BlockPos ignor = new BlockPos((BlockPos.ofFloored(explosionPosition)));

        double exposureEffect = (1.0 - normalizedDistance) * (double) getExposure(explosionPosition, entity, ignor, sampleData);

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
                        boolean isClientPlayer = (entity instanceof ClientPlayerEntity) && !MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson();
                        if (hitResult.getType() == HitResult.Type.MISS) {
                            visiblePointsCount++;
                            if(!(entity instanceof ClientPlayerEntity) || !isClientPlayer){
                                sampleData.castPointData.add(new RayCastData(point, source, true));
                            }

                        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                            BlockPos hitPos = ((BlockHitResult) hitResult).getBlockPos();

                            // 判断是否是爆炸源方块
                            if (ignoreBlock.equals(hitPos) && ignorSelf) {
                                visiblePointsCount++;
                                if(!(entity instanceof ClientPlayerEntity) ||!isClientPlayer) {
                                    sampleData.castPointData.add(new RayCastData(point, source, true));
                                }
                            }
                            else {
                                if(!(entity instanceof ClientPlayerEntity) || !isClientPlayer) {
                                    sampleData.castPointData.add(new RayCastData(point, hitResult.getPos(), false));
                                }
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
        LucidityConfig.CONFIG_HANDLER.instance();
        if(LucidityConfig.invert)
            return !((Xmin <= x && Xmax >= x) && (Ymin <= y && Ymax >= y) && (Zmin <= z && Zmax >= z));
        else
            return (Xmin <= x && Xmax >= x) && (Ymin <= y && Ymax >= y) && (Zmin <= z && Zmax >= z);
    }
    public boolean inLayer(int layer)
    {

        return (LayerMin<= layer && LayerMax >= layer);
    }
}