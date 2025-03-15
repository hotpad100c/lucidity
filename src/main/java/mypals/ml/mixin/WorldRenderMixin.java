package mypals.ml.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.features.blockOutline.OutlineManager;
import mypals.ml.rendering.InformationRender;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRenderMixin {
	@Shadow private @Nullable ClientWorld world;

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
	@ModifyVariable(method = "render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", at = @At(value = "STORE", ordinal = 0), name = "bl3")
	private boolean modifyBl3(boolean original) {
		if(!OutlineManager.targetedBlocks.isEmpty()) {
			return true;
		}
		return original;
	}
	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gl/Framebuffer;beginWrite(Z)V",
					ordinal = 1,
					shift = At.Shift.AFTER
			)
	)private void onRenderOutline(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
		//OutlineManager.onRenderOutline(tickCounter, camera,matrix4f);
	}


	}