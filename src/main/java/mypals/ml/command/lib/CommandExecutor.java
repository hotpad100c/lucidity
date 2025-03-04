package mypals.ml.command.lib;

import mypals.ml.config.LucidityConfig;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.Consumer;

import static mypals.ml.Lucidity.updateConfig;

public class CommandExecutor {
    public static <T> void setStaticField(String fieldName, T value, Class<?> target) {
        try {
            Field field = target.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed to update field '" + fieldName + "': " + e.getMessage());
        }
    }
    public static <T> void setConfigValue(String fieldName, T value, Class<?> target) {
        setStaticField(fieldName, value, target);
        LucidityConfig.CONFIG_HANDLER.save();
        updateConfig();

    }
}
