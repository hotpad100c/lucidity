package mypals.ml.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.features.ImageRendering.ImageDataParser;
import mypals.ml.features.ImageRendering.configuration.MediaEntry;
import mypals.ml.features.blockOutline.OutlineManager;
import mypals.ml.features.selectiveRendering.AreaBox;
import mypals.ml.features.selectiveRendering.WandActionsManager;
import mypals.ml.rendering.shapes.*;
import net.caffeinemc.mods.sodium.client.render.frapi.SodiumRenderer;
import net.caffeinemc.mods.sodium.fabric.SodiumFabricMod;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static mypals.ml.Lucidity.*;
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
    public static List<MultiPointLine> multiPointLines = new CopyOnWriteArrayList<>();
    public static ConcurrentHashMap<Vec3d, ShineMarker> shineMarkers = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Vec3d, Text> texts = new ConcurrentHashMap<>();
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
        if( FabricLoader.getInstance().isModLoaded("sodium")) {
            return true;
        }
        return false;
    }
    public static void addBox(BoxShape box) {
        if(!boxes.contains(box)){
            boxes.add(box);
        }
    }
    public static void addText(Text text) {
        texts.put(text.pos, text);
    }
    public static void addCube(CubeShape cube) {
        cubes.put(cube.pos, cube);
    }
    public static void addLine(LineShape line) {
        lines.add(line);
    }
    public static void addLine(MultiPointLine line) {
        multiPointLines.add(line);
    }

    public static void addShineMarker(ShineMarker shineMarker,int time){
        if(shineMarkers.containsKey(shineMarker.pos)){
            shineMarkers.get(shineMarker.pos).lifeTime = time;
            shineMarkers.get(shineMarker.pos).color = shineMarker.color;
        }else{
            shineMarker.lifeTime = time;
            shineMarkers.put(shineMarker.pos, shineMarker);
        }
    }
    public static void addAreaBox(AreaBox areaBox){
        areaBoxes.put(Map.entry(areaBox.minPos,areaBox.maxPos), areaBox);
    }
    public static void addOnGroundMarker(OnGroundMarker onGroundMarker){
        onGroundMarkers.put(onGroundMarker.pos, onGroundMarker);
    }
    public static void render(MatrixStack matrixStack, RenderTickCounter counter){
        Tessellator tessellator = Tessellator.getInstance();

        if(MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().gameRenderer.getCamera().isReady()) {
            try {

                //renderPictureFromIdentifier(matrixStack, Identifier.of(MOD_ID,"textures/examples/ray.png"),new Vec3d(0,0,0),new Vec3d(45,90,16),new Vec2f(0.05f,0.05f),0.05f,15728880, OverlayTexture.DEFAULT_UV,counter.getTickDelta(true));
                //renderPictureFromPath(matrixStack, "C:/Users/Ryan/Downloads/lost-file.png/",new Vec3d(9,0,0),new Vec3d(45,0,90),new Vec2f(0.09f,0.09f),0.1f,15720000, OverlayTexture.DEFAULT_UV,counter.getTickDelta(true));

                /*List<String> pictures = new ArrayList<>();
                pictures.add("C:\\Users\\Ryan\\Downloads\\test-transparent.png;p1;[0,0,0];[0,90.3,45];[0.04,0.04]");
                pictures.add("C:\\Users\\Ryan\\Downloads\\BE2DC86B6FF92FF374D26B22DCC27195.png;pic2;[10,0,10];[180,45,90];[0.05,0.08]");
                */
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
                for(CubeShape cube : cubes.values()){
                    cube.draw(matrixStack, 0.01f, counter.getTickDelta(true));
                }
                drawLines(matrixStack);
                for(ShineMarker marker : shineMarkers.values()){
                    marker.draw(matrixStack, MinecraftClient.getInstance().cameraEntity.age, Math.round(calculateAlpha(MinecraftClient.getInstance().cameraEntity.getPos(),
                            marker.pos, marker.lifeTime) * 255),marker.seeThrough);
                }
                for(OnGroundMarker marker : onGroundMarkers.values()){
                    marker.draw(matrixStack);
                }
                for(Text text : texts.values()){
                    text.draw(matrixStack, counter.getTickDelta(true));
                }
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
        shineMarkers.forEachValue(1,marker->{
            marker.lifeTime--;
            if(marker.lifeTime <= 0){
                shineMarkers.remove(marker.pos);
            }
        });


    }
    private static void drawSelectedAreas(MatrixStack matrixStack){
        if (MinecraftClient.getInstance().player != null && (MinecraftClient.getInstance().player.getMainHandStack().getItem() == wand || (MinecraftClient.getInstance().player.isSpectator() && selectInSpectator))) {

            BlockHitResult result = getPlayerLookedBlock(MinecraftClient.getInstance().player, MinecraftClient.getInstance().world);
            BlockPos lookingAt = result.getType() == HitResult.Type.BLOCK ? result.getBlockPos() : BlockPos.ofFloored(result.getPos());
            if(renderSelectionMarker) {
                CubeShape.draw(matrixStack, lookingAt, 0.01f, 0, deleteMode ? Color.red : Color.white, 0.2f, false);
            }
            if (WandActionsManager.pos1 != null) {
                renderSelectionBox(matrixStack, MinecraftClient.getInstance().gameRenderer.getCamera(), 0);
            }

            if (deleteMode) {
                for (AreaBox selectedArea : getAreasToDelete(lookingAt, false)) {
                    selectedArea.draw(matrixStack, Color.red, 0.4f, true);
                }
            }
            for (AreaBox selectedArea : selectedAreas) {
                int minX = Math.min(selectedArea.minPos.getX(), selectedArea.maxPos.getX());
                int minY = Math.min(selectedArea.minPos.getY(), selectedArea.maxPos.getY());
                int minZ = Math.min(selectedArea.minPos.getZ(), selectedArea.maxPos.getZ());
                int maxX = Math.max(selectedArea.minPos.getX(), selectedArea.maxPos.getX());
                int maxY = Math.max(selectedArea.minPos.getY(), selectedArea.maxPos.getY());
                int maxZ = Math.max(selectedArea.minPos.getZ(), selectedArea.maxPos.getZ());

                // 遍历所有方块
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            OutlineManager.targetedBlocks.add(new BlockPos(x, y, z));
                        }
                    }
                }
                //selectedArea.draw(matrixStack, false);
            }
        }

    }
    private static void drawLines(MatrixStack matrixStack){
        for (LineShape line : lines) {
            line.draw(matrixStack, line.start, line.end, line.color, line.alpha,line.seeThrough);
        }
        for (MultiPointLine line : multiPointLines) {
            line.draw(matrixStack, line.points, line.color, line.alpha,line.seeThrough);
        }
    }
}
