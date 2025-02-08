package mypals.ml.mixin;

import mypals.ml.config.LucidityConfig;
import mypals.ml.rendering.InformationRender;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkMixin {

    @Shadow private ClientWorld world;

    @Inject(method = "onBlockUpdate", at = @At("HEAD"), cancellable = true)
    public void onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
    }
}
