package mypals.ml.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import mypals.ml.Lucidity;
import mypals.ml.features.explosionVisualizer.simulate.ExplosionSimulator;
import mypals.ml.features.selectiveRendering.WandActionsManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.ArrayList;

import static mypals.ml.config.ScreenGenerator.getConfigScreen;
import static mypals.ml.features.advancedAdvancedTooltip.AdvancedAdvancedToolTip.DEFAULT_TOOLTIP_OPTIONS;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.*;
import static mypals.ml.features.selectiveRendering.WandActionsManager.resolveWandMode;
import static mypals.ml.features.worldEaterHelper.MineralFinder.DEFAULT_SELECTED;

public class LucidityModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {

        return ScreenGenerator::getConfigScreen;
    }
}
