package mypals.ml.features.explosionManage;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;


import static net.minecraft.block.RespawnAnchorBlock.CHARGES;

public class ExplosiveObjectFinder {
    protected static final Random random = Random.create();;

    public static List<ExplosionData> findExplosiveBlocksInRange(World world, BlockPos playerPos) {
        List<ExplosionData> explosiveBlocks = new ArrayList<>();

        int range = 12; // 设置范围为24x24x24的一半
        if(!world.getDimension().respawnAnchorWorks()) {
            for (int x = playerPos.getX() - range; x <= playerPos.getX() + range; x++) {
                for (int y = playerPos.getY() - range; y <= playerPos.getY() + range; y++) {
                    for (int z = playerPos.getZ() - range; z <= playerPos.getZ() + range; z++) {
                        BlockPos bpos = new BlockPos(x, y, z);
                        BlockState bs = world.getBlockState(bpos);

                        if (bs.getBlock() == Blocks.RESPAWN_ANCHOR && !world.getDimension().respawnAnchorWorks()) {
                            if (bs.get(CHARGES) > 0) {
                                explosiveBlocks.add(new ExplosionData(Vec3d.of(bpos), 5.0f));
                            }

                        }

                        if (bs.isIn(BlockTags.BEDS) && !world.getDimension().bedWorks()) {
                            explosiveBlocks.add(new ExplosionData(Vec3d.of(bpos), 5.0f));

                        }

                    }
                }
            }
        }
        return explosiveBlocks;
    }
    public static List<ExplosionData> findCrystlesInRange(World world, BlockPos playerPos) {
        List<ExplosionData> explosiveEntitys = new ArrayList<>();

        int range = 12; // 设置范围为24x24x24的一半
        Box searchBox = new Box(Vec3d.of(playerPos.add(-range, -range, -range)), Vec3d.of(playerPos.add(range, range, range)));
        for (Entity entity : world.getEntitiesByType(EntityType.END_CRYSTAL, searchBox, e -> true)) {
            Vec3d crystalPos = entity.getPos();
            explosiveEntitys.add(new ExplosionData(crystalPos, 6.0f));
        }
        for (Entity entity : world.getEntitiesByType(EntityType.TNT, searchBox, e -> true)) {
            Vec3d tntPos = entity.getPos();
            double tntPosY = tntPos.getY() + (double)entity.getHeight() * 0.0625;
            explosiveEntitys.add(new ExplosionData(new Vec3d(tntPos.getX(), tntPosY, tntPos.getZ()), 4.0f));
        }
        for (Entity entity : world.getEntitiesByType(EntityType.WITHER_SKULL, searchBox, e -> true)) {
            Vec3d Pos = entity.getPos();
            explosiveEntitys.add(new ExplosionData(new Vec3d(Pos.getX(), Pos.getY(), Pos.getZ()), 1.0f));
        }
        for (Entity entity : world.getEntitiesByType(EntityType.FIREBALL, searchBox, e -> true)) {
            Vec3d Pos = entity.getPos();
            explosiveEntitys.add(new ExplosionData(new Vec3d(Pos.getX(), Pos.getY(), Pos.getZ()), 1.0f));
        }
        for (Entity entity : world.getEntitiesByType(EntityType.WITHER, searchBox, e -> true)) {
            Vec3d Pos = entity.getPos();
            if (entity instanceof WitherEntity wither)
            {
                if(wither.getInvulnerableTimer() > 0)
                    explosiveEntitys.add(new ExplosionData(new Vec3d(Pos.getX(), entity.getEyeY(), Pos.getZ()), 7.0f));
            }

        }
        for (Entity entity : world.getEntitiesByType(EntityType.TNT_MINECART, searchBox, e -> true)) {
            Vec3d tntCartPos = entity.getPos();
            double d = Math.sqrt(entity.getVelocity().horizontalLengthSquared());
            if (d > 5.0) {
                d = 5.0;
            }
            explosiveEntitys.add(new ExplosionData(tntCartPos, (float)(4.0 + random.nextDouble() * 1.5 * d)));
        }
        for (Entity entity : world.getEntitiesByType(EntityType.CREEPER, searchBox, e -> true)) {
            NbtCompound nbtData = new NbtCompound();
            entity.writeNbt(nbtData);
            int explosionRadius = nbtData.contains("ExplosionRadius") ? nbtData.getInt("ExplosionRadius") : 3;
            Vec3d crystalPos = entity.getPos();
            explosiveEntitys.add(new ExplosionData(crystalPos, explosionRadius));
        }

        return explosiveEntitys;
    }
}

