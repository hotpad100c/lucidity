package mypals.ml;

import mypals.ml.features.arrowCamera.ArrowCamera;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public class LucidityClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArrowCamera.onInitialize();
    }
}
