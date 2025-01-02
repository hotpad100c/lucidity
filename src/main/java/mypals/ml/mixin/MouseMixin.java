package mypals.ml.mixin;

import mypals.ml.features.renderKeyPresses.KeyPressesManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.features.renderKeyPresses.KeyPressesManager.getTranslatedMouseButtonName;

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

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScrollInject(long window, double horizontal, double vertical, CallbackInfo ci) {
        // 确保事件来自当前客户端窗口
        if (window != this.client.getWindow().getHandle()) {
            return;
        }

        // 将滚动信息传递给管理类
        KeyPressesManager.handleMouseScrollEvent(horizontal, vertical);
    }

}
