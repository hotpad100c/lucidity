package mypals.ml.config;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static org.lwjgl.glfw.GLFW.*;

public class Keybinds {
    public static KeyBinding addArea;
    public static KeyBinding switchRenderMode;
    public static KeyBinding deleteArea;


    public static void init() {
        // 注册快捷键
        addArea = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.selective_renderings.addSelection",
                InputUtil.Type.KEYSYM,
                GLFW_KEY_EQUAL,
                "category.selective_renderings"
        ));
        switchRenderMode = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.selective_renderings.renderingMode",
                InputUtil.Type.KEYSYM,
                GLFW_KEY_LEFT_ALT,
                "category.selective_renderings"
        ));
        deleteArea = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.selective_renderings.removeSelection",
                InputUtil.Type.KEYSYM,
                GLFW_KEY_MINUS,
                "category.selective_renderings"
        ));

    }
}
