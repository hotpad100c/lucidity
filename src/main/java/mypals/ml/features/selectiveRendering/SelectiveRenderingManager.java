package mypals.ml.features.selectiveRendering;

import mypals.ml.config.LucidityConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class SelectiveRenderingManager {
    public static Map<Integer, Map<Property,Object>> selectedBlockTypes = new HashMap<>();
    public static List<Integer> selectedEntityTypes = new CopyOnWriteArrayList<>();
    public static List<Integer> selectedParticleTypes = new CopyOnWriteArrayList<>();
    public static List<AreaBox> selectedAreas = new CopyOnWriteArrayList<>();

    public static Item wand;
    public static RenderMode blockRenderMode = RenderMode.OFF;
    public static RenderMode entityRenderMode = RenderMode.OFF;
    public static RenderMode particleRenderMode = RenderMode.OFF;
    public enum RenderMode {
        OFF("config.lucidity.render_mode.off","textures/gui/rendering_mode/render_mode_off.png"),
        RENDER_INSIDE_INCLUDE("config.lucidity.render_mode.render_inside_include","textures/gui/rendering_mode/render_mode_1.png"),
        RENDER_OUTSIDE_INCLUDE("config.lucidity.render_mode.render_outside_include","textures/gui/rendering_mode/render_mode_2.png"),
        RENDER_INSIDE_EXCLUDE("config.lucidity.render_mode.render_inside_exclude","textures/gui/rendering_mode/render_mode_3.png"),
        RENDER_OUTSIDE_EXCLUDE("config.lucidity.render_mode.render_outside_exclude","textures/gui/rendering_mode/render_mode_4.png"),
        //RENDER_INSIDE_NONE("config.lucidity.render_mode.render_inside_none"),
        RENDER_ONLY_SPECIFIC("config.lucidity.render_mode.render_only_specific","textures/gui/rendering_mode/render_mode_5.png"),
        //RENDER_INSIDE_ONLY("config.lucidity.render_mode.render_inside_only"),
        RENDER_EXCLUDE_SPECIFIC("config.lucidity.render_mode.render_exclude_specific","textures/gui/rendering_mode/render_mode_6.png"),
        RENDER_INSIDE_ALL("config.lucidity.render_mode.render_inside_all","textures/gui/rendering_mode/render_mode_7.png"),
        RENDER_OUTSIDE_ALL("config.lucidity.render_mode.render_outside_all","textures/gui/rendering_mode/render_mode_8.png"),
        RENDER_INSIDE_EXCLUDE_SPECIFIC("config.lucidity.render_mode.render_inside_exclude_specific","textures/gui/rendering_mode/render_mode_9.png"),
        RENDER_OUTSIDE_EXCLUDE_SPECIFIC("config.lucidity.render_mode.render_outside_exclude_specific","textures/gui/rendering_mode/render_mode_10.png");
        private final String translationKey;
        private final String icon;

        RenderMode(String translationKey, String icon) {
            this.translationKey = translationKey;
            this.icon = icon;
        }
        public String getTranslationKey() {
            return translationKey;
        }
        public String getIcon() {
            return icon;
        }
    }



    public static void resolveSelectedBlockStatesFromString(List<String> blockStrings) {
        selectedBlockTypes.clear();

        LucidityConfig.CONFIG_HANDLER.instance();
        blockStrings.forEach(blockString -> {
            try {
                blockString = blockString.replace(" ","");
                String[] parts = blockString.split("\\[", 2);
                String blockIdString = parts[0];
                // 如果没有 ":"，则默认补充 "minecraft:"
                if (!blockIdString.contains(":")) {
                    blockIdString = "minecraft:" + blockIdString;
                }

                Identifier blockId = Identifier.of(blockIdString);
                Block block = Registries.BLOCK.get(blockId);

                if (block == null) {
                    return;
                }

                Map<Property,Object> states = new HashMap<>();
                boolean hasState = false;
                NbtCompound w = new NbtCompound();
                // 如果存在属性，处理属性部分
                if (parts.length > 1) {
                    String propertiesString = parts[1].replace("]", "");
                    String[] properties = propertiesString.split(",");

                    for (String property : properties) {
                        String[] keyValue = property.split("=");
                        if (keyValue.length != 2) continue;

                        String key = keyValue[0];
                        String value = keyValue[1];

                        Property<?> blockProperty = block.getStateManager().getProperty(key);
                        if (blockProperty != null) {
                            states.put(blockProperty,value);
                            hasState = true;
                        }
                    }
                }

                if(hasState) {
                    // 将解析后的方块状态添加到列表
                    selectedBlockTypes.put(Registries.BLOCK.getRawId(block), states);
                }else{
                    selectedBlockTypes.put(Registries.BLOCK.getRawId(block), null);
                }

            } catch (Exception e) {
                // 处理解析错误
                System.err.println("Failed to parse block state: " + blockString);
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
                    ,color,0.2f,false
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in input: " + areaString, e);
        }
    }
    public static boolean shouldRenderBlock(BlockState block, BlockPos pos) {
        return shouldRender(
                blockRenderMode,
                block,
                new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                blockType -> Registries.BLOCK.getRawId(block.getBlock()),
                null,
                selectedBlockTypes
        );
    }

    public static boolean shouldRenderEntity(EntityType entity, Vec3d pos) {
        return shouldRender(
                entityRenderMode,
                entity,
                pos,
                entityType -> Registries.ENTITY_TYPE.getRawId(entity),
                selectedEntityTypes,
                null
        );
    }

    public static boolean shouldRenderParticle(ParticleType particle, Vec3d pos) {
        return shouldRender(
                particleRenderMode,
                particle,
                pos,
                particleType -> Registries.PARTICLE_TYPE.getRawId(particle),
                selectedParticleTypes,
                null
        );
    }
    private static <T> boolean shouldRender(
            RenderMode renderMode,
            T type,
            Vec3d pos,
            Function<T, Integer> getIdFunction,
            @Nullable
            List<Integer> selectedTypes,
            @Nullable
            Map<Integer, Map<Property,Object>> selectedBlockStates
    ) {
        if (renderMode == RenderMode.OFF) {
            return true;
        }
        boolean isSelected;
        if(selectedBlockStates == null){
            if(selectedTypes == null) return true;
            isSelected = isSelectedType(getIdFunction.apply(type), selectedTypes);
        }else{
            isSelected = isSelectedTypeAndState((BlockState) type, selectedBlockStates);
        }

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

            case RENDER_ONLY_SPECIFIC:
                return isSelected;

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
    public static boolean isSelectedTypeAndState(BlockState state, Map<Integer, Map<Property, Object>> selectedTypes) {
        for (Map.Entry<Integer, Map<Property, Object>> entry : selectedTypes.entrySet()) {
            Integer blockId = entry.getKey();
            Map<Property, Object> properties = entry.getValue();

            // 如果属性为空或方块ID不匹配，返回false
            if (!blockId.equals(Registries.BLOCK.getRawId(state.getBlock()))) {
                continue;
            }
            if(properties == null || properties.isEmpty()){
                if(Registries.BLOCK.getRawId(state.getBlock()) == blockId){
                    return true;
                }
            }

            // 检查BlockState是否包含所有属性
            boolean hasAllProperties = true;
            for (Map.Entry<Property, Object> property : properties.entrySet()) {
                Optional<Object> stateProperty = state.getOrEmpty(property.getKey());

                // 如果BlockState中不存在该属性
                if (!stateProperty.isPresent()) {
                    hasAllProperties = false;
                    break;
                }

                // 如果属性的值不匹配
                if (!stateProperty.get().toString().equals(property.getValue())) {
                    hasAllProperties = false;
                    break;
                }
            }

            // 如果所有属性都匹配
            if (hasAllProperties) {
                return true;
            }
        }
        return false;
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
