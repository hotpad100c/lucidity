package mypals.ml.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import mypals.ml.Lucidity;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LucidityConfig {
    public static ConfigClassHandler<LucidityConfig> CONFIG_HANDLER = ConfigClassHandler.createBuilder(LucidityConfig.class)
            .id(Lucidity.id("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("LucidityConfig.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();
    @SerialEntry
    public static List<String> selectedBlockTypes = new ArrayList<>();
    @SerialEntry
    public static List<String> selectedEntityTypes = new ArrayList<>();
    @SerialEntry
    public static List<String> selectedParticleTypes = new ArrayList<>();
    @SerialEntry
    public static List<String> selectedAreasSaved = new ArrayList<>();
    @SerialEntry
    public static Boolean selectInSpectator = true;
    /*@SerialEntry
    public static Boolean enableSelectiveBlockRender = false;
    @SerialEntry
    public static Boolean enableSelectiveEntityRender = false;
    @SerialEntry
    public static Boolean enableSelectiveParticleRender = false;
    @SerialEntry*/
    public static String wand = "minecraft:breeze_rod";
    @SerialEntry
    public static int renderModeBlock = 0;
    @SerialEntry
    public static int renderModeEntity = 0;
    @SerialEntry
    public static int renderModeParticle = 0;
    @SerialEntry
    public static int wandApplyMode = 0;


    @SerialEntry
    public static Boolean enableWorldEaterHelper = false;
    @SerialEntry
    public static int hightLightRange = 30;

    @SerialEntry
    public static boolean renderKeyPresses = false;

    @SerialEntry
    public static boolean enableDamageIndicator = false;
    @SerialEntry
    public static int damageIndicatorLifeTime = 50;
    @SerialEntry
    public static int indicatorOffset = 0;
    @SerialEntry
    public static Color indicatorColor = Color.red;

}
