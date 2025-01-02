package mypals.ml.features.renderKeyPresses;

import java.util.LinkedHashSet;
import java.util.Set;

public class KeyPressTracker {
    public static final Set<String> pressedKeys = new LinkedHashSet<>();

    public static void onKeyPress(String keyName) {
        pressedKeys.add(keyName);
    }

    public static void onKeyRelease(String keyName) {
        pressedKeys.remove(keyName);
    }

    public static String getKeyDisplay() {
        return String.join(" + ", pressedKeys);
    }
}