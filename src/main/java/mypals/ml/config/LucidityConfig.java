package mypals.ml.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import mypals.ml.Lucidity;
import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.FakeExplosion;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
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

    //selective renderings
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
    @SerialEntry
    public static String wand = "minecraft:breeze_rod";
    @SerialEntry
    public static int renderModeBlock = 0;
    @SerialEntry
    public static int renderModeEntity = 0;
    @SerialEntry
    public static int renderModeParticle = 0;
    @SerialEntry
    public static int wandApplyMode = 0;

    //world eater helper
    @SerialEntry
    public static Boolean enableWorldEaterHelper = false;
    @SerialEntry
    public static int hightLightRange = 30;

    @SerialEntry
    public static boolean renderKeyPresses = false;
    public static List<String> selectedBlocksToHighLight = Arrays.asList(
            "minecraft:coal_ore;#000000",
            "minecraft:iron_ore;#D8D8D8",
            "minecraft:gold_ore;#FFD700",
            "minecraft:diamond_ore;#00FFFF",
            "minecraft:emerald_ore;#009900",
            "minecraft:redstone_ore;#FF0000",
            "minecraft:lapis_ore;#0000FF",
            "minecraft:copper_ore;#FFA500",
            "minecraft:nether_quartz_ore;#FFF0DC",
            "minecraft:nether_gold_ore;#FFA500",
            "minecraft:ancient_debris;#804020",
            "minecraft:obsidian;#200040",
            "minecraft:deepslate_coal_ore;#101010",
            "minecraft:deepslate_iron_ore;#BFBFBF",
            "minecraft:deepslate_gold_ore;#FFD700",
            "minecraft:deepslate_diamond_ore;#00CCCC",
            "minecraft:deepslate_emerald_ore;#009900",
            "minecraft:deepslate_redstone_ore;#990000",
            "minecraft:deepslate_lapis_ore;#000099",
            "minecraft:deepslate_copper_ore;#FFA500"
    );
    @SerialEntry
    public static Color colorPalette = Color.white;

    //indicator
    @SerialEntry
    public static boolean enableDamageIndicator = false;
    @SerialEntry
    public static int damageIndicatorLifeTime = 50;
    @SerialEntry
    public static int indicatorOffset = 0;
    @SerialEntry
    public static Color indicatorColor = Color.red;

    //fluid
    @SerialEntry
    public static boolean fluidSourceHighLight = true;

    //trajectory
    @SerialEntry
    public static boolean renderTrajectory = false;
    @SerialEntry
    public static Color mobTrajectoryColor = Color.orange;
    @SerialEntry
    public static Color selfTrajectoryColor = Color.green;
    @SerialEntry
    public static Color playerTrajectoryColor = Color.magenta;
    @SerialEntry
    public static Color linerTrajectoryColor = Color.red;
    @SerialEntry
    public static Color farTrajectoryColor = Color.yellow;
    @SerialEntry
    public static Color nearTrajectoryColor = Color.cyan;

    //gui in portals
    @SerialEntry
    public static boolean guiInPortals = true;

    //explosion visualizer
    @SerialEntry
    public static boolean enableExplosionVisualizer = false;
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
    public static boolean invert = false;
    @SerialEntry
    public static int ColorType = 0;
    @SerialEntry
    public static Color Colored_UP = Color.yellow;
    @SerialEntry
    public static Color Colored_DOWN = Color.green;
    @SerialEntry
    public static Color Colored_FRONT= Color.red;
    @SerialEntry
    public static Color Colored_BACK = Color.cyan;
    @SerialEntry
    public static Color Colored_LEFT= Color.magenta;
    @SerialEntry
    public static Color Colored_RIGHT = Color.white;
    @SerialEntry
    public static Color Single_Color = Color.lightGray;

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

    //mob spawn
    @SerialEntry
    public static boolean renderMobSpawn = false;
    @SerialEntry
    public static int renderMobSpawnRange = 2;
    @SerialEntry
    public static float renderMobSpawnSize = 0.0025f;

    @SerialEntry
    public static boolean renderMobChaseRange = false;
    @SerialEntry
    public static boolean renderMobEyeLineConnection = false;
    @SerialEntry
    public static boolean renderWardenAttackRange = false;

    @SerialEntry
    public static boolean commandHelper = false;

}
