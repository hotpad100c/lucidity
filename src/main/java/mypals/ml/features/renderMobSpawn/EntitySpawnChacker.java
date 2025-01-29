package mypals.ml.features.renderMobSpawn;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.dimension.DimensionType;

import java.util.Set;

public class EntitySpawnChacker {
    public static final Set<EntityType<?>> OVERWORLD_ENTITIES = Set.of(
            EntityType.ARMADILLO,
            EntityType.BAT,
            EntityType.BOGGED,
            EntityType.CAVE_SPIDER,
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.CREEPER,
            EntityType.DONKEY,
            EntityType.FROG,
            EntityType.GOAT,
            EntityType.HORSE,
            EntityType.HUSK,
            EntityType.IRON_GOLEM,
            EntityType.LLAMA,
            EntityType.MOOSHROOM,
            EntityType.MULE,
            EntityType.PARROT,
            EntityType.PIG,
            EntityType.PILLAGER,
            EntityType.POLAR_BEAR,
            EntityType.RABBIT,
            EntityType.SHEEP,
            EntityType.SKELETON,
            EntityType.SKELETON_HORSE,
            EntityType.SLIME,
            EntityType.SPIDER,
            EntityType.STRAY,
            EntityType.TURTLE,
            EntityType.VILLAGER,
            EntityType.WITCH,
            EntityType.WOLF,
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.CAT,
            EntityType.EVOKER,
            EntityType.FOX,
            EntityType.PANDA,
            EntityType.RAVAGER,
            EntityType.VINDICATOR,
            EntityType.WANDERING_TRADER,
            EntityType.WARDEN
    );

    // 2) 下界（Nether）专属生物
    public static final Set<EntityType<?>> NETHER_ENTITIES = Set.of(
            EntityType.BLAZE,
            EntityType.GHAST,
            EntityType.HOGLIN,
            EntityType.MAGMA_CUBE,
            EntityType.PIGLIN,
            EntityType.STRIDER,
            EntityType.WITHER_SKELETON,
            EntityType.SKELETON,
            EntityType.ZOMBIFIED_PIGLIN
    );
    public static final Set<EntityType<?>> END_ENTITIES = Set.of(
            
    );
    public static final Set<EntityType<?>> UNIVERSAL_ENTITIES = Set.of(
            EntityType.ENDERMAN
    );
    public static boolean canSpawnInDark(EntityType<? extends HostileEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getDifficulty() != Difficulty.PEACEFUL
                && (isSpawnDark(world, pos, random))
                && canMobSpawn(type, world, spawnReason, pos, random);
    }
    public static boolean canMobSpawn(EntityType<?> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        if(!isInRightDimension(type)){return false;}
        BlockPos blockPos = pos.down();
        return world.getBlockState(blockPos).allowsSpawning(world, blockPos, type);
    }
    public static boolean isInRightDimension(EntityType<?> type) {
        var dimensionKey = MinecraftClient.getInstance().world.getRegistryKey();
        if (dimensionKey == World.NETHER) {
            return NETHER_ENTITIES.contains(type) || UNIVERSAL_ENTITIES.contains(type);
        } else if (dimensionKey == World.END) {
            return END_ENTITIES.contains(type) || UNIVERSAL_ENTITIES.contains(type);
        } else {
            return OVERWORLD_ENTITIES.contains(type) || UNIVERSAL_ENTITIES.contains(type);
        }
    }
    public static boolean canStraySpawn(EntityType<StrayEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        BlockPos blockPos = pos;

        do {
            blockPos = blockPos.up();
        } while (world.getBlockState(blockPos).isOf(Blocks.POWDER_SNOW));

        return canSpawnInDark(type, world, spawnReason, pos, random) && (SpawnReason.isAnySpawner(spawnReason) || world.isSkyVisible(blockPos.down()));
    }
    public static boolean canHuskSpawn(EntityType<HuskEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return canSpawnInDark(type, world, spawnReason, pos, random) && world.isSkyVisible(pos);
    }
    public static boolean canSpawnIgnoreLightLevel(
            EntityType<?> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random
    ) {
        return world.getDifficulty() != Difficulty.PEACEFUL && canMobSpawn(type, world, spawnReason, pos, random);
    }
    public static boolean isSpawnDark(WorldAccess world, BlockPos pos, Random random) {
        if (world.getLightLevel(LightType.SKY, pos) > random.nextInt(32)) {
            return false;
        } else {
            DimensionType dimensionType = world.getDimension();
            int i = dimensionType.monsterSpawnBlockLightLimit();
            if (i < 15 && world.getLightLevel(LightType.BLOCK, pos) > i) {
                return false;
            } else {
                int j = world.getLightLevel(pos);
                return j <= dimensionType.monsterSpawnLightTest().get(random);
            }
        }
    }
    public static boolean isValidNaturalSpawn(EntityType<? extends AnimalEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.ANIMALS_SPAWNABLE_ON) && isLightLevelValidForNaturalSpawn(world, pos);
    }
    protected static boolean isLightLevelValidForNaturalSpawn(BlockRenderView world, BlockPos pos) {
        return world.getBaseLightLevel(pos, 0) > 8;
    }
}
