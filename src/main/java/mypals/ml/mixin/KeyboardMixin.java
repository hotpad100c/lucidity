package mypals.ml.mixin;

import mypals.ml.features.renderKeyPresses.KeyPressesManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.features.renderKeyPresses.KeyPressesManager.getTranslatedKeyName;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKeyInject(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        // 确保 window 对应当前客户端窗口
        if (window != this.client.getWindow().getHandle()) {
            return;
        }

        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE) {
            boolean isPressed = action == GLFW.GLFW_PRESS;
            String keyName = getTranslatedKeyName(key, scancode);

            if (keyName != null) {
                KeyPressesManager.handleKeyEvent(keyName, isPressed);
            }
        }
    }
}