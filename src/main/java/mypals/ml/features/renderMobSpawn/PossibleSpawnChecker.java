package mypals.ml.features.renderMobSpawn;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.SpawnHelper.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.registry.Registries;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Blocks;

import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static mypals.ml.features.renderMobSpawn.EntitySpawnChacker.isInRightDimension;

public class PossibleSpawnChecker {

    public static ArrayList<String> getPossibleSpawnsAt(ClientWorld world, BlockPos pos) {
        ArrayList<String> result = new ArrayList<>();
        if(world.getBlockState(pos.down()).isAir()){return result;}
        if(!(world.getBlockState(pos).isSolidBlock(world,pos) || world.getBlockState(pos).isFullCube(world,pos) || world.getBlockState(pos).isAir())){return result;}
        LocalFakeSpawnRestriction.RESTRICTIONS.entrySet().forEach(entry->{
            if(isInRightDimension(entry.getKey())) {

                if (canSpawnHere(world, entry.getKey(), pos)) {
                    String idStr = Text.translatable(entry.getKey().getTranslationKey()).getString();
                    result.add(idStr);
                }
            }
        } );
        return result;
    }

    private static boolean canSpawnHere(ClientWorld world, EntityType<?> type, BlockPos pos) {
        if (!LocalFakeSpawnRestriction.isSpawnPosAllowed(type, world, pos)) {
            return false;
        }
        if (!LocalFakeSpawnRestriction.canSpawn(type, world, SpawnReason.NATURAL, pos, world.random)) {
            return false;
        }
        Vec3d spawnCenter = new Vec3d(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5
        );
        return world.isSpaceEmpty(type.getSpawnBox(spawnCenter.x, spawnCenter.y, spawnCenter.z));
    }
}
