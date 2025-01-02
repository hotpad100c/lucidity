package mypals.ml;

import mypals.ml.config.LucidityConfig;
import mypals.ml.config.Keybinds;
import mypals.ml.features.renderKeyPresses.KeyPressesManager;
import mypals.ml.features.sonicBoomDetection.WardenStateResolver;
import mypals.ml.rendering.InformationRender;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mypals.ml.config.LucidityConfig.*;
import static mypals.ml.features.safeDigging.DiggingSituationResolver.*;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.*;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;
import static mypals.ml.features.selectiveRendering.WandActionsManager.*;
import static mypals.ml.features.worldEaterHelper.oreResolver.scanForMineralsOptimized;

public class Lucidity implements ModInitializer {
	public static final String MOD_ID = "lucidity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final SimpleOption<Double> GAMMA_BYPASS = new SimpleOption<>("options.gamma", SimpleOption.emptyTooltip(), (optionText, value) -> Text.empty(), SimpleOption.DoubleSliderCallbacks.INSTANCE.withModifier(
			d -> (double) 40, d -> 1
	), 40.0, value -> {
	});
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

    public static void onConfigUpdated() {
		MinecraftClient client = MinecraftClient.getInstance();
        LucidityConfig.CONFIG_HANDLER.instance();
		updateChunks(client);
		loadConfig();
		resolveSettings();
    }
	private static void resolveSettings(){
		resolveSelectedBlockTypesFromString(LucidityConfig.selectedBlockTypes);
		resolveSelectedEntityTypesFromString(LucidityConfig.selectedEntityTypes);
		resolveSelectedParticleTypesFromString(LucidityConfig.selectedParticleTypes);

		resolveSelectedAreasFromString(LucidityConfig.selectedAreasSaved);
		resolveSelectedWandFromString(LucidityConfig.wand);
		if(!!blockRenderMode.equals(RenderMode.OFF) && MinecraftClient.getInstance().player!=null)
			MinecraftClient.getInstance().player.
					removeStatusEffect(StatusEffects.NIGHT_VISION);
	}
	public static void updateChunks(MinecraftClient client){
		client.worldRenderer.reload();
	}
	public static void loadConfig(){
		var instance = LucidityConfig.CONFIG_HANDLER;
		instance.load();
		LucidityConfig.CONFIG_HANDLER.instance();
		resolveSettings();
	}

    @Override
	public void onInitialize() {
		loadConfig();

		Keybinds.initialize();
		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			KeyPressesManager.renderPressed(context);
			//WandTooltipRenderer.renderWandTooltip(context);
		});
		ClientTickEvents.END_CLIENT_TICK.register(client-> {
			InformationRender.clear();
			wandActions(MinecraftClient.getInstance());
			//resolveEntities(client,50);
			resolveEnviroment(client);
			UpdateTimers();
		});
		ClientTickEvents.END_WORLD_TICK.register(t->{
			extraActions();
		});
		AttackBlockCallback.EVENT.register((player, world, hand, pos,dir) -> {
			warningTime = WARNING_TIME;
			if (world.isClient && player.getStackInHand(hand).getItem() == wand && player.isCreative()) {
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});
	}
	private static void extraActions(){
		LucidityConfig.CONFIG_HANDLER.instance();
		if(!blockRenderMode.equals(RenderMode.OFF) && MinecraftClient.getInstance().player != null){
			MinecraftClient.getInstance().player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,10000,5,true,false,false));
        }
	}
	private static void resolveEntities(MinecraftClient client, double range){
		ClientWorld world = client.world;
		if(world == null){return;}
        assert client.player != null;
        Box searchBox = new Box(
				client.player.getX() - range, client.player.getY() - range, client.player.getZ() - range,
				client.player.getX() + range, client.player.getY() + range, client.player.getZ() + range
		);
		world.getEntitiesByClass(WardenEntity.class,searchBox,warden -> true)
				.forEach(WardenStateResolver::resolveWardenState);
		
	}
	private static void resolveEnviroment(MinecraftClient client){
		ClientWorld world = client.world;
		if(world == null){return;}
		assert client.player != null;
		PlayerEntity player = client.player;
		if(warningTime > 0){
			BlockHitResult blockBreakingRayCast = getPlayerLookedBlock(player,world);
			//resolveBreakingSituation(player,world,blockBreakingRayCast.getBlockPos());
		}
		if(enableWorldEaterHelper) {
			scanForMineralsOptimized(hightLightRange);
		}
	}
	public static void UpdateTimers(){
		warningTime = warningTime <= 0? 0 : warningTime-1;
		selectCoolDown = selectCoolDown <= 0? 0 : selectCoolDown-1;
	}
	public static BlockHitResult getPlayerLookedBlock(PlayerEntity player, World world) {
		Entity camera = MinecraftClient.getInstance().getCameraEntity();

		Vec3d start = camera.getCameraPosVec(1.0F);

		Vec3d end = start.add(camera.getRotationVec(1.0F).multiply(player.isCreative()?5:4));


		RaycastContext context = new RaycastContext(
				start,
				end,
				RaycastContext.ShapeType.OUTLINE,
				RaycastContext.FluidHandling.NONE,
				player
		);
		return world.raycast(context);
	}
}