package mypals.ml.features.ImageRendering.configuration;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import mypals.ml.config.ScreenGenerator;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public class ImageConfigCommands {
    public static void register(){
        ClientCommandRegistrationCallback.EVENT.register(ImageConfigCommands::register);
    }
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess){
        dispatcher.register(ClientCommandManager.literal("image")
                .then(ClientCommandManager.literal("config")
                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    // 打开自定义 GUI 界面
                                    openImageRenderingConfigGUI(name);
                                    return 1;
                                })
                        )
                )
        );
    }
    public static void openImageRenderingConfigGUI(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            client.setScreen(new ImageConfigScreen(Text.literal(name)));
        });
    }

}
