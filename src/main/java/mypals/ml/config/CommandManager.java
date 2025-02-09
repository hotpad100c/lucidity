package mypals.ml.config;

import net.minecraft.util.Formatting;

import java.lang.reflect.Field;

import static mypals.ml.Lucidity.onConfigUpdated;

public class CommandManager {
    public static void setStaticBooleanField(String fieldName, boolean flag) {
        try {
            Class<?> clazz = LucidityConfig.class;
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, flag);
            LucidityConfig.CONFIG_HANDLER.save();
            onConfigUpdated();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed to update field '" + fieldName + "': " + e.getMessage());
        }
    }
    public static void setStaticFloatField(String fieldName, float value) {
        try {
            Class<?> clazz = LucidityConfig.class;
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
            LucidityConfig.CONFIG_HANDLER.save();
            onConfigUpdated();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed to update field '" + fieldName + "': " + e.getMessage());
        }
    }
    public static void setStaticIntField(String fieldName, int value) {
        try {
            Class<?> clazz = LucidityConfig.class;
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
            LucidityConfig.CONFIG_HANDLER.save();
            onConfigUpdated();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed to update field '" + fieldName + "': " + e.getMessage());
        }
    }
    private static Formatting getEnabledOrDisabledColor(boolean enable){
        return enable?Formatting.GREEN:Formatting.GRAY;
    }
}
