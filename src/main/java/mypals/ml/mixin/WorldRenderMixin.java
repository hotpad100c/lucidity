package mypals.ml.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.features.blockOutline.OutlineManager;
import mypals.ml.rendering.InformationRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.selectedAreas;

@Mixin(WorldRenderer.class)
public class WorldRenderMixin {

	@Inject(
			method = "method_62212",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;renderLate(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V")
	)private void render(CallbackInfo ci,
						 @Local MatrixStack stack
	) {
		InformationRender.render(new MatrixStack(), new RenderTickCounter() {
			@Override
			public float getLastFrameDuration() {
				return 0;
			}

			@Override
			public float getTickDelta(boolean ignoreFreeze) {
				return 0;
			}

			@Override
			public float getLastDuration() {
				return 0;
			}
		});
	}
}