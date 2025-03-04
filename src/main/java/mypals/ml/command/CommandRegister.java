package mypals.ml.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import mypals.ml.command.lib.CommandBuilder;
import mypals.ml.command.lib.CommandExecutor;
import mypals.ml.config.LucidityConfig;
import mypals.ml.features.ImageRendering.configuration.ImageConfigCommands;
import mypals.ml.features.ImageRendering.configuration.ImageConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

import static mypals.ml.Lucidity.onConfigUpdated;
import static mypals.ml.Lucidity.updateConfig;
import static mypals.ml.command.lib.CommandExecutor.setConfigValue;
import static mypals.ml.command.lib.CommandExecutor.setStaticField;

public class CommandRegister {
    public static void registerCommands() {
        CommandBuilder.clientCommand("enableWorldEaterHelper")
                .root("lucidity")
                .argument("toggle", BoolArgumentType.bool(),Boolean.class)
                .execute(args -> {
                    setConfigValue("enableWorldEaterHelper", args[0], LucidityConfig.class);
                })
                .feedBack("enableWorldEaterHelper -> " + LucidityConfig.enableWorldEaterHelper, Formatting.GOLD)
                .register();

        CommandBuilder.clientCommand("renderKeyPresses")
                .root("lucidity")
                .argument("toggle", BoolArgumentType.bool(),Boolean.class)
                .execute(args -> {
                    setConfigValue("renderKeyPresses", args[0], LucidityConfig.class);
                })
                .feedBack("renderKeyPresses -> " + LucidityConfig.renderKeyPresses, Formatting.GOLD)
                .register();
    }

}
