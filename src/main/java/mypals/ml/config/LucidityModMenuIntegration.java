package mypals.ml.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import mypals.ml.Lucidity;
import mypals.ml.features.selectiveRendering.WandActionsManager;
import net.minecraft.text.Text;

import java.util.ArrayList;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.*;
import static mypals.ml.features.selectiveRendering.WandActionsManager.resolveSelectiveWandMode;

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
                                                .range(0, WandActionsManager.WandApplyToMode.values().length)
                                                .step(1)
                                                .formatValue(val -> Text.translatable(resolveSelectiveWandMode(val))))
                                        .build()
                                ).option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.lucidity.render_mode.rendering_mode_block"))
                                        .description(OptionDescription.of(Text.translatable("config.lucidity.render_mode.rendering_mode_block")))
                                        .binding(0, () -> instance.instance().renderModeBlock, v -> instance.instance().renderModeBlock = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, RenderMode.values().length)
                                                .step(1)
                                                .formatValue(val -> Text.translatable(resolveSelectiveBlockRenderingMode(val))))
                                        .build()
                                )
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.lucidity.render_mode.rendering_mode_entity"))
                                        .description(OptionDescription.of(Text.translatable("config.lucidity.render_mode.rendering_mode_entity")))
                                        .binding(0, () -> instance.instance().renderModeEntity, v -> instance.instance().renderModeEntity = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, RenderMode.values().length)
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
                                        )
                                        .build()
                                )

                                .build()
                )
                .save(() -> {
                    instance.save();
                    Lucidity.onConfigUpdated();
                })
                .build()
                .generateScreen(screen);
    }
}
