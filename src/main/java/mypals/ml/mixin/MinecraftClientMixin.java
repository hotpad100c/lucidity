package mypals.ml.mixin;

import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At(value = "HEAD"), method = "isAmbientOcclusionEnabled()Z", cancellable = true)
    private static void isAmbientOcclusionEnabled(CallbackInfoReturnable<Boolean> cir) {
        if (!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
