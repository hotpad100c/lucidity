package mypals.ml.rendering;

import mypals.ml.features.selectiveRendering.AreaBox;
import mypals.ml.features.selectiveRendering.WandActionsManager;
import mypals.ml.rendering.glowingMarker.GlowEffect;
import mypals.ml.rendering.shapes.BoxShape;
import mypals.ml.rendering.shapes.CubeShape;
import mypals.ml.rendering.shapes.Line;
import mypals.ml.rendering.shapes.ShineMarker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static mypals.ml.Lucidity.MOD_ID;
import static mypals.ml.Lucidity.getPlayerLookedBlock;
import static mypals.ml.config.LucidityConfig.selectInSpectator;
import static mypals.ml.features.AdvancedContentRender.ImageRenderer.renderPicture;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.selectedAreas;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;
import static mypals.ml.features.selectiveRendering.WandActionsManager.deleteMode;
import static mypals.ml.features.selectiveRendering.WandActionsManager.getAreasToDelete;
import static mypals.ml.rendering.ShapeRender.*;
import static mypals.ml.rendering.glowingMarker.BeamRenderer.renderBeam;
import static mypals.ml.rendering.glowingMarker.GlowEffect.renderGlowEffect;

public class InformationRender {
    public static List<BoxShape> boxes = new CopyOnWriteArrayList<>();
    public static List<CubeShape> cubes = new CopyOnWriteArrayList<>();
    public static List<Line> lines = new CopyOnWriteArrayList<>();
    public static List<ShineMarker> shineMarkers = new CopyOnWriteArrayList<>();



    public static void addBox(BoxShape box) {
        boxes.add(box);
    }
    public static void addCube(CubeShape cube) {
        cubes.add(cube);
    }
    public static void addLine(Line line) {
        lines.add(line);
    }
    public static void addShineMarker(ShineMarker shineMarker,int time){
        shineMarker.lifeTime = time;
        shineMarkers.forEach(marker->{
            if(shineMarker.pos == marker.pos){
                marker.lifeTime = time;
                return;
            }
        });
        shineMarkers.add(shineMarker);
    }
    public static void render(MatrixStack matrixStack, RenderTickCounter counter){
        if(MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().gameRenderer.getCamera().isReady()) {
            renderPicture(matrixStack, Identifier.of(MOD_ID,"textures/DebugPNG.png"),new Vec3d(0,0,0),1,240, OverlayTexture.DEFAULT_UV,counter.getTickDelta(true));
            for (BoxShape box : boxes) {
                ShapeRender.drawCube(matrixStack, box.pos.toCenterPos(), box.length, box.weigth, box.height, counter.getTickDelta(true), box.color, box.alpha);
            }
            for (Line line : lines) {
                ShapeRender.drawLine(matrixStack, line.start, line.end, counter.getTickDelta(true), line.color, line.alpha);
            }
            for (CubeShape cube : cubes) {
                if (cube.seeThrough)
                    ShapeRender.drawCubeSeeThrough(matrixStack, cube.pos, 0.01f, counter.getTickDelta(true), cube.color, cube.alpha);
                else
                    ShapeRender.drawCube(matrixStack, cube.pos, 0.01f, counter.getTickDelta(true), cube.color, cube.alpha);
            }
            for (ShineMarker marker : shineMarkers) {
                assert MinecraftClient.getInstance().cameraEntity != null;
                renderBeam(matrixStack, 1);
                renderGlowEffect(matrixStack, marker.pos, marker.size, counter,
                        MinecraftClient.getInstance().cameraEntity.age, marker.lights, marker.speed, 2,
                        marker.color.getRed(), marker.color.getGreen(), marker.color.getBlue(), (int) Math.round(mapAlpha(marker.lifeTime, 0, 30) * 255), marker.seed);
            }
            if (MinecraftClient.getInstance().player != null && (MinecraftClient.getInstance().player.getMainHandStack().getItem() == wand || (MinecraftClient.getInstance().player.isSpectator() && selectInSpectator))){
                BlockHitResult result = getPlayerLookedBlock(MinecraftClient.getInstance().player, MinecraftClient.getInstance().world);
                BlockPos lookingAt = result.getType() == HitResult.Type.BLOCK ? result.getBlockPos() : BlockPos.ofFloored(result.getPos());
                ShapeRender.drawCube(matrixStack, lookingAt, 0.01f, counter.getTickDelta(false), deleteMode?Color.red:Color.white, 0.2f);
                if (WandActionsManager.pos1 != null) {
                    renderSelectionBox(matrixStack, MinecraftClient.getInstance().gameRenderer.getCamera(), counter.getTickDelta(true));
                }
                if(deleteMode){
                    for (AreaBox selectedArea : getAreasToDelete(lookingAt,false)) {
                        drawArea(matrixStack, selectedArea.minPos, selectedArea.maxPos, counter.getTickDelta(true), Color.red, 0.4f);
                    }
                }
                for (AreaBox selectedArea : selectedAreas) {
                    drawArea(matrixStack, selectedArea.minPos, selectedArea.maxPos, counter.getTickDelta(true), selectedArea.color, 0.02f);
                }
            }
        }
    }
    public static void clear(){
        boxes.clear();
        lines.clear();
        cubes.clear();
        shineMarkers.forEach(marker->{
            marker.lifeTime--;
            if(marker.lifeTime <= 0){
                shineMarkers.remove(marker);
            }
        });
    }
    public static double mapAlpha(double x, float min, float max) {
        if (x < min) x = min;
        if (x > max) x = max;

        return (x - min) / (max - min);
    }


}
