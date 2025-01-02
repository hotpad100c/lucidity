package mypals.ml.features.selectiveRendering;

import mypals.ml.config.LucidityConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;


public class SelectiveRenderingManager {
    public static List<Integer> selectedBlockTypes = new CopyOnWriteArrayList<>();
    public static List<Integer> selectedEntityTypes = new CopyOnWriteArrayList<>();
    public static List<Integer> selectedParticleTypes = new CopyOnWriteArrayList<>();
    public static List<AreaBox> selectedAreas = new CopyOnWriteArrayList<>();

    public static Item wand;
    public static RenderMode blockRenderMode = RenderMode.OFF;
    public static RenderMode entityRenderMode = RenderMode.OFF;
    public static RenderMode particleRenderMode = RenderMode.OFF;
    public enum RenderMode {
        OFF("config.lucidity.render_mode.off"),
        RENDER_INSIDE_INCLUDE("config.lucidity.render_mode.render_inside_include"),
        RENDER_OUTSIDE_INCLUDE("config.lucidity.render_mode.render_outside_include"),
        RENDER_INSIDE_EXCLUDE("config.lucidity.render_mode.render_inside_exclude"),
        RENDER_OUTSIDE_EXCLUDE("config.lucidity.render_mode.render_outside_exclude"),
        RENDER_INSIDE_NONE("config.lucidity.render_mode.render_inside_none"),
        RENDER_ONLY_SPECIFIC("config.lucidity.render_mode.render_only_specific"),
        RENDER_INSIDE_ONLY("config.lucidity.render_mode.render_inside_only"),
        RENDER_EXCLUDE_SPECIFIC("config.lucidity.render_mode.render_exclude_specific"),
        RENDER_INSIDE_ALL("config.lucidity.render_mode.render_inside_all"),
        RENDER_OUTSIDE_ALL("config.lucidity.render_mode.render_outside_all"),
        RENDER_INSIDE_EXCLUDE_SPECIFIC("config.lucidity.render_mode.render_inside_exclude_specific"),
        RENDER_OUTSIDE_EXCLUDE_SPECIFIC("config.lucidity.render_mode.render_outside_exclude_specific");

        private final String translationKey;

        RenderMode(String translationKey) {
            this.translationKey = translationKey;
        }
        public String getTranslationKey() {
            return translationKey;
        }
    }




    public static void resolveSelectedBlockTypesFromString(List<String> blockStrings){
        selectedBlockTypes.clear();

        LucidityConfig.CONFIG_HANDLER.instance();
        blockStrings.forEach(blockString -> {

            if (!blockString.contains(":")) {
                blockString = "minecraft:" + blockString;
            }
            Identifier blockId = Identifier.of(blockString);
            Block targetBlock = Registries.BLOCK.get(blockId);
            if (targetBlock == null) {
                blockStrings.remove(blockId);
            }
            else{
                selectedBlockTypes.add(Registries.BLOCK.getRawId(targetBlock));
            }
        });
    }
    public static void resolveSelectedEntityTypesFromString(List<String> entityStrings){
        selectedEntityTypes.clear();

        LucidityConfig.CONFIG_HANDLER.instance();
        entityStrings.forEach(entityString -> {

            if (!entityString.contains(":")) {
                entityString = "minecraft:" + entityString;
            }
            Identifier entityId = Identifier.of(entityString);
            EntityType targetEntity = Registries.ENTITY_TYPE.get(entityId);
            if (targetEntity == null) {
                entityStrings.remove(entityId);
            }
            else{
                selectedEntityTypes.add(Registries.ENTITY_TYPE.getRawId(targetEntity));
            }
        });
    }
    public static void resolveSelectedParticleTypesFromString(List<String> particleStrings){
        selectedParticleTypes.clear();

        LucidityConfig.CONFIG_HANDLER.instance();
        particleStrings.forEach(particleString -> {

            if (!particleString.contains(":")) {
                particleString = "minecraft:" + particleString;
            }
            Identifier particleId = Identifier.of(particleString);
            ParticleType targetParticle = Registries.PARTICLE_TYPE.get(particleId);
            if (targetParticle == null) {
                particleStrings.remove(particleId);
            }
            else{
                selectedParticleTypes.add(Registries.PARTICLE_TYPE.getRawId(targetParticle));
            }
        });
    }
    public static void resolveSelectedWandFromString(String name){
        Item last_wind = wand;
        if (!name.contains(":")) {
            name = "minecraft:" + name;
        }
        Identifier id = Identifier.of(name);
        Item nweWand = Registries.ITEM.get(id);
        if (nweWand == null) {
            wand = last_wind;
        }
        else{
            wand = nweWand;
        }
    }
    public static String resolveSelectiveRenderingMode(int index, RenderMode[] modes, Consumer<RenderMode> modeSetter) {
        if (index >= 0 && index < modes.length) {
            RenderMode mode = modes[index];
            modeSetter.accept(mode);
            return mode.getTranslationKey();
        }
        return "-";
    }
    public static String resolveSelectiveBlockRenderingMode(int index) {
        return resolveSelectiveRenderingMode(index, RenderMode.values(), mode -> blockRenderMode = mode);
    }

    public static String resolveSelectiveEntityRenderingMode(int index) {
        return resolveSelectiveRenderingMode(index, RenderMode.values(), mode -> entityRenderMode = mode);
    }

    public static String resolveSelectiveParticleRenderingMode(int index) {
        return resolveSelectiveRenderingMode(index, RenderMode.values(), mode -> particleRenderMode = mode);
    }

    public static void resolveSelectedAreasFromString(List<String> areaStrings){
        selectedAreas.clear();

        areaStrings.forEach(areaString -> {
            try {
                AreaBox area = parseAABB(areaString);
                selectedAreas.add(area);
            } catch (IllegalArgumentException e) {
                System.err.println("Failed to parse area: " + areaString + " -> " + e.getMessage());
            }
        });
    }
    private static AreaBox parseAABB(String areaString) throws IllegalArgumentException {
        String[] parts = areaString.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid format. Expected x1,y1,z1:x2,y2,z2");
        }

        String[] startCoords = parts[0].split(",");
        String[] endCoords = parts[1].split(",");

        if (startCoords.length != 3 || endCoords.length != 3) {
            throw new IllegalArgumentException("Invalid coordinates. Expected x1,y1,z1:x2,y2,z2");
        }

        try {
            Random rand = new Random();
            int x1 = Integer.parseInt(startCoords[0].trim());
            int y1 = Integer.parseInt(startCoords[1].trim());
            int z1 = Integer.parseInt(startCoords[2].trim());

            int x2 = Integer.parseInt(endCoords[0].trim());
            int y2 = Integer.parseInt(endCoords[1].trim());
            int z2 = Integer.parseInt(endCoords[2].trim());

            int hash = new BlockPos(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2)).hashCode();
            float hue = (hash % 360) / 360.0f;
            float saturation = 1f;
            float brightness = 1f;
            Color color = Color.getHSBColor(hue, saturation, brightness);

            return new AreaBox(
                    new BlockPos(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2)),
                    new BlockPos(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2))
                    ,color
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in input: " + areaString, e);
        }
    }
    public static boolean shouldRenderBlock(BlockState block, BlockPos pos) {
        return shouldRender(
                blockRenderMode,
                block.getBlock(),
                new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                blockType -> Registries.BLOCK.getRawId(block.getBlock()),
                selectedBlockTypes
        );
    }

    public static boolean shouldRenderEntity(EntityType entity, Vec3d pos) {
        return shouldRender(
                entityRenderMode,
                entity,
                pos,
                entityType -> Registries.ENTITY_TYPE.getRawId(entity),
                selectedEntityTypes
        );
    }

    public static boolean shouldRenderParticle(ParticleType particle, Vec3d pos) {
        return shouldRender(
                particleRenderMode,
                particle,
                pos,
                particleType -> Registries.PARTICLE_TYPE.getRawId(particle),
                selectedParticleTypes
        );
    }
    private static <T> boolean shouldRender(
            RenderMode renderMode, T type, Vec3d pos, Function<T, Integer> getIdFunction
            ,List<Integer> selectedTypes) {
        if (renderMode == RenderMode.OFF) {
            return true;
        }

        boolean isSelected = isSelectedType(getIdFunction.apply(type), selectedTypes);
        boolean isInArea = isSelectedArea(pos);

        switch (renderMode) {
            case RENDER_INSIDE_INCLUDE:
                return isInArea && isSelected;

            case RENDER_OUTSIDE_INCLUDE:
                return !isInArea && isSelected;

            case RENDER_INSIDE_EXCLUDE:
                return !(isInArea && isSelected);

            case RENDER_OUTSIDE_EXCLUDE:
                return !(isSelected && !isInArea);

            case RENDER_INSIDE_NONE:
                return !isInArea;

            case RENDER_ONLY_SPECIFIC:
                return isSelected;

            case RENDER_INSIDE_ONLY:
                return isInArea;

            case RENDER_EXCLUDE_SPECIFIC:
                return !isSelected;

            case RENDER_INSIDE_ALL:
                return isInArea;

            case RENDER_OUTSIDE_ALL:
                return !isInArea;

            case RENDER_INSIDE_EXCLUDE_SPECIFIC:
                return isInArea && !isSelected;

            case RENDER_OUTSIDE_EXCLUDE_SPECIFIC:
                return !isInArea && !isSelected;

            default:
                return true;
        }
    }
    public static boolean isSelectedType(int id, List<Integer> selectedTypes) {
        return selectedTypes.contains(id);
    }
    public static boolean isSelectedArea(Vec3d blockPos){
        for(AreaBox selectedArea : selectedAreas){
            if (isInsideArea(blockPos, selectedArea)) {
                return true;
            }
        }
        return false;
    }
    public static boolean isInsideArea(Vec3d pos, AreaBox areaBox){
        if (areaBox.minPos.getX() <= pos.getX() && pos.getX() <= areaBox.maxPos.getX() &&
                areaBox.minPos.getY() <= pos.getY() && pos.getY() <= areaBox.maxPos.getY() &&
                areaBox.minPos.getZ() <= pos.getZ() && pos.getZ() <= areaBox.maxPos.getZ()) {
            return true;
        }
        return false;
    }
}
