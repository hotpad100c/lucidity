package mypals.ml.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mypals.ml.config.LucidityConfig;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidRenderer.class)
public class FluidRenderMixin {

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/FluidRenderer;vertex(Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFI)V"
            )
    )
    private void wrapVertex(FluidRenderer instance, VertexConsumer vertexConsumer, float x, float y, float z, float red, float green, float blue, float u, float v, int light, Operation<Void> original) {
        float alpha = LucidityConfig.fluidTransparency;
        if(alpha >= 1.0f){
            original.call(instance, vertexConsumer, x, y, z, red, green, blue, u, v, light);
        }else{
            vertexConsumer.vertex(x, y, z)
                    .color(red, green, blue, alpha)
                    .texture(u, v)
                    .light(light)
                    .normal(0.0F, 1.0F, 0.0F);
        }
    }
}
