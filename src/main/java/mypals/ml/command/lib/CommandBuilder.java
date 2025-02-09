package mypals.ml.command.lib;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CommandBuilder<S extends CommandSource> {
    private LiteralArgumentBuilder<S> root;
    private LiteralArgumentBuilder<S> commandNode;
    private final List<String> argumentNames = new ArrayList<>();
    private final List<ArgumentType<?>> argumentTypes = new ArrayList<>();
    private final List<Class<?>> argumentClasses = new ArrayList<>();
    private final EnvType environment;
    private String rootName;
    private String feedbackMessage;
    private Formatting feedbackColor = Formatting.WHITE;

    @SuppressWarnings("unchecked")
    private CommandBuilder(String name, EnvType environment) {
        this.environment = environment;
        this.rootName = name;

        if (environment == EnvType.CLIENT) {
            this.root = (LiteralArgumentBuilder<S>) ClientCommandManager.literal(name);
        } else {
            this.root = (LiteralArgumentBuilder<S>) CommandManager.literal(name);
        }
        this.commandNode = root; // 初始情况下，命令节点就是 root
    }

    public static CommandBuilder<FabricClientCommandSource> clientCommand(String name) {
        return new CommandBuilder<>(name, EnvType.CLIENT);
    }

    public static CommandBuilder<ServerCommandSource> serverCommand(String name) {
        return new CommandBuilder<>(name, EnvType.SERVER);
    }

    /**
     * 指定命令的根前缀，例如 "/lucidity enableWorldEaterHelper"
     */
    @SuppressWarnings("unchecked")
    public CommandBuilder<S> root(String rootName) {
        this.rootName = rootName;

        LiteralArgumentBuilder<S> newRoot;
        if (environment == EnvType.CLIENT) {
            newRoot = (LiteralArgumentBuilder<S>) ClientCommandManager.literal(rootName);
        } else {
            newRoot = (LiteralArgumentBuilder<S>) CommandManager.literal(rootName);
        }

        this.root = newRoot;
        this.commandNode = (LiteralArgumentBuilder<S>) ClientCommandManager.literal(this.rootName);
        root.then(this.commandNode);
        return this;
    }

    public <T> CommandBuilder<S> argument(String name, ArgumentType<T> type, Class<T> clazz) {
        argumentNames.add(name);
        argumentTypes.add(type);
        argumentClasses.add(clazz);
        return this;
    }

    @SuppressWarnings("unchecked")
    public CommandBuilder<S> execute(Consumer<Object[]> action) {
        if (argumentNames.isEmpty()) {
            commandNode.executes(ctx -> executeCommand(ctx, action, 0));
        } else {
            ArgumentBuilder<S, ?> argBuilder = commandNode;
            for (int i = 0; i < argumentNames.size(); i++) {
                int finalI = i;
                argBuilder = argBuilder.then(
                        (ArgumentBuilder<S, ?>) CommandManager.argument(argumentNames.get(i), argumentTypes.get(i))
                                .executes(ctx -> executeCommand(ctx, action, finalI + 1))
                );
            }
            commandNode.then(argBuilder);
        }
        return this;
    }
    public CommandBuilder<S> feedBack(String message, Formatting color) {
        this.feedbackMessage = message;
        this.feedbackColor = color;
        return this;
    }

    private int executeCommand(CommandContext<?> ctx, Consumer<Object[]> action, int argCount) {
        Object[] args = new Object[argCount];

        for (int i = 0; i < argCount; i++) {
            args[i] = ctx.getArgument(argumentNames.get(i), argumentClasses.get(i));
        }

        action.accept(args);

        // 在 CLIENT 端发送反馈
        if (environment == EnvType.CLIENT) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && feedbackMessage != null) {
                player.sendMessage(Text.literal(feedbackMessage).formatted(feedbackColor), false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("unchecked")
    public void register() {
        if (environment == EnvType.CLIENT) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register((LiteralArgumentBuilder<FabricClientCommandSource>) root);
            });
        } else {
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
                if (env.dedicated) {
                    dispatcher.register((LiteralArgumentBuilder<ServerCommandSource>) root);
                }
            });
        }
    }
}

