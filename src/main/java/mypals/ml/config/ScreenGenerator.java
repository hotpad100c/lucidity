package mypals.ml.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import mypals.ml.Lucidity;
import mypals.ml.features.explosionVisualizer.simulate.ExplosionSimulator;
import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import mypals.ml.features.selectiveRendering.WandActionsManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

import static mypals.ml.features.advancedAdvancedTooltip.AdvancedAdvancedToolTip.DEFAULT_TOOLTIP_OPTIONS;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.*;
import static mypals.ml.features.selectiveRendering.WandActionsManager.resolveWandMode;
import static mypals.ml.features.OreFinder.MineralFinder.DEFAULT_SELECTED;

public class ScreenGenerator {
    public static Screen getConfigScreen(Screen screen){
        var instance = LucidityConfig.CONFIG_HANDLER;
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("config.lucidity.title"))
                .category(
                        ConfigCategory.createBuilder()
                                .name(Text.translatable("config.lucidity.category.selectiveRenderings"))
                                //==================================================
                                .group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("config.lucidity.category.selectedBlock"))
                                        .description
                                                (OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.selectiveRenderings"))

                                                        .build()
                                                )
                                        .binding(
                                                new ArrayList<>(),
                                                () -> {
                                                    if (LucidityConfig.selectedBlockTypes == null) {
                                                        LucidityConfig.selectedBlockTypes = new ArrayList<>();
                                                    }
                                                    return LucidityConfig.selectedBlockTypes;
                                                },
                                                list -> LucidityConfig.selectedBlockTypes = list
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .initial("")
                                        .build()
                                ).group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("config.lucidity.category.selectedEntity"))
                                        .description
                                                (OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.selectiveRenderings"))

                                                        .build()
                                                )
                                        .binding(
                                                new ArrayList<>(),
                                                () -> {
                                                    // 返回配置值，确保非空
                                                    if (LucidityConfig.selectedEntityTypes == null) {
                                                        LucidityConfig.selectedEntityTypes = new ArrayList<>();
                                                    }
                                                    return LucidityConfig.selectedEntityTypes;
                                                },
                                                list -> LucidityConfig.selectedEntityTypes = list
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .initial("")
                                        .build()
                                ).group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("config.lucidity.category.selectedParticle"))
                                        .description
                                                (OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.selectiveRenderings"))

                                                        .build()
                                                )
                                        .binding(
                                                new ArrayList<>(),
                                                () -> {
                                                    if (LucidityConfig.selectedParticleTypes == null) {
                                                        LucidityConfig.selectedParticleTypes = new ArrayList<>();
                                                    }
                                                    return LucidityConfig.selectedParticleTypes;
                                                },
                                                list -> LucidityConfig.selectedParticleTypes = list
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .initial("")
                                        .build()
                                ).group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("config.lucidity.option.selectedAreaRender"))
                                        .description
                                                (OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.selectiveAreaRender"))
                                                        .build()
                                                )
                                        .binding(
                                                new ArrayList<>(),
                                                () -> {
                                                    if (LucidityConfig.selectedAreasSaved == null) {
                                                        LucidityConfig.selectedAreasSaved = new ArrayList<>();
                                                    }
                                                    return LucidityConfig.selectedAreasSaved;
                                                },
                                                list -> LucidityConfig.selectedAreasSaved = list
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .initial("")
                                        .build()
                                )
                                //==================================================
                                .group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.category.selectiveRenderings"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.selectiveRenderings"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.option.spectator_select"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.spectator_select"))
                                                                .build()
                                                        )
                                                        .binding(true, () -> LucidityConfig.selectInSpectator, bool -> LucidityConfig.selectInSpectator = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.renderSelectionMarker"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.renderSelectionMarker"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.renderSelectionMarker, bool -> LucidityConfig.renderSelectionMarker = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.autoNightVision"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.autoNightVision"))
                                                                .build()
                                                        )
                                                        .binding(true, () -> LucidityConfig.autoNightVision, bool -> LucidityConfig.autoNightVision = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        )
                                        .option(Option.<String>createBuilder()
                                                .name(Text.translatable("config.lucidity.option.wand"))
                                                .description(OptionDescription.of(Text.translatable("config.lucidity.description.wand")))
                                                .binding("minecraft:breeze_rod", () -> LucidityConfig.wand, s -> LucidityConfig.wand = s)
                                                .controller(StringControllerBuilder::create)
                                                .build()
                                        ).option(Option.<Integer>createBuilder()
                                                .name(Text.translatable("config.lucidity.option.wand_apply"))
                                                .description(OptionDescription.of(Text.translatable("config.lucidity.description.wand_apply")))
                                                .binding(0, () -> LucidityConfig.wandApplyMode, v -> LucidityConfig.wandApplyMode = v)
                                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                        .range(0, WandActionsManager.WandApplyToMode.values().length-1)
                                                        .step(1)
                                                        .formatValue(val -> Text.translatable(resolveWandMode(val))))
                                                .build()
                                        ).option(Option.<Integer>createBuilder()
                                                .name(Text.translatable("config.lucidity.render_mode.rendering_mode_block"))
                                                .description(OptionDescription.of(Text.translatable("config.lucidity.render_mode.rendering_mode_block")))
                                                .binding(0, () -> LucidityConfig.renderModeBlock, v -> LucidityConfig.renderModeBlock = v)
                                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                        .range(0, RenderMode.values().length-1)
                                                        .step(1)
                                                        .formatValue(val -> Text.translatable(resolveSelectiveBlockRenderingMode(val))))
                                                .build()
                                        )
                                        .option(Option.<Integer>createBuilder()
                                                .name(Text.translatable("config.lucidity.render_mode.rendering_mode_entity"))
                                                .description(OptionDescription.of(Text.translatable("config.lucidity.render_mode.rendering_mode_entity")))
                                                .binding(0, () -> LucidityConfig.renderModeEntity, v -> LucidityConfig.renderModeEntity = v)
                                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                        .range(0, RenderMode.values().length-1)
                                                        .step(1)
                                                        .formatValue(val -> Text.translatable(resolveSelectiveEntityRenderingMode(val))))
                                                .build()
                                        )
                                        .option(Option.<Integer>createBuilder()
                                                .name(Text.translatable("config.lucidity.render_mode.rendering_mode_particle"))
                                                .description(OptionDescription.of(Text.translatable("config.lucidity.render_mode.rendering_mode_particle")))
                                                .binding(0, () -> LucidityConfig.renderModeParticle, v -> LucidityConfig.renderModeParticle = v)
                                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                        .range(0, RenderMode.values().length-1)
                                                        .step(1)
                                                        .formatValue(val -> Text.translatable(resolveSelectiveParticleRenderingMode(val))))
                                                .build()
                                        )
                                        .build()
                                )

                                .build()
                ).category(
                        ConfigCategory.createBuilder()
                                .name(Text.translatable("config.lucidity.category.pictureRenderings"))
                                .group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("config.lucidity.pictures"))
                                        .binding(
                                                new ArrayList<>(),
                                                () -> {
                                                    if (LucidityConfig.picturesToRender == null) {
                                                        LucidityConfig.picturesToRender = new ArrayList<>();
                                                    }
                                                    return LucidityConfig.picturesToRender;
                                                },
                                                list -> LucidityConfig.picturesToRender = list
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .initial(()->{

                                            if(MinecraftClient.getInstance().player != null) {
                                                Vec3d pos = MinecraftClient.getInstance().player.getPos();
                                                return String.format("path;name;[" + pos.x + ","+ pos.y + "," + pos.z +"];[0,0,0];[1,1];true");
                                            }else{
                                                return "path;name;[x,y,z];[0,0,0];[1,1];true";
                                            }

                                        })
                                        .build()
                                ).group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.category.pictureRenderings"))
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.translatable("config.lucidity.pixel_per_block"))
                                                .description(OptionDescription.of(Text.translatable("config.lucidity.description.pixel_per_block")))
                                                .binding(377.95f, () -> LucidityConfig.pixelsPerBlock, v -> LucidityConfig.pixelsPerBlock = v)
                                                .controller(FloatFieldControllerBuilder::create)
                                                .build()
                                        )
                                        .build()
                                )

                                .build()
                ).category(
                        ConfigCategory.createBuilder()
                                .name(Text.translatable("config.lucidity.category.oreHighlight"))
                                //==================================================
                                .group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.category.oreHighlight"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.oreHighlight"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.option.oreHighlight"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.oreHighlight"))
                                                                .text(Text.of(""))
                                                                .text(Text.translatable("warn.lucidity.photosensitive-seizure-warning"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.enableWorldEaterHelper, bool -> LucidityConfig.enableWorldEaterHelper = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(Option.<Integer>createBuilder()
                                                .name(Text.translatable("config.lucidity.option.oreHighlightRange"))
                                                .binding(20, () -> LucidityConfig.oreHighlightRange, v -> LucidityConfig.oreHighlightRange = v)
                                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                        .range(0, 100)
                                                        .step(1)
                                                        .formatValue(val -> Text.of(val.toString())))
                                                .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.option.palette"))

                                                        .binding(Color.white, () -> LucidityConfig.colorPalette, color -> LucidityConfig.colorPalette = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(false))
                                                        .build()
                                        )
                                        .build()
                                ).group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("config.lucidity.category.selectedBlock"))
                                        .description
                                                (OptionDescription.createBuilder()
                                                        .build()
                                                )
                                        .binding(
                                                DEFAULT_SELECTED,
                                                () -> {
                                                    if (LucidityConfig.selectedBlocksToHighLight == null) {
                                                        LucidityConfig.selectedBlocksToHighLight = new ArrayList<>();
                                                    }
                                                    return LucidityConfig.selectedBlocksToHighLight;
                                                },
                                                list -> LucidityConfig.selectedBlocksToHighLight = list
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .initial(String.format(";#%06X", (LucidityConfig.colorPalette.getRGB() & 0xFFFFFF)))
                                        .build()
                                )

                                .build()
                ).category(
                        ConfigCategory.createBuilder()
                                .name(Text.translatable("config.lucidity.keyPresses"))
                                //==================================================
                                .group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.keyPresses"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.keyPresses"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.keyPresses"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.keyPresses"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.renderKeyPresses, bool -> LucidityConfig.renderKeyPresses = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        )
                                        .build()
                                )

                                .build()
                ).category(
                        ConfigCategory.createBuilder()
                                .name(Text.translatable("config.lucidity.damageIndicator"))
                                //==================================================
                                .group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.damageIndicator"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.damageIndicator"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.damageIndicator.enable"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.damageIndicator"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.enableDamageIndicator, bool -> LucidityConfig.enableDamageIndicator = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Integer>createBuilder()
                                                        .name(Text.translatable("config.lucidity.damageIndicator.offset"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.damageIndicator.offset"))
                                                                .build()
                                                        )
                                                        .binding(0, () -> LucidityConfig.indicatorOffset, integer -> LucidityConfig.indicatorOffset = integer)
                                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                                .range(0, 50)
                                                                .step(1)
                                                                .formatValue(val -> Text.of(val.toString())))
                                                        .build()
                                        ).option(
                                                Option.<Integer>createBuilder()
                                                        .name(Text.translatable("config.lucidity.damageIndicator.time"))

                                                        .binding(0, () -> LucidityConfig.damageIndicatorLifeTime, integer -> LucidityConfig.damageIndicatorLifeTime = integer)
                                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                                .range(50, 100)
                                                                .step(1)
                                                                .formatValue(val -> Text.of(val.toString())))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.damageIndicator.color"))

                                                        .binding(Color.red, () -> LucidityConfig.indicatorColor, color -> LucidityConfig.indicatorColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.clientsideDamageCalculation"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.clientsideDamageCalculation"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.damageCaculator, bool -> LucidityConfig.damageCaculator = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        )

                                        .build()
                                )

                                .build()
                ).category(
                        ConfigCategory.createBuilder()
                                .name(Text.translatable("config.lucidity.heighLightLava"))
                                //==================================================
                                .group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.heighLightLava"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.heighLightLava"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.heighLightLava"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.heighLightLava"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.fluidSourceHighLight, bool -> LucidityConfig.fluidSourceHighLight = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Float>createBuilder()
                                                        .name(Text.translatable("config.lucidity.fluidTransparency"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.fluidTransparency"))
                                                                .build()
                                                        )
                                                        .binding(1.01f, () -> LucidityConfig.fluidTransparency, v -> LucidityConfig.fluidTransparency = v)
                                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                                .range(0f, 1.01f)
                                                                .step(0.01f)
                                                                .formatValue(val -> Text.of((val < 1f)? val.toString() : "-")))
                                                        .build()

                                        )
                                        .build()
                                )

                                .build()
                )
                .category(
                        ConfigCategory.createBuilder()
                                .name(Text.translatable("config.lucidity.trajectory"))
                                //==================================================
                                .group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.trajectory"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.trajectory"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.trajectory"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.trajectory"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.renderTrajectory, bool -> LucidityConfig.renderTrajectory = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.mob_color"))

                                                        .binding(Color.orange, () -> LucidityConfig.mobTrajectoryColor, color -> LucidityConfig.mobTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.player_color"))

                                                        .binding(Color.green, () -> LucidityConfig.selfTrajectoryColor, color -> LucidityConfig.selfTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.self_color"))

                                                        .binding(Color.magenta, () -> LucidityConfig.playerTrajectoryColor, color -> LucidityConfig.playerTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.liner_projectiles_color"))

                                                        .binding(Color.red, () -> LucidityConfig.linerTrajectoryColor, color -> LucidityConfig.linerTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.far_projectiles_color"))

                                                        .binding(Color.yellow, () -> LucidityConfig.farTrajectoryColor, color -> LucidityConfig.farTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.near_projectiles_color"))

                                                        .binding(Color.cyan, () -> LucidityConfig.nearTrajectoryColor, color -> LucidityConfig.nearTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        )
                                        .build()
                                )

                                .build()
                )

                .title(Text.translatable("config.title.explosion_visualizer"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.category.explosion_visualizer"))

                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("config.group.renders"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("config.description.main_features"))
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.main_render"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.main_render").formatted(Formatting.YELLOW))
                                                .image(Lucidity.id("textures/examples/main-render.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(true, () -> LucidityConfig.enableExplosionVisualizer, bool -> LucidityConfig.enableExplosionVisualizer = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.block_destruction"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.block_destruction"))
                                                .image(Lucidity.id("textures/examples/block-destruction.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> LucidityConfig.showBlockDestroyInfo, bool -> LucidityConfig.showBlockDestroyInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.block_detection_ray"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.block_detection_ray"))
                                                .image(Lucidity.id("textures/examples/ray.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> LucidityConfig.showExplosionBlockDamageRayInfo, bool -> LucidityConfig.showExplosionBlockDamageRayInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.sample_points"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.sample_points"))
                                                .image(Lucidity.id("textures/examples/sample-points.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> LucidityConfig.showRayCastInfo, bool -> LucidityConfig.showRayCastInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.explosion_damage"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.explosion_damage"))
                                                .image(Lucidity.id("textures/examples/damage.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> LucidityConfig.showDamageInfo, bool -> LucidityConfig.showDamageInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("config.group.block_detection_ray_settings"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("config.description.block_detection_ray_settings"))
                                        .image(Lucidity.id("textures/examples/example.png"), 500, 500)
                                        .build()
                                )
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.x_min"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.x_min")))
                                        .binding(0, () -> LucidityConfig.Xmin, v -> LucidityConfig.Xmin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.x_max"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.x_max")))
                                        .binding(15, () -> LucidityConfig.Xmax, v -> LucidityConfig.Xmax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.y_min"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.y_min")))
                                        .binding(0, () -> LucidityConfig.Ymin, v -> LucidityConfig.Ymin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.y_max"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.y_max")))
                                        .binding(15, () -> LucidityConfig.Ymax, v -> LucidityConfig.Ymax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.z_min"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.z_min")))
                                        .binding(0, () -> LucidityConfig.Zmin, v -> LucidityConfig.Zmin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.z_max"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.z_max")))
                                        .binding(15, () -> LucidityConfig.Zmax, v -> LucidityConfig.Zmax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.invert"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.invert"))
                                                .image(Lucidity.id("textures/examples/invert.png"), 192, 108)
                                                .build())
                                        .binding(false, () -> LucidityConfig.invert, bool -> LucidityConfig.invert = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.layer_min"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.layer_min")))
                                        .binding(0, () -> LucidityConfig.LayerMin, v -> LucidityConfig.LayerMin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 100)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.layer_max"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.layer_max")))
                                        .binding(100, () -> LucidityConfig.LayerMax, v -> LucidityConfig.LayerMax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 100)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.option.block_detection_ray_icon"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.block_detection_ray_icon")))
                                        .binding("⧈", () -> LucidityConfig.BlockDetectionRayIcon, v -> LucidityConfig.BlockDetectionRayIcon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build()
                                )

                                .option(Option.<Float>createBuilder()
                                        .name(Text.translatable("config.option.block_detection_ray_icon_size"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.block_detection_ray_icon_size")))
                                        .binding(0.005F, () -> LucidityConfig.BlockDetectionRayIconSize, v -> LucidityConfig.BlockDetectionRayIconSize = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0F, 0.1F)
                                                .step(0.005F)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.render_with_alpha"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.render_with_alpha")))
                                        .binding(false, () -> LucidityConfig.EnableAlpha, bool -> LucidityConfig.EnableAlpha = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.see_Throw"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.see_Throw")))
                                        .binding(false, () -> LucidityConfig.BlockDetectionRaySeeThrow, bool -> LucidityConfig.BlockDetectionRaySeeThrow = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.type"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.type")))
                                        .binding(0, () -> LucidityConfig.ColorType, v -> LucidityConfig.ColorType = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 2)
                                                .step(1)
                                                .formatValue(ExplosionSimulator::getColoringTypeForRays))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.+y"))
                                        .binding(Color.YELLOW, () -> LucidityConfig.Colored_UP, v -> LucidityConfig.Colored_UP = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.-y"))
                                        .binding(Color.GREEN, () -> LucidityConfig.Colored_DOWN, v -> LucidityConfig.Colored_DOWN = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.+x"))
                                        .binding(Color.RED, () -> LucidityConfig.Colored_FRONT, v -> LucidityConfig.Colored_FRONT = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.-x"))
                                        .binding(Color.BLUE, () -> LucidityConfig.Colored_BACK, v -> LucidityConfig.Colored_BACK = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.+z"))
                                        .binding(Color.MAGENTA, () -> LucidityConfig.Colored_LEFT, v -> LucidityConfig.Colored_LEFT = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.-z"))
                                        .binding(Color.WHITE, () -> LucidityConfig.Colored_RIGHT, v -> LucidityConfig.Colored_RIGHT = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.single_color"))
                                        .binding(Color.WHITE, () -> LucidityConfig.Single_Color, v -> LucidityConfig.Single_Color = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )



                                .build()
                        )

                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("config.option.block_destruction_render_settings"))
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.block_destruction_render_color"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.block_destruction_render_color")))
                                        .binding(Color.YELLOW, () -> LucidityConfig.BlockDestroyIconColor, v -> LucidityConfig.BlockDestroyIconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.option.block_destruction_render_icon"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.block_destruction_render_icon")))
                                        .binding("⨂", () -> LucidityConfig.BlockDestroyIcon, v -> LucidityConfig.BlockDestroyIcon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Text.translatable("config.option.block_destruction_render_icon_size"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.block_destruction_render_icon_size")))
                                        .binding(0.05F, () -> LucidityConfig.BlockDestroyIconSize, v -> LucidityConfig.BlockDestroyIconSize = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0F, 0.2F)
                                                .step(0.05F)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.see_Throw"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.see_Throw")))
                                        .binding(true, () -> LucidityConfig.BlockDestroyIconSeeThrow, bool -> LucidityConfig.BlockDestroyIconSeeThrow = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )
                                .build()
                        )

                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("config.option.entity_sample_point_render_settings"))
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_safe_color"))
                                        .binding(Color.GREEN, () -> LucidityConfig.EntitySamplePoion_Safe_IconColor, v -> LucidityConfig.EntitySamplePoion_Safe_IconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_safe_icon"))
                                        .binding("√", () -> LucidityConfig.EntitySamplePoion_Safe_Icon, v -> LucidityConfig.EntitySamplePoion_Safe_Icon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_danger_color"))
                                        .binding(Color.RED, () -> LucidityConfig.EntitySamplePoion_Danger_IconColor, v -> LucidityConfig.EntitySamplePoion_Danger_IconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_danger_icon"))
                                        .binding("X", () -> LucidityConfig.EntitySamplePoion_Danger_Icon, v -> LucidityConfig.EntitySamplePoion_Danger_Icon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_blocked_color"))
                                        .binding(Color.MAGENTA, () -> LucidityConfig.EntitySamplePoion_Blocked_IconColor, v -> LucidityConfig.EntitySamplePoion_Blocked_IconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_blocked_icon"))
                                        .binding("❖", () -> LucidityConfig.EntitySamplePoion_Blocked_Icon, v -> LucidityConfig.EntitySamplePoion_Blocked_Icon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_icon_size"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.entity_sample_point_icon_size")))
                                        .binding(0.01F, () -> LucidityConfig.EntitySamplePoionIconSize, v -> LucidityConfig.EntitySamplePoionIconSize = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0F, 0.03F)
                                                .step(0.01F)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.see_Throw"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.see_Throw")))
                                        .binding(true, () -> LucidityConfig.EntitySamplePointSeeThrow, bool -> LucidityConfig.EntitySamplePointSeeThrow = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                ).category(
                        ConfigCategory.createBuilder()
                                .name(Text.translatable("config.lucidity.mod_debugs"))
                                //==================================================
                                .group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.mod_debugs"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.mod_debugs"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.mob_chase_range"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.mob_chase_range"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.renderMobChaseRange, bool -> LucidityConfig.renderMobChaseRange = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.mob_eye-line"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.mob_eye-line"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.renderMobEyeLineConnection, bool -> LucidityConfig.renderMobEyeLineConnection = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.mob_spawn"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.mob_spawn.warden_attack_range"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.renderMobSpawn, bool -> LucidityConfig.renderMobSpawn = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.warden_attack_range"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.warden_attack_range"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.renderWardenAttackRange, bool -> LucidityConfig.renderWardenAttackRange = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        )
                                        .build()
                                )

                                .build()
                ).category(
                        ConfigCategory.createBuilder()
                                .name(Text.translatable("config.lucidity.creative_helper"))
                                //==================================================
                                .group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.creative_helper"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.creative_helper"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.option.containerComparatorOutputPreview"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.containerComparatorOutputPreview"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.containerComparatorOutputPreview, bool -> LucidityConfig.containerComparatorOutputPreview = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        )
                                        .option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.command_helper"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.command_helper"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.commandHelper, bool -> LucidityConfig.commandHelper = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.betterStructureVoidBlock"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.betterStructureVoidBlock"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.betterStructureVoid, bool -> LucidityConfig.betterStructureVoid = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.betterBarrierBlock"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.betterBarrierBlock"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.betterBarrier, bool -> LucidityConfig.betterBarrier = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        )/*.option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.betterLightBlock"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.betterLightBlock"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.betterLight, bool -> LucidityConfig.betterLight = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        )*/.option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.forceTechnicalBlocksToRender"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.forceTechnicalBlocksToRender"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.forceRenderTechnicalBlocks, bool -> LucidityConfig.forceRenderTechnicalBlocks = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        )
                                        .build()
                                )
                                .build()

                ).category(
                        ConfigCategory.createBuilder()
                                .name(Text.translatable("config.lucidity.fun"))
                                //==================================================
                                .group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.fun"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.fun"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.arrowcam"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.arrowcam"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.arrowcam, bool -> LucidityConfig.arrowcam = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.advancedAdvancedToolTips"))
                                                        .binding(false, () -> LucidityConfig.advancedAdvancedToolTips, bool -> LucidityConfig.advancedAdvancedToolTips = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).build()
                                ).group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.soundEventRender"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.soundEventRender"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.soundEventRender"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.soundEventRender"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.renderSoundEvents, bool -> LucidityConfig.renderSoundEvents = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Integer>createBuilder()
                                                        .name(Text.translatable("config.lucidity.soundEventsExpiredTime"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.soundEventsExpiredTime"))
                                                                .build()
                                                        )
                                                        .binding(1000, () -> LucidityConfig.renderSoundEventsExpiredTime, bool -> LucidityConfig.renderSoundEventsExpiredTime = bool)
                                                        .controller(IntegerFieldControllerBuilder::create)
                                                        .build()
                                        ).option(Option.<Color>createBuilder()
                                                .name(Text.translatable("config.lucidity.soundEventRenderColor"))
                                                .binding(new Color(191, 255, 0,100), () -> LucidityConfig.renderSoundEventsColor, v -> LucidityConfig.renderSoundEventsColor = v)
                                                .controller(opt -> ColorControllerBuilder.create(opt)
                                                        .allowAlpha(true))
                                                .build()
                                        )
                                        .build()
                                ).group(OptionGroup.createBuilder()
                                        .name(Text.translatable("config.lucidity.blockEventRender"))
                                        .description(
                                                OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.blockEventRender"))
                                                        .build()
                                        ).option(
                                                Option.<Boolean>createBuilder()
                                                        .name(Text.translatable("config.lucidity.blockEventRender"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.blockEventRender"))
                                                                .build()
                                                        )
                                                        .binding(false, () -> LucidityConfig.renderBlockEvents, bool -> LucidityConfig.renderBlockEvents = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Integer>createBuilder()
                                                        .name(Text.translatable("config.lucidity.blockEventsExpiredTime"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.blockEventsExpiredTime"))
                                                                .build()
                                                        )
                                                        .binding(1000, () -> LucidityConfig.renderBlockEventsExpiredTime, bool -> LucidityConfig.renderBlockEventsExpiredTime = bool)
                                                        .controller(IntegerFieldControllerBuilder::create)
                                                        .build()
                                        ).option(Option.<Color>createBuilder()
                                                .name(Text.translatable("config.lucidity.blockEventRenderColor"))
                                                .binding(new Color(255,165,0,100), () -> LucidityConfig.renderBlockEventsColor, v -> LucidityConfig.renderBlockEventsColor = v)
                                                .controller(opt -> ColorControllerBuilder.create(opt)
                                                        .allowAlpha(true))
                                                .build()
                                        ).build()
                                ).group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("config.lucidity.advancedAdvancedToolTips"))
                                        .description
                                                (OptionDescription.createBuilder()
                                                        .build()
                                                )
                                        .binding(
                                                DEFAULT_TOOLTIP_OPTIONS,
                                                () -> {
                                                    if (LucidityConfig.blockToolTipAttributes == null) {
                                                        LucidityConfig.blockToolTipAttributes = new ArrayList<>();
                                                    }
                                                    return LucidityConfig.blockToolTipAttributes;
                                                },
                                                list -> LucidityConfig.blockToolTipAttributes = list
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .initial(String.format("#%06X", (LucidityConfig.colorPalette.getRGB() & 0xFFFFFF)))
                                        .build()
                                ).build()
                )


                .save(() -> {
                    instance.save();
                    Lucidity.onConfigUpdated();
                })
                .build()
                .generateScreen(screen);
    }
}
