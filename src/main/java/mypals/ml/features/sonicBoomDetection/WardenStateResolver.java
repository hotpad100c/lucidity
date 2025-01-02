package mypals.ml.features.sonicBoomDetection;

import mypals.ml.Lucidity;
import mypals.ml.rendering.shapes.BoxShape;
import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.Line;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;


public class WardenStateResolver {
    public static void resolveWardenState(WardenEntity wardenEntity) {

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (wardenEntity.chargingSonicBoomAnimationState.getTimeRunning() > 0) {
            InformationRender.addBox(new BoxShape(wardenEntity.getBlockPos(),30,30,40, Color.cyan,0.2f));
        }
        if(player!=null && (!player.isCreative() && !player.isSpectator()) && wardenEntity.isInRange(player,16)){
            InformationRender.addLine(new Line(wardenEntity.getEyePos(), player.getPos().add(0,1,0),Color.cyan,0.5f));
        }
    }
}
