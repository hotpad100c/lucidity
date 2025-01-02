package mypals.ml.mixin;

import mypals.ml.Lucidity;
import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.blockRenderMode;

@Mixin(value = LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getGamma()Lnet/minecraft/client/option/SimpleOption;", opcode = Opcodes.INVOKEVIRTUAL), method = "update(F)V")
    private SimpleOption<Double> getFieldValue(GameOptions options) {
        if (!blockRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF)) {
            return Lucidity.GAMMA_BYPASS;
        } else {
            return options.getGamma();
        }
    }
}
