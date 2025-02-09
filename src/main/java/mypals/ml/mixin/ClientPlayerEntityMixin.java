package mypals.ml.mixin;

import mypals.ml.config.LucidityConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Redirect(
            method = "tickNausea(Z)V",  // 方法签名：tickNausea(boolean)
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;closeHandledScreen()V"
            )
    )
    private void disablePortalCloseHandledScreen(ClientPlayerEntity self) {
        if(!LucidityConfig.guiInPortals)
            self.closeHandledScreen();
    }

    @Redirect(
            method = "tickNausea(Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"
            )
    )
    private void disablePortalSetScreen(MinecraftClient instance, Screen screen) {
        if(!LucidityConfig.guiInPortals)
            instance.setScreen(null);
    }
}
