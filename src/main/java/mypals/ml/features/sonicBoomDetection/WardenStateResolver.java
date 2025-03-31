package mypals.ml.features.sonicBoomDetection;

import mypals.ml.rendering.shapes.BoxShape;
import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.LineShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;


public class WardenStateResolver {
    public static void resolveWardenState(WardenEntity wardenEntity) {
        if (wardenEntity.chargingSonicBoomAnimationState.getTimeInMilliseconds((float)wardenEntity.age) > 0) {
            InformationRender.addBox(new BoxShape(wardenEntity.getBlockPos().toCenterPos(),30,30,40, Color.cyan,0.2f,true));
        }
    }
}
