package mypals.ml.mixin;

import mypals.ml.config.LucidityConfig;
import mypals.ml.features.renderKeyPresses.KeyPressesManager;
import mypals.ml.features.selectiveRendering.WandActionsManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.config.Keybinds.switchRenderMode;
import static mypals.ml.features.renderKeyPresses.KeyPressesManager.getTranslatedMouseButtonName;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;

@Mixin(Mouse.class)
public class MouseMixin {
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButtonInject(long window, int button, int action, int mods, CallbackInfo ci) {
        // 确保事件来自当前客户端窗口
        if (window != this.client.getWindow().getHandle()) {
            return;
        }

        // 判断按键动作 (按下或释放)
        boolean isPressed = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE;
        if (!isPressed) return;

        // 获取按键名称
        String buttonName = getTranslatedMouseButtonName(button);

        if (buttonName != null) {
            KeyPressesManager.handleMouseEvent(buttonName, action == GLFW.GLFW_PRESS);
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void injectOnMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {

        if (window == this.client.getWindow().getHandle()) {
            if (this.client.player != null) {
                ItemStack mainHand = this.client.player.getMainHandStack();
                if (((mainHand.isOf(wand) || (this.client.player.isSpectator() && LucidityConfig.selectInSpectator)) && switchRenderMode.isPressed())) {
                    double sensitivity = this.client.options.getMouseWheelSensitivity().getValue();
                    double scrollAmount = (this.client.options.getDiscreteMouseScroll().getValue() ?
                            Math.signum(vertical) : vertical) * sensitivity;

                    if (scrollAmount > 0) {
                        WandActionsManager.switchRenderMod(true);
                    } else if (scrollAmount < 0) {
                        WandActionsManager.switchRenderMod(false);
                    }
                    ci.cancel();
                }
            }
        }
    }

}
