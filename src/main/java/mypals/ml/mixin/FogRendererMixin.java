package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.config.LucidityConfig;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(BackgroundRenderer.class)
public class FogRendererMixin {
    @WrapMethod(
            method = "getFogColor(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)Lorg/joml/Vector4f;"
    )
    private static Vector4f modifyFogColor(Camera camera, float tickDelta, ClientWorld world, int clampedViewDistance, float skyDarkness, Operation<Vector4f> original) {
        Vector4f finalColor = original.call(camera, tickDelta, world, clampedViewDistance, skyDarkness);
        CameraSubmersionType type = camera.getSubmersionType();
        if (LucidityConfig.fluidTransparency <= 1.0 && (
                type == CameraSubmersionType.LAVA || type == CameraSubmersionType.WATER)) {
            float newAlpha = MathHelper.clamp(finalColor.w *
                            LucidityConfig.fluidTransparency,
                    0.0f, 1.0f);
            return new Vector4f(finalColor.x, finalColor.y, finalColor.z, newAlpha);
        }
        return finalColor;
    }
}
