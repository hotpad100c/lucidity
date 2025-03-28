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

	@Inject(method = "render", at = @At(value = "INVOKE",target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/Camera;)V", ordinal = 0))
	private void render(CallbackInfo ci,
						@Local MatrixStack matrixStack,
						@Local(argsOnly = true) RenderTickCounter tickCounter
	) {
		InformationRender.render(matrixStack,tickCounter);
	}
	@Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("HEAD"))
	private void reload(CallbackInfo ci
	) {
		OutlineManager.init();
	}
	@Inject(method = "onResized(II)V", at = @At("HEAD"))
	private void resize(CallbackInfo ci
	) {
		OutlineManager.init();
	}
	@SuppressWarnings({"InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
	@ModifyVariable(
			method = "render",
			at = @At(
					value = "LOAD",
					ordinal = 0
			),
			ordinal = 3
	)
	private boolean forceOutline(boolean bl3) {
		return bl3 || !OutlineManager.targetedBlocks.isEmpty() || !selectedAreas.isEmpty();
	}
}