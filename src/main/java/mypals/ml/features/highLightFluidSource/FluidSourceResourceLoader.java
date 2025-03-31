package mypals.ml.features.highLightFluidSource;

import mypals.ml.config.LucidityConfig;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static mypals.ml.Lucidity.MOD_ID;
import static net.minecraft.block.BubbleColumnBlock.DRAG;
import static net.minecraft.client.texture.SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;

public class FluidSourceResourceLoader implements SimpleSynchronousResourceReloadListener {
    public static final Sprite[] lavaSourceSpites = new Sprite[2];
    public static final Sprite[] defaultLavaSourceSpites = new Sprite[2];

    public static final Sprite[] waterSourceSpites = new Sprite[3];
    public static final Sprite[] bubbleWaterSpitesUp = new Sprite[3];
    public static final Sprite[] bubbleWaterSpitesDown = new Sprite[3];

    public static final Sprite[] defaultWaterSourceSpites = new Sprite[3];
    private static final Identifier LISTENER_ID = Identifier.of(MOD_ID,"reload_listener");
    private static final Identifier FLOWING_LAVA_SPRITE_ID = Identifier.of(MOD_ID,"block/lava_flow");
    private static final Identifier STILL_LAVA_SPRITE_ID = Identifier.of(MOD_ID,"block/lava_still");
    private static final Identifier FLOWING_WATER_SPRITE_ID = Identifier.of(MOD_ID,"block/water_flow");
    private static final Identifier STILL_WATER_SPRITE_ID = Identifier.of(MOD_ID,"block/water_still");

    private static final Identifier UP_BUBBLE_SPRITE_ID = Identifier.of(MOD_ID,"block/bubble_up");
    private static final Identifier DOWN_BUBBLE_SPRITE_ID = Identifier.of(MOD_ID,"block/bubble_down");
    public static Sprite lavaSourceFlowSprite;
    public static Sprite lavaSourceStillSprite;
    public static Sprite defaultLavaSourceFlowSprite;
    public static Sprite defaultLavaSourceStillSprite;

    public static Sprite waterSourceFlowSprite;
    public static Sprite waterSourceStillSprite;
    public static Sprite defaultWaterSourceFlowSprite;
    public static Sprite defaultWaterOverlaySprite;
    public static Sprite defaultWaterSourceStillSprite;

    public static Sprite bubbleUpSprite;
    public static Sprite bubbleDownSprite;

    public static void init() {
        //registerSprites();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new FluidSourceResourceLoader());
    }

    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        //final Function<Identifier, SpriteAtlasTexture> atlas = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        final Function<Identifier, Sprite> atlas = MinecraftClient.getInstance().getSpriteAtlas(BLOCK_ATLAS_TEXTURE);
        lavaSourceStillSprite = atlas.apply(STILL_LAVA_SPRITE_ID);
        lavaSourceFlowSprite = atlas.apply(FLOWING_LAVA_SPRITE_ID);
        lavaSourceSpites[0] = lavaSourceStillSprite;
        lavaSourceSpites[1] = lavaSourceFlowSprite;
        defaultLavaSourceStillSprite = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.LAVA.getDefaultState()).getParticleSprite();
        defaultLavaSourceFlowSprite = ModelBaker.LAVA_FLOW.getSprite();
        defaultLavaSourceSpites[0] = defaultLavaSourceStillSprite;
        defaultLavaSourceSpites[1] = defaultLavaSourceFlowSprite;
        LucidityConfig.CONFIG_HANDLER.instance();
        FluidRenderHandler lavaSourceRenderHandler = (view, pos, state) -> {
            if (view != null && pos != null && LucidityConfig.fluidSourceHighLight) {
                BlockState blockState = view.getBlockState(pos);
                if (blockState.contains(FluidBlock.LEVEL) && blockState.get(FluidBlock.LEVEL) == 0) {
                    return lavaSourceSpites;
                }
            }
            return defaultLavaSourceSpites;
        };
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.LAVA, lavaSourceRenderHandler);
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.FLOWING_LAVA, lavaSourceRenderHandler);
        defaultWaterOverlaySprite = ModelBaker.WATER_OVERLAY.getSprite();

        waterSourceStillSprite = atlas.apply(STILL_WATER_SPRITE_ID);
        waterSourceFlowSprite = atlas.apply(FLOWING_WATER_SPRITE_ID);
        waterSourceSpites[0] = waterSourceStillSprite;
        waterSourceSpites[1] = waterSourceFlowSprite;
        waterSourceSpites[2] = defaultWaterOverlaySprite;

        bubbleDownSprite = atlas.apply(DOWN_BUBBLE_SPRITE_ID);
        bubbleUpSprite = atlas.apply(UP_BUBBLE_SPRITE_ID);
        bubbleWaterSpitesDown[0] = waterSourceStillSprite;
        bubbleWaterSpitesDown[1] = bubbleDownSprite;
        bubbleWaterSpitesDown[2] = defaultWaterOverlaySprite;

        bubbleWaterSpitesUp[0] = waterSourceStillSprite;
        bubbleWaterSpitesUp[1] = bubbleUpSprite;
        bubbleWaterSpitesUp[2] = defaultWaterOverlaySprite;

        defaultWaterSourceStillSprite = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();
        defaultWaterSourceFlowSprite = ModelBaker.WATER_FLOW.getSprite();

        defaultWaterSourceSpites[0] = defaultWaterSourceStillSprite;
        defaultWaterSourceSpites[1] = defaultWaterSourceFlowSprite;
        defaultWaterSourceSpites[2] = defaultWaterOverlaySprite;

        LucidityConfig.CONFIG_HANDLER.instance();
        FluidRenderHandler waterSourceRenderHandler = new FluidRenderHandler() {
            @Override
            public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
                if (view != null && pos != null && LucidityConfig.fluidSourceHighLight) {
                    BlockState blockState = view.getBlockState(pos);
                    if (blockState.isOf(Blocks.BUBBLE_COLUMN) && blockState.get(DRAG)  && view.getFluidState(pos).isStill()) {
                        return bubbleWaterSpitesDown;
                    }
                    if (blockState.isOf(Blocks.BUBBLE_COLUMN) && !blockState.get(DRAG)  && view.getFluidState(pos).isStill()) {
                        return bubbleWaterSpitesUp;
                    }
                    if (view.getFluidState(pos).isStill()) {
                        return waterSourceSpites;
                    }
                }
                return defaultWaterSourceSpites;
            }
            @Override
            public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
                return BiomeColors.getWaterColor(view == null?MinecraftClient.getInstance().world : view, pos == null?new BlockPos(0,0,0):pos);
            }
        };
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.WATER, waterSourceRenderHandler);
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.FLOWING_WATER, waterSourceRenderHandler);

    }

}
