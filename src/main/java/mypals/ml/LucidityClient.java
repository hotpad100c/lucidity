package mypals.ml;

import mypals.ml.features.arrowCamera.ArrowCamera;
import net.fabricmc.api.ClientModInitializer;

public class LucidityClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArrowCamera.onInitialize();
    }
}
