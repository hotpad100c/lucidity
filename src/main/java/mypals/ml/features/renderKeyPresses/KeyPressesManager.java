package mypals.ml.features.renderKeyPresses;

import mypals.ml.config.LucidityConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import static mypals.ml.config.LucidityConfig.renderKeyPresses;
import static mypals.ml.features.renderKeyPresses.KeyPressTracker.*;

public class KeyPressesManager {
    public static void handleKeyEvent(String keyName, boolean isPressed) {
        if (isPressed) {
            if (keyName != null) onKeyPress(keyName);
        } else {
            if (keyName != null) onKeyRelease(keyName);
        }
    }
    public static void handleMouseEvent(String keyName, boolean isPressed){
        if (isPressed) {
            if (keyName != null) onKeyPress(keyName);
        } else {
            if (keyName != null) onKeyRelease(keyName);
        }
    }
    public static void handleMouseScrollEvent(double horizontal, double vertical){

    }
    public static String getTranslatedKeyName(int key, int scancode) {
        String translationKey = GLFW.glfwGetKeyName(key, scancode);
        InputUtil.Key inputKey = InputUtil.fromKeyCode(key, scancode);
        if (inputKey == null) {
            return "Unknown Key";
        }
        if(translationKey == null) {
            translationKey = inputKey.getTranslationKey();
        }
        return Text.translatable(translationKey).getString();
    }
    public static String getTranslatedMouseButtonName(int button) {
        // 提供翻译的按键名称
        return switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> I18n.translate("key.mouse.left");
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> I18n.translate("key.mouse.right");
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> I18n.translate("key.mouse.middle");
            default -> I18n.translate("key.mouse." + button);
        };
    }
    public static void renderPressed(DrawContext context) {
        LucidityConfig.CONFIG_HANDLER.instance();
        if (!getKeyDisplay().isEmpty() && renderKeyPresses && !MinecraftClient.getInstance().options.hudHidden) {
            int x = MinecraftClient.getInstance().getWindow().getScaledWidth() - 110;
            int y = MinecraftClient.getInstance().getWindow().getScaledHeight() - 20;
            context.drawText(MinecraftClient.getInstance().textRenderer, getKeyDisplay(), x, y, 0xFFFFFF, true);

        }

    }
}
