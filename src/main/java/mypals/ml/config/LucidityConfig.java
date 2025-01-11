package mypals.ml.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import mypals.ml.Lucidity;
import mypals.ml.features.explosionManage.FakeExplosion;
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

    //Explosion
    @SerialEntry
    public static boolean showInfo = false;
    @SerialEntry
    public static boolean showRayCastInfo = false;
    @SerialEntry
    public static boolean showBlockDestroyInfo = false;
    @SerialEntry
    public static boolean showDamageInfo = false;
    @SerialEntry
    public static boolean showExplosionBlockDamageRayInfo = false;

    @SerialEntry
    public static String BlockDetectionRayIcon = "⧈";
    @SerialEntry
    public static float BlockDetectionRayIconSize = 0.005F;
    @SerialEntry
    public static boolean BlockDetectionRaySeeThrow = false;
    @SerialEntry
    public static boolean EnableAlpha = true;
    @SerialEntry
    public static int Xmin = 0, Ymin = 0, Zmin = 0;
    @SerialEntry
    public static int Xmax = 16, Ymax = 16, Zmax = 16;
    @SerialEntry
    public static int LayerMin = 0, LayerMax = 100;
    @SerialEntry
    public static boolean Invert = false;


    @SerialEntry
    public static Color BlockDestroyIconColor = Color.yellow;
    @SerialEntry
    public static String BlockDestroyIcon = "!";
    @SerialEntry
    public static float BlockDestroyIconSize = 0.045F;
    @SerialEntry
    public static boolean BlockDestroyIconSeeThrow = true;


    @SerialEntry
    public static Color EntitySamplePoion_Safe_IconColor = Color.green;

    @SerialEntry
    public static Color EntitySamplePoion_Danger_IconColor = Color.red;

    @SerialEntry
    public static Color EntitySamplePoion_Blocked_IconColor = Color.MAGENTA;
    @SerialEntry
    public static boolean EntitySamplePointSeeThrow = true;

    @SerialEntry
    public static String EntitySamplePoion_Safe_Icon = "√";
    @SerialEntry
    public static String EntitySamplePoion_Danger_Icon = "X";
    @SerialEntry
    public static String EntitySamplePoion_Blocked_Icon = "❖";
    @SerialEntry
    public static float EntitySamplePoionIconSize = 0.01F;

    @SerialEntry
    public static ArrayList<FakeExplosion> fakeExplosions = new ArrayList<FakeExplosion>();

}
