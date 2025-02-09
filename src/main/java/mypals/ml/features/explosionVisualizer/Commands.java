package mypals.ml.features.explosionVisualizer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.FakeExplosion;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.stream.Collectors;

public class Commands {
    /*public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        dispatcher.register(
                ClientCommandManager.literal("explosionVisualizer")
                        .then(ClientCommandManager.literal("mainRender")
                                .then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean toggle = BoolArgumentType.getBool(context, "toggle");

                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.main_render", toggle).getString()).formatted(Formatting.GOLD);

                                            assert player != null;
                                            player.sendMessage(coloredMessage, false);

                                            setOnOff(toggle);
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommandManager.literal("renderEntityDamage")
                                .then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean toggle = BoolArgumentType.getBool(context, "toggle");

                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.entity_damage_render", toggle).getString()).formatted(Formatting.GREEN);

                                            assert player != null;
                                            player.sendMessage(coloredMessage, false);

                                            setDamageOnOff(toggle);
                                            return 1;
                                        })
                                ))
                        .then(ClientCommandManager.literal("renderEntityRayCast")
                                .then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean toggle = BoolArgumentType.getBool(context, "toggle");

                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.entity_ray_cast", toggle).getString()).formatted(Formatting.GREEN);

                                            assert player != null;
                                            player.sendMessage(coloredMessage, false);

                                            setRayCastInfoOnOff(toggle);
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommandManager.literal("renderBlockDestruction")
                                .then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean toggle = BoolArgumentType.getBool(context, "toggle");

                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.block_destruction_render", toggle).getString()).formatted(Formatting.GREEN);

                                            assert player != null;
                                            player.sendMessage(coloredMessage, false);

                                            setBlockDestroyInfoOnOff(toggle);
                                            return 1;
                                        })
                                ))
                        .then(ClientCommandManager.literal("renderBlockDetectionRay")
                                .then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean toggle = BoolArgumentType.getBool(context, "toggle");

                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.block_detection_ray_render", toggle).getString()).formatted(Formatting.GREEN);

                                            assert player != null;
                                            player.sendMessage(coloredMessage, false);

                                            setExplosionBlockDamageRayInfoOnOff(toggle);
                                            return 1;
                                        })
                                ))
                        .then(ClientCommandManager.literal("blockDamageRayRendererSettings")
                                .then(ClientCommandManager.literal("range")
                                        .then(ClientCommandManager.argument("Xmin", IntegerArgumentType.integer(-1, 16))
                                                .then(ClientCommandManager.argument("Xmax", IntegerArgumentType.integer(-1, 16))
                                                        .then(ClientCommandManager.argument("Ymin", IntegerArgumentType.integer(-1, 16))
                                                                .then(ClientCommandManager.argument("Ymax", IntegerArgumentType.integer(-1, 16))
                                                                        .then(ClientCommandManager.argument("Zmin", IntegerArgumentType.integer(-1, 16))
                                                                                .then(ClientCommandManager.argument("Zmax", IntegerArgumentType.integer(-1, 16))
                                                                                        .executes(context -> {
                                                                                            int Xmin = IntegerArgumentType.getInteger(context, "Xmin");
                                                                                            int Xmax = IntegerArgumentType.getInteger(context, "Xmax");
                                                                                            int Ymin = IntegerArgumentType.getInteger(context, "Ymin");
                                                                                            int Ymax = IntegerArgumentType.getInteger(context, "Ymax");
                                                                                            int Zmin = IntegerArgumentType.getInteger(context, "Zmin");
                                                                                            int Zmax = IntegerArgumentType.getInteger(context, "Zmax");
                                                                                            SetDestructionRayRenderRange(Xmin, Xmax, Ymin,Ymax,Zmin,Zmax);

                                                                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.block_damage_ray_range_updated").getString()).formatted(Formatting.GREEN);

                                                                                            assert player != null;
                                                                                            player.sendMessage(coloredMessage, false);

                                                                                            return 1;
                                                                                        })
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(ClientCommandManager.literal("layer")
                                        .then(ClientCommandManager.argument("LayerMin", IntegerArgumentType.integer(0, 114514))
                                                .then(ClientCommandManager.argument("LayerMax", IntegerArgumentType.integer(0, 114514))
                                                        .executes(context -> {
                                                            int LayerMin = IntegerArgumentType.getInteger(context, "LayerMin");
                                                            int LayerMax = IntegerArgumentType.getInteger(context, "LayerMax");
                                                            SetDestructionRayRenderLayer(LayerMin,LayerMax);

                                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.block_damage_ray_layer_updated").getString()).formatted(Formatting.GREEN);

                                                            assert player != null;
                                                            player.sendMessage(coloredMessage, false);

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(ClientCommandManager.literal("resetAll")
                                        .executes(context -> {
                                            SetDestructionRayRenderLayer(0,100);
                                            SetDestructionRayRenderRange(0,16,0,16,0,16);

                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.block_detection_ray_settings_reset").getString()).formatted(Formatting.RED);

                                            assert player != null;
                                            player.sendMessage(coloredMessage, false);

                                            return 1;
                                        })
                                )
                                .then(ClientCommandManager.literal("resetLayer")
                                        .executes(context -> {
                                            SetDestructionRayRenderLayer(0,100);

                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.block_detection_ray_layer_reset").getString()).formatted(Formatting.YELLOW);

                                            assert player != null;
                                            player.sendMessage(coloredMessage, false);

                                            return 1;
                                        })
                                )
                                .then(ClientCommandManager.literal("resetRange")
                                        .executes(context -> {
                                            SetDestructionRayRenderRange(0,16,0,16,0,16);

                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.block_detection_ray_range_reset").getString()).formatted(Formatting.YELLOW);

                                            assert player != null;
                                            player.sendMessage(coloredMessage, false);

                                            return 1;
                                        })
                                )

                        )
                        .then(ClientCommandManager.literal("fakeExplosion")
                                .then(ClientCommandManager.literal("add")
                                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                                .then(ClientCommandManager.argument("x", FloatArgumentType.floatArg())
                                                        .then(ClientCommandManager.argument("y", FloatArgumentType.floatArg())
                                                                .then(ClientCommandManager.argument("z", FloatArgumentType.floatArg())
                                                                        .then(ClientCommandManager.argument("power", FloatArgumentType.floatArg())
                                                                                .then(ClientCommandManager.argument("ignoreBlockInside", BoolArgumentType.bool())
                                                                                        .executes(context -> {

                                                                                            float x = FloatArgumentType.getFloat(context, "x");
                                                                                            float y = FloatArgumentType.getFloat(context, "y");
                                                                                            float z = FloatArgumentType.getFloat(context, "z");
                                                                                            float p = FloatArgumentType.getFloat(context, "power");
                                                                                            boolean ignoreBlockInside = BoolArgumentType.getBool(context,"ignoreBlockInside");
                                                                                            String name = StringArgumentType.getString(context, "name");

                                                                                            for(FakeExplosion FE : fakeExplosions)
                                                                                            {
                                                                                                if(Objects.equals(FE.name, name)) {

                                                                                                    Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.fake_explosion_duplicate",name, new Vec3d(x, y, z).toString()).getString()).formatted(Formatting.RED);

                                                                                                    assert player != null;
                                                                                                    player.sendMessage(coloredMessage, false);
                                                                                                    return 1;
                                                                                                }
                                                                                            }
                                                                                            fakeExplosions.add(new FakeExplosion(x, y, z, p, ignoreBlockInside, name));
                                                                                            Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.fake_explosion_add", name, new Vec3d(x, y, z), p).getString()).formatted(Formatting.GREEN);
                                                                                            assert player != null;
                                                                                            player.sendMessage(coloredMessage, false);
                                                                                            return 1;
                                                                                        })
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(ClientCommandManager.literal("remove")
                                        .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                                .suggests(suggestFromSet(fakeExplosions))
                                                .executes(context -> {
                                                    String n = StringArgumentType.getString(context, "name");
                                                    fakeExplosions.removeIf(fe -> Objects.equals(fe.name, n));
                                                    Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.fake_explosion_remove" + n).getString()).formatted(Formatting.YELLOW);

                                                    assert player != null;
                                                    player.sendMessage(coloredMessage, false);
                                                    return 1;
                                                })
                                        )
                                        .then(ClientCommandManager.literal("all")
                                                .executes(context -> {
                                                    fakeExplosions.clear();
                                                    Text coloredMessage = Text.literal(Text.translatable("command.explosion-visualizer.fake_explosion_clear").getString()).formatted(Formatting.RED);

                                                    assert player != null;
                                                    player.sendMessage(coloredMessage, false);
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }*/
    private static SuggestionProvider<FabricClientCommandSource> suggestFromSet(Set<FakeExplosion> explosions) {
        return (context, builder) -> {

            Set<String> names = explosions.stream()
                    .map(fakeExplosion -> fakeExplosion.name)
                    .collect(Collectors.toSet());
            return CommandSource.suggestMatching(names, builder);
        };
    }

}
