package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.ParseResults;
import mypals.ml.config.LucidityConfig;
import mypals.ml.features.pastBlockEvents.ClientsideBlockEventManager;
import mypals.ml.features.solarWizard.SolarWizard;
import mypals.ml.rendering.InformationRender;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkMixin {

    @Shadow private ClientWorld world;

    @Shadow protected abstract ParseResults<CommandSource> parse(String command);

    @Inject(method = "onBlockEvent", at = @At("HEAD"), cancellable = true)
    public void onBlockUpdate(BlockEventS2CPacket packet, CallbackInfo ci) {
        ClientsideBlockEventManager.addSyncedBlockEvent(packet.getPos(),packet.getBlock(),packet.getType(),packet.getData());
    }




}
