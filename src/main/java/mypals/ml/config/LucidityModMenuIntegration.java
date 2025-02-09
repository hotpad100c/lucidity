package mypals.ml.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import mypals.ml.Lucidity;
import mypals.ml.features.explosionVisualizer.simulate.ExplosionSimulator;
import mypals.ml.features.selectiveRendering.WandActionsManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.ArrayList;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.*;
import static mypals.ml.features.selectiveRendering.WandActionsManager.resolveWandMode;
import static mypals.ml.features.worldEaterHelper.MineralFinder.DEFAULT_SELECTED;

public class LucidityModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        var instance = LucidityConfig.CONFIG_HANDLER;
        return screen -> YetAnotherConfigLib.createBuilder()
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
                                                //.image(Lucidity.id("textures/main-render.png"), 192, 108)
                                                .build()
                                        )
                                .binding(
                                        new ArrayList<>(),
                                        () -> {
                                            // 返回配置值，确保非空
                                            if (instance.instance().selectedBlockTypes == null) {
                                                LucidityConfig.selectedBlockTypes = new ArrayList<>();
                                            }
                                            return instance.instance().selectedBlockTypes;
                                        },
                                        list -> instance.instance().selectedBlockTypes = list
                                )
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .build()
                        ).group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("config.lucidity.category.selectedEntity"))
                                        .description
                                                (OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.selectiveRenderings"))
                                                        //.image(Lucidity.id("textures/main-render.png"), 192, 108)
                                                        .build()
                                                )
                                        .binding(
                                                new ArrayList<>(),
                                                () -> {
                                                    // 返回配置值，确保非空
                                                    if (instance.instance().selectedEntityTypes == null) {
                                                        LucidityConfig.selectedEntityTypes = new ArrayList<>();
                                                    }
                                                    return instance.instance().selectedEntityTypes;
                                                },
                                                list -> instance.instance().selectedEntityTypes = list
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .initial("")
                                        .build()
                        ).group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("config.lucidity.category.selectedParticle"))
                                        .description
                                                (OptionDescription.createBuilder()
                                                        .text(Text.translatable("config.lucidity.description.selectiveRenderings"))
                                                        //.image(Lucidity.id("textures/main-render.png"), 192, 108)
                                                        .build()
                                                )
                                        .binding(
                                                new ArrayList<>(),
                                                () -> {
                                                    if (instance.instance().selectedParticleTypes == null) {
                                                        LucidityConfig.selectedParticleTypes = new ArrayList<>();
                                                    }
                                                    return instance.instance().selectedParticleTypes;
                                                },
                                                list -> instance.instance().selectedParticleTypes = list
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
                                            // 返回配置值，确保非空
                                            if (instance.instance().selectedAreasSaved == null) {
                                                LucidityConfig.selectedAreasSaved = new ArrayList<>();
                                            }
                                            return instance.instance().selectedAreasSaved;
                                            },
                                        list -> instance.instance().selectedAreasSaved = list
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
                                                        //.image(Lucidity.id(""), 192, 108)
                                                        .build()
                                                )
                                                .binding(true, () -> instance.instance().selectInSpectator, bool -> instance.instance().selectInSpectator = bool)
                                                .controller(BooleanControllerBuilder::create)
                                                .build()
                                )
                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.lucidity.option.wand"))
                                        .description(OptionDescription.of(Text.translatable("config.lucidity.description.wand")))
                                        .binding("minecraft:breeze_rod", () -> instance.instance().wand, s -> instance.instance().wand = s)
                                        .controller(opt -> StringControllerBuilder.create(opt))
                                        .build()
                                ).option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.lucidity.option.wand_apply"))
                                        .description(OptionDescription.of(Text.translatable("config.lucidity.description.wand_apply")))
                                        .binding(0, () -> instance.instance().wandApplyMode, v -> instance.instance().wandApplyMode = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, WandActionsManager.WandApplyToMode.values().length-1)
                                                .step(1)
                                                .formatValue(val -> Text.translatable(resolveWandMode(val))))
                                        .build()
                                ).option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.lucidity.render_mode.rendering_mode_block"))
                                        .description(OptionDescription.of(Text.translatable("config.lucidity.render_mode.rendering_mode_block")))
                                        .binding(0, () -> instance.instance().renderModeBlock, v -> instance.instance().renderModeBlock = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, RenderMode.values().length-1)
                                                .step(1)
                                                .formatValue(val -> Text.translatable(resolveSelectiveBlockRenderingMode(val))))
                                        .build()
                                )
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.lucidity.render_mode.rendering_mode_entity"))
                                        .description(OptionDescription.of(Text.translatable("config.lucidity.render_mode.rendering_mode_entity")))
                                        .binding(0, () -> instance.instance().renderModeEntity, v -> instance.instance().renderModeEntity = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, RenderMode.values().length-1)
                                                .step(1)
                                                .formatValue(val -> Text.translatable(resolveSelectiveEntityRenderingMode(val))))
                                        .build()
                                )
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.lucidity.render_mode.rendering_mode_particle"))
                                        .description(OptionDescription.of(Text.translatable("config.lucidity.render_mode.rendering_mode_particle")))
                                        .binding(0, () -> instance.instance().renderModeParticle, v -> instance.instance().renderModeParticle = v)
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
                                                        .binding(false, () -> instance.instance().enableWorldEaterHelper, bool -> instance.instance().enableWorldEaterHelper = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(Option.<Integer>createBuilder()
                                                .name(Text.translatable("config.lucidity.option.oreHighlightRange"))
                                                .binding(20, () -> instance.instance().hightLightRange, v -> instance.instance().hightLightRange = v)
                                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                        .range(0, 100)
                                                        .step(1)
                                                        .formatValue(val -> Text.of(val.toString())))
                                                .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.option.palette"))

                                                        .binding(Color.white, () -> instance.instance().colorPalette, color -> instance.instance().colorPalette = color)
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
                                                    if (instance.instance().selectedBlocksToHighLight == null) {
                                                        LucidityConfig.selectedBlocksToHighLight = new ArrayList<>();
                                                    }
                                                    return instance.instance().selectedBlocksToHighLight;
                                                },
                                                list -> instance.instance().selectedBlocksToHighLight = list
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .initial(String.format("#%06X", (instance.instance().colorPalette.getRGB() & 0xFFFFFF)))
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
                                                        .binding(false, () -> instance.instance().renderKeyPresses, bool -> instance.instance().renderKeyPresses = bool)
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
                                                        .binding(false, () -> instance.instance().enableDamageIndicator, bool -> instance.instance().enableDamageIndicator = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Integer>createBuilder()
                                                        .name(Text.translatable("config.lucidity.damageIndicator.offset"))
                                                        .description(OptionDescription.createBuilder()
                                                                .text(Text.translatable("config.lucidity.description.damageIndicator.offset"))
                                                                .build()
                                                        )
                                                        .binding(0, () -> instance.instance().indicatorOffset, integer -> instance.instance().indicatorOffset = integer)
                                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                                .range(0, 50)
                                                                .step(1)
                                                                .formatValue(val -> Text.of(val.toString())))
                                                        .build()
                                        ).option(
                                                Option.<Integer>createBuilder()
                                                        .name(Text.translatable("config.lucidity.damageIndicator.time"))

                                                        .binding(0, () -> instance.instance().damageIndicatorLifeTime, integer -> instance.instance().damageIndicatorLifeTime = integer)
                                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                                .range(50, 100)
                                                                .step(1)
                                                                .formatValue(val -> Text.of(val.toString())))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.damageIndicator.color"))

                                                        .binding(Color.red, () -> instance.instance().indicatorColor, color -> instance.instance().indicatorColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
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
                                                        .binding(false, () -> instance.instance().fluidSourceHighLight, bool -> instance.instance().fluidSourceHighLight = bool)
                                                        .controller(BooleanControllerBuilder::create)
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
                                                        .binding(false, () -> instance.instance().renderTrajectory, bool -> instance.instance().renderTrajectory = bool)
                                                        .controller(BooleanControllerBuilder::create)
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.mob_color"))

                                                        .binding(Color.orange, () -> instance.instance().mobTrajectoryColor, color -> instance.instance().mobTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.player_color"))

                                                        .binding(Color.green, () -> instance.instance().selfTrajectoryColor, color -> instance.instance().selfTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.self_color"))

                                                        .binding(Color.magenta, () -> instance.instance().playerTrajectoryColor, color -> instance.instance().playerTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.liner_projectiles_color"))

                                                        .binding(Color.red, () -> instance.instance().linerTrajectoryColor, color -> instance.instance().linerTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.far_projectiles_color"))

                                                        .binding(Color.yellow, () -> instance.instance().farTrajectoryColor, color -> instance.instance().farTrajectoryColor = color)
                                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                                .allowAlpha(true))
                                                        .build()
                                        ).option(
                                                Option.<Color>createBuilder()
                                                        .name(Text.translatable("config.lucidity.near_projectiles_color"))

                                                        .binding(Color.cyan, () -> instance.instance().nearTrajectoryColor, color -> instance.instance().nearTrajectoryColor = color)
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
                                //.description(OptionDescription.of(Text.literal("Main mod features")))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("config.description.main_features"))
                                        .image(Lucidity.id("textures/all-explosives.png"), 192, 108)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.main_render"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.main_render").formatted(Formatting.YELLOW))
                                                .image(Lucidity.id("textures/examples/main-render.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(true, () -> instance.instance().enableExplosionVisualizer, bool -> instance.instance().enableExplosionVisualizer = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.block_destruction"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.block_destruction"))
                                                .image(Lucidity.id("textures/examples/block-destruction.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> instance.instance().showBlockDestroyInfo, bool -> instance.instance().showBlockDestroyInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.block_detection_ray"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.block_detection_ray"))
                                                .image(Lucidity.id("textures/examples/ray.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> instance.instance().showExplosionBlockDamageRayInfo, bool -> instance.instance().showExplosionBlockDamageRayInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.sample_points"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.sample_points"))
                                                .image(Lucidity.id("textures/examples/sample-points.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> instance.instance().showRayCastInfo, bool -> instance.instance().showRayCastInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.explosion_damage"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("config.option.description.explosion_damage"))
                                                .image(Lucidity.id("textures/examples/damage.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> instance.instance().showDamageInfo, bool -> instance.instance().showDamageInfo = bool)
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
                                        .binding(0, () -> instance.instance().Xmin, v -> instance.instance().Xmin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.x_max"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.x_max")))
                                        .binding(15, () -> instance.instance().Xmax, v -> instance.instance().Xmax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.y_min"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.y_min")))
                                        .binding(0, () -> instance.instance().Ymin, v -> instance.instance().Ymin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.y_max"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.y_max")))
                                        .binding(15, () -> instance.instance().Ymax, v -> instance.instance().Ymax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.z_min"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.z_min")))
                                        .binding(0, () -> instance.instance().Zmin, v -> instance.instance().Zmin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 15)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.z_max"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.z_max")))
                                        .binding(15, () -> instance.instance().Zmax, v -> instance.instance().Zmax = v)
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
                                        .binding(false, () -> instance.instance().invert, bool -> instance.instance().invert = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.layer_min"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.layer_min")))
                                        .binding(0, () -> instance.instance().LayerMin, v -> instance.instance().LayerMin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 100)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.layer_max"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.layer_max")))
                                        .binding(100, () -> instance.instance().LayerMax, v -> instance.instance().LayerMax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 100)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.option.block_detection_ray_icon"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.block_detection_ray_icon")))
                                        .binding("⧈", () -> instance.instance().BlockDetectionRayIcon, v -> instance.instance().BlockDetectionRayIcon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build()
                                )

                                .option(Option.<Float>createBuilder()
                                        .name(Text.translatable("config.option.block_detection_ray_icon_size"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.block_detection_ray_icon_size")))
                                        .binding(0.005F, () -> instance.instance().BlockDetectionRayIconSize, v -> instance.instance().BlockDetectionRayIconSize = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0F, 0.1F)
                                                .step(0.005F)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.render_with_alpha"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.render_with_alpha")))
                                        .binding(false, () -> instance.instance().EnableAlpha, bool -> instance.instance().EnableAlpha = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.see_Throw"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.see_Throw")))
                                        .binding(false, () -> instance.instance().BlockDetectionRaySeeThrow, bool -> instance.instance().BlockDetectionRaySeeThrow = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.option.type"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.type")))
                                        .binding(0, () -> instance.instance().ColorType, v -> instance.instance().ColorType = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 2)
                                                .step(1)
                                                .formatValue(ExplosionSimulator::getColoringTypeForRays))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.+y"))
                                        .binding(Color.YELLOW, () -> instance.instance().Colored_UP, v -> instance.instance().Colored_UP = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.-y"))
                                        .binding(Color.GREEN, () -> instance.instance().Colored_DOWN, v -> instance.instance().Colored_DOWN = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.+x"))
                                        .binding(Color.RED, () -> instance.instance().Colored_FRONT, v -> instance.instance().Colored_FRONT = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.-x"))
                                        .binding(Color.BLUE, () -> instance.instance().Colored_BACK, v -> instance.instance().Colored_BACK = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.+z"))
                                        .binding(Color.MAGENTA, () -> instance.instance().Colored_LEFT, v -> instance.instance().Colored_LEFT = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.-z"))
                                        .binding(Color.WHITE, () -> instance.instance().Colored_RIGHT, v -> instance.instance().Colored_RIGHT = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.single_color"))
                                        .binding(Color.WHITE, () -> instance.instance().Single_Color, v -> instance.instance().Single_Color = v)
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
                                        .binding(Color.YELLOW, () -> instance.instance().BlockDestroyIconColor, v -> instance.instance().BlockDestroyIconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.option.block_destruction_render_icon"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.block_destruction_render_icon")))
                                        .binding("⨂", () -> instance.instance().BlockDestroyIcon, v -> instance.instance().BlockDestroyIcon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Text.translatable("config.option.block_destruction_render_icon_size"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.block_destruction_render_icon_size")))
                                        .binding(0.05F, () -> instance.instance().BlockDestroyIconSize, v -> instance.instance().BlockDestroyIconSize = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0F, 0.2F)
                                                .step(0.05F)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.see_Throw"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.see_Throw")))
                                        .binding(true, () -> instance.instance().BlockDestroyIconSeeThrow, bool -> instance.instance().BlockDestroyIconSeeThrow = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )
                                .build()
                        )

                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("config.option.entity_sample_point_render_settings"))
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_safe_color"))
                                        .binding(Color.GREEN, () -> instance.instance().EntitySamplePoion_Safe_IconColor, v -> instance.instance().EntitySamplePoion_Safe_IconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_safe_icon"))
                                        .binding("√", () -> instance.instance().EntitySamplePoion_Safe_Icon, v -> instance.instance().EntitySamplePoion_Safe_Icon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_danger_color"))
                                        .binding(Color.RED, () -> instance.instance().EntitySamplePoion_Danger_IconColor, v -> instance.instance().EntitySamplePoion_Danger_IconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_danger_icon"))
                                        .binding("X", () -> instance.instance().EntitySamplePoion_Danger_Icon, v -> instance.instance().EntitySamplePoion_Danger_Icon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_blocked_color"))
                                        .binding(Color.MAGENTA, () -> instance.instance().EntitySamplePoion_Blocked_IconColor, v -> instance.instance().EntitySamplePoion_Blocked_IconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build()
                                )
                                .option(Option.<String>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_blocked_icon"))
                                        .binding("❖", () -> instance.instance().EntitySamplePoion_Blocked_Icon, v -> instance.instance().EntitySamplePoion_Blocked_Icon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Text.translatable("config.option.entity_sample_point_icon_size"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.entity_sample_point_icon_size")))
                                        .binding(0.01F, () -> instance.instance().EntitySamplePoionIconSize, v -> instance.instance().EntitySamplePoionIconSize = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0F, 0.03F)
                                                .step(0.01F)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.option.see_Throw"))
                                        .description(OptionDescription.of(Text.translatable("config.option.description.see_Throw")))
                                        .binding(true, () -> instance.instance().EntitySamplePointSeeThrow, bool -> instance.instance().EntitySamplePointSeeThrow = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build()
                                )



                                .build()
                        )
                        .build())


                .save(() -> {
                    instance.save();
                    Lucidity.onConfigUpdated();
                })
                .build()
                .generateScreen(screen);
    }
}
