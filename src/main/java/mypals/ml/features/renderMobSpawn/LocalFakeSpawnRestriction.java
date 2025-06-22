package mypals.ml.features.renderMobSpawn;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.*;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ArmadilloEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class LocalFakeSpawnRestriction {
    public static final Map<EntityType<? extends Entity>, Entry> RESTRICTIONS = new HashMap();

    public static <T extends MobEntity> void register(
            EntityType<T> type,
            SpawnLocation location,
            Heightmap.Type heightmapType,
            SpawnPredicate<T> predicate
    ) {
        Entry old = RESTRICTIONS.put(type, new Entry(heightmapType, location, predicate));
        if (old != null) {
            throw new IllegalStateException("Duplicate registration for type " + Registries.ENTITY_TYPE.getId(type));
        }
    }

    public static SpawnLocation getLocation(EntityType<?> type) {
        LocalFakeSpawnRestriction.Entry entry = RESTRICTIONS.get(type);
        return entry == null ? SpawnLocationTypes.UNRESTRICTED : entry.location;
    }

    public static boolean isSpawnPosAllowed(EntityType<?> type, WorldView world, BlockPos pos) {
        return getLocation(type).isSpawnPositionOk(world, pos, type);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> boolean canSpawn(
            EntityType<T> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random
    ) {
        Entry entry = RESTRICTIONS.get(type);
        if (entry == null) {
            return true;
        }
        SpawnPredicate<T> pred = (SpawnPredicate<T>) entry.predicate;
        return pred.test(type, world, spawnReason, pos, random);
    }

    static {
        register(EntityType.ARMADILLO, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ArmadilloEntity::canSpawn);
        register(EntityType.BAT, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BatEntity::canSpawn);
        register(EntityType.BLAZE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnIgnoreLightLevel);
        register(EntityType.BOGGED, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.CAVE_SPIDER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.CHICKEN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::isValidNaturalSpawn);
        register(EntityType.COW, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::isValidNaturalSpawn);
        register(EntityType.CREEPER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.DONKEY, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::isValidNaturalSpawn);
        register(EntityType.ENDERMAN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.FROG, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, FrogEntity::canSpawn);
        register(EntityType.GHAST, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canMobSpawn);
        register(EntityType.GOAT, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, GoatEntity::canSpawn);
        register(EntityType.HORSE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::isValidNaturalSpawn);
        register(EntityType.HUSK, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canHuskSpawn);
        register(EntityType.IRON_GOLEM, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canMobSpawn);
        register(EntityType.LLAMA, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::isValidNaturalSpawn);
        register(EntityType.MAGMA_CUBE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MagmaCubeEntity::canMagmaCubeSpawn);
        register(EntityType.MOOSHROOM, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MooshroomEntity::canSpawn);
        register(EntityType.MULE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::isValidNaturalSpawn);
        register(EntityType.PARROT, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING, ParrotEntity::canSpawn);
        register(EntityType.PIG, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::isValidNaturalSpawn);
        register(EntityType.HOGLIN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HoglinEntity::canSpawn);
        register(EntityType.PIGLIN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PiglinEntity::canSpawn);
        register(EntityType.PILLAGER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PatrolEntity::canSpawn);
        register(EntityType.POLAR_BEAR, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PolarBearEntity::canSpawn);
        register(EntityType.RABBIT, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, RabbitEntity::canSpawn);
        register(EntityType.SHEEP, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::isValidNaturalSpawn);
        register(EntityType.SKELETON, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.SKELETON_HORSE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SkeletonHorseEntity::canSpawn);
        register(EntityType.SLIME, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SlimeEntity::canSpawn);
        register(EntityType.SPIDER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.STRAY, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canStraySpawn);
        register(EntityType.STRIDER, SpawnLocationTypes.IN_LAVA, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, StriderEntity::canSpawn);
        register(EntityType.TURTLE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, TurtleEntity::canSpawn);
        register(EntityType.VILLAGER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canMobSpawn);
        register(EntityType.WITCH, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.WITHER_SKELETON, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.WOLF, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, WolfEntity::canSpawn);
        register(EntityType.ZOMBIE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.ZOMBIFIED_PIGLIN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ZombifiedPiglinEntity::canSpawn);
        register(EntityType.ZOMBIE_VILLAGER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.CAT, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::isValidNaturalSpawn);
        register(EntityType.EVOKER, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.FOX, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, FoxEntity::canSpawn);
        register(EntityType.PANDA, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::isValidNaturalSpawn);
        register(EntityType.RAVAGER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.VINDICATOR, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canSpawnInDark);
        register(EntityType.WANDERING_TRADER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canMobSpawn);
        register(EntityType.WARDEN, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntitySpawnChacker::canMobSpawn);
    }

    record Entry(Heightmap.Type heightmapType, SpawnLocation location, LocalFakeSpawnRestriction.SpawnPredicate<?> predicate) {
    }

    @FunctionalInterface
    public interface SpawnPredicate<T extends Entity> {
        boolean test(EntityType<T> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random);
    }
}
