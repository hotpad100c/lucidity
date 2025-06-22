package mypals.ml.mixin.vanilla;

import mypals.ml.config.LucidityConfig;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public class LucidityRenderLayersMixin {
    @Inject(method = "getFluidLayer", at = @At("HEAD"), cancellable = true)
    private static void injectCustomFluidRenderLayer(FluidState state, CallbackInfoReturnable<RenderLayer> cir) {
        if (LucidityConfig.fluidTransparency < 1.01f) {
            cir.setReturnValue(RenderLayer.getTranslucent());
        }
    }
}
