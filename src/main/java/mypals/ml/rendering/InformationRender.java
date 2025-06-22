package mypals.ml.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.features.ImageRendering.ImageDataParser;
import mypals.ml.features.ImageRendering.configuration.MediaEntry;
import mypals.ml.features.selectiveRendering.AreaBox;
import mypals.ml.features.selectiveRendering.WandActionsManager;
import mypals.ml.rendering.shapes.*;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static mypals.ml.Lucidity.*;
import static mypals.ml.config.Keybinds.deleteArea;
import static mypals.ml.config.LucidityConfig.*;
import static mypals.ml.features.ImageRendering.ImageRenderer.*;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.selectedAreas;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;
import static mypals.ml.features.selectiveRendering.WandActionsManager.deleteMode;
import static mypals.ml.features.selectiveRendering.WandActionsManager.getAreasToDelete;
import static mypals.ml.rendering.ShapeRender.*;
import static mypals.ml.rendering.shapes.ShineMarker.calculateAlpha;

public class InformationRender {
    public static List<BoxShape> boxes = new CopyOnWriteArrayList<>();
    public static ConcurrentHashMap<BlockPos, CubeShape> cubes = new ConcurrentHashMap<>();
    public static List<LineShape> lines = new CopyOnWriteArrayList<>();
    public static List<LineStrip> multiPointLines = new CopyOnWriteArrayList<>();
    public static Map<Color, ConcurrentHashMap<Vec3d, ShineMarker>> shineMarkers = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Vec3d, TextShape> texts = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Map.Entry<BlockPos,BlockPos>, AreaBox> areaBoxes = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<BlockPos,OnGroundMarker> onGroundMarkers = new ConcurrentHashMap<>();
    public static final RenderPhase.DepthTest NO_DEPTH_TEST = new RenderPhase.DepthTest("none", 0) {
        @Override
        public void startDrawing() {
            RenderSystem.disableDepthTest();
        }

        @Override
        public void endDrawing() {
            RenderSystem.enableDepthTest();
        }
    };

    public static boolean isIrisShaderUsed(){
        if( FabricLoader.getInstance().isModLoaded("iris")) {
            return IrisApi.getInstance() != null && IrisApi.getInstance().isShaderPackInUse();
        }
        return false;
    }
    public static boolean isSodiumUsed(){
        return FabricLoader.getInstance().isModLoaded("sodium");
    }
    public static void addBox(BoxShape box) {
        if(!boxes.contains(box)){
            boxes.add(box);
        }
    }
    public static void addText(TextShape text) {
        texts.put(text.pos, text);
    }
    public static void addCube(CubeShape cube) {
        cubes.put(cube.pos, cube);
    }
    public static void addLine(LineShape line) {
        lines.add(line);
    }
    public static void addLine(LineStrip line) {
        multiPointLines.add(line);
    }

    public static void addShineMarker(ShineMarker shineMarker, int time) {

        ConcurrentHashMap<Vec3d, ShineMarker> markers = shineMarkers.computeIfAbsent(
                shineMarker.color,
                k -> new ConcurrentHashMap<>()
        );

        // 更新或添加 ShineMarker
        markers.compute(shineMarker.pos, (pos, existingMarker) -> {
            if (existingMarker != null) {
                existingMarker.lifeTime = time;
                existingMarker.color = shineMarker.color; // 更新颜色（如果需要）
                return existingMarker;
            } else {
                shineMarker.lifeTime = time;
                return shineMarker;
            }
        });
    }
    public static void addAreaBox(AreaBox areaBox){
        areaBoxes.put(Map.entry(areaBox.minPos,areaBox.maxPos), areaBox);
    }
    public static void addOnGroundMarker(OnGroundMarker onGroundMarker){
        onGroundMarkers.put(onGroundMarker.pos, onGroundMarker);
    }
    public static void render(MatrixStack matrixStack, RenderTickCounter counter){
        if(MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().gameRenderer.getCamera().isReady()) {
            try {
                for(MediaEntry image : ImageDataParser.images.values()){
                    renderPictureWorldSpace(matrixStack, image,
                            new Vec3d(image.getPos()[0],image.getPos()[1],image.getPos()[2]),
                            new Vec3d(image.getRotation()[0],image.getRotation()[1],image.getRotation()[2]),
                            new Vector2d(image.getScale()[0],image.getScale()[1]),
                            pixelsPerBlock,15720000, OverlayTexture.DEFAULT_UV,counter.getTickDelta(true),false);
                }
                //parse()
                for (BoxShape box : boxes) {
                    box.draw(matrixStack);
                }
                CubeShape.drawCubes(matrixStack,cubes,0.01f,counter.getTickDelta(true));
                drawLines(matrixStack);
                for (Map.Entry<Color,ConcurrentHashMap<Vec3d, ShineMarker>> markers : shineMarkers.entrySet()){
                    ShineMarker.drawMultiple(matrixStack,markers.getValue().values().stream().toList(),MinecraftClient.getInstance().cameraEntity.age, markers.getKey());
                }
                OnGroundMarker.drawMultiple(matrixStack,onGroundMarkers.values().stream().toList());
                TextShape.drawMultiple(matrixStack, texts.values().stream().toList(),counter.getTickDelta(true));
                for (AreaBox areaBox: areaBoxes.values()) {
                    areaBox.draw(matrixStack, false);
                }
                drawSelectedAreas(matrixStack);
            }catch (Exception e){
                LOGGER.info(e.toString());
            }
        }
    }
    public static void clear(){
        boxes.clear();
        lines.clear();
        cubes.clear();
        texts.clear();
        multiPointLines.clear();
        areaBoxes.clear();
        onGroundMarkers.clear();
        shineMarkers.entrySet().removeIf(entry -> {
            Map<Vec3d,ShineMarker> markerSet = entry.getValue();
            markerSet.entrySet().removeIf(marker -> {
                marker.getValue().lifeTime--;
                return marker.getValue().lifeTime <= 0;
            });
            return markerSet.isEmpty();
        });


    }
    private static void drawSelectedAreas(MatrixStack matrixStack){
        if (MinecraftClient.getInstance().player != null && (MinecraftClient.getInstance().player.getMainHandStack().getItem() == wand || (MinecraftClient.getInstance().player.isSpectator() && selectInSpectator))) {

            BlockHitResult result = getPlayerLookedBlock(MinecraftClient.getInstance().player, MinecraftClient.getInstance().world);
            BlockPos lookingAt = result.getType() == HitResult.Type.BLOCK ? result.getBlockPos() : BlockPos.ofFloored(result.getPos());
            if(renderSelectionMarker) {
                CubeShape.drawSingle(matrixStack, lookingAt, 0.01f, 0, deleteMode ? Color.red : Color.white, 0.2f, false);
            }
            if (WandActionsManager.pos1 != null) {
                renderSelectionBox(matrixStack, MinecraftClient.getInstance().gameRenderer.getCamera(), 0);
            }
            List<AreaBox> areasToDelete = getAreasToDelete(lookingAt, false);

            if(deleteArea.isPressed()) {
                for (AreaBox selectedArea : selectedAreas) {
                    if (!areasToDelete.contains(selectedArea)) {
                        selectedArea.draw(matrixStack, Color.white, 0.01f, true);
                    } else {
                        selectedArea.draw(matrixStack, Color.white, 0.1f, true);
                    }
                }
            }
        }

    }
    private static void drawLines(MatrixStack matrixStack){
        LineShape.drawLines(matrixStack, lines);
        LineStrip.drawLineStrips(matrixStack, multiPointLines);
    }
}
