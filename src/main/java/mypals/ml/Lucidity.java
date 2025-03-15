package mypals.ml;

import mypals.ml.config.LucidityConfig;
import mypals.ml.config.Keybinds;
import mypals.ml.config.ScreenGenerator;
import mypals.ml.features.ImageRendering.ImageDataParser;
import mypals.ml.features.ImageRendering.configuration.ImageConfigCommands;
import mypals.ml.features.advancedAdvancedTooltip.AdvancedAdvancedToolTip;
import mypals.ml.features.arrowCamera.ArrowCamera;
import mypals.ml.features.betterBarrier.BetterBarrier;
import mypals.ml.features.blockOutline.OutlineManager;
import mypals.ml.features.commandHelper.ChatCommandScreenObserver;
import mypals.ml.features.damageIndicator.DamageHandler;
import mypals.ml.features.damageIndicator.IndicatorRenderer;
import mypals.ml.features.explosionVisualizer.ExplosionVisualizer;
import mypals.ml.features.highLightFluidSource.FluidSourceResourceLoader;
import mypals.ml.features.mobFollowRange.MobFollowRangeScanner;
import mypals.ml.features.pastBlockEvents.ClientsideBlockEventManager;
import mypals.ml.features.renderKeyPresses.KeyPressesManager;
import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import mypals.ml.features.selectiveRendering.WandTooltipRenderer;
import mypals.ml.features.sonicBoomDetection.WardenStateResolver;
import mypals.ml.features.soundListener3D.SoundListener;
import mypals.ml.features.trajectory.TrajectoryManager;
import mypals.ml.features.OreFinder.OreResolver;
import mypals.ml.rendering.InformationRender;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Predicate;

import static mypals.ml.command.CommandRegister.registerCommands;
import static mypals.ml.config.Keybinds.openConfigKey;
import static mypals.ml.config.Keybinds.openImageRenderingsConfigKey;
import static mypals.ml.config.LucidityConfig.*;
import static mypals.ml.features.ImageRendering.ImageDataParser.prepareImages;
import static mypals.ml.features.ImageRendering.configuration.ImageConfigCommands.openImageRenderingConfigGUI;
import static mypals.ml.features.renderMobSpawn.SpaceScanner.addSpawnDataToInformationRenderer;
import static mypals.ml.features.safeDigging.DiggingSituationResolver.*;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.*;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;
import static mypals.ml.features.selectiveRendering.WandActionsManager.*;
import static mypals.ml.features.OreFinder.MineralFinder.iterateBlocksWithinDistance;
import static mypals.ml.features.OreFinder.MineralFinder.parseSelectedBlocks;

public class Lucidity implements ModInitializer {
	private static SoundListener soundListener  = new SoundListener();
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
		updateChunks(client);
		if(!enableWorldEaterHelper) {
			OreResolver.recordedOres.clear();
		}else{
			iterateBlocksWithinDistance(MinecraftClient.getInstance().player.getBlockPos(), oreHighlightRange);
		}
		try {
			updateConfig();
		}catch (Exception e){
			LOGGER.error(e.toString());
			e.printStackTrace();
		}

    }
	private static void resolveSettings(){
		resolveSelectedBlockStatesFromString(LucidityConfig.selectedBlockTypes);
		resolveSelectedEntityTypesFromString(LucidityConfig.selectedEntityTypes);
		resolveSelectedParticleTypesFromString(LucidityConfig.selectedParticleTypes);
		parseSelectedBlocks();
		prepareImages();
		resolveSelectedAreasFromString(LucidityConfig.selectedAreasSaved);
		resolveSelectedWandFromString(LucidityConfig.wand);
		if(blockRenderMode.equals(RenderMode.OFF) && MinecraftClient.getInstance().player!=null)
			MinecraftClient.getInstance().player.
					removeStatusEffect(StatusEffects.NIGHT_VISION);
	}
	public static void updateChunks(MinecraftClient client){
		client.worldRenderer.reload();
	}
	public static void updateConfig() {
		var instance = LucidityConfig.CONFIG_HANDLER;
		instance.load();
		ExplosionVisualizer.FixRangeIssue();
		LucidityConfig.CONFIG_HANDLER.instance();
		resolveSettings();
	}

    @Override
	public void onInitialize() {
		BetterBarrier.init();
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
			ResourceManagerHelper.registerBuiltinResourcePack(
					Identifier.of(MOD_ID, "lavahighlight"),
					container,
					ResourcePackActivationType.NORMAL
			);
		});
		ImageConfigCommands.register();
		registerCommands();
		TrajectoryManager.init();
		new SoundListener();
		new ArrowCamera();
		ArrowCamera.onInitialize();
		AdvancedAdvancedToolTip.onInitialize();
		OutlineManager.targetedBlocks.add(new BlockPos(0,0,0));
		OutlineManager.targetedBlocks.add(new BlockPos(5,0,0));
		OutlineManager.targetedBlocks.add(new BlockPos(-5,0,0));
		OutlineManager.targetedBlocks.add(new BlockPos(0,0,-5));
		OutlineManager.targetedBlocks.add(new BlockPos(0,0,5));
		OutlineManager.targetedBlocks.add(new BlockPos(0,-5,0));
		OutlineManager.targetedBlocks.add(new BlockPos(0,5,0));

		SelectiveRenderingManager.resolveSelectiveBlockRenderingMode(renderModeBlock);
		SelectiveRenderingManager.resolveSelectiveEntityRenderingMode(renderModeEntity);
		SelectiveRenderingManager.resolveSelectiveParticleRenderingMode(renderModeParticle);
        FluidSourceResourceLoader.init();
		Keybinds.init();
		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			KeyPressesManager.renderPressed(context);
			WandTooltipRenderer.renderWandTooltip(context);
			IndicatorRenderer.renderIndicators(context);
		});
		WorldRenderEvents.AFTER_ENTITIES.register((context) ->{
			//OutlineManager.init();
			OutlineManager.resolveBlocks();
			OutlineManager.init();
			OutlineManager.onRenderWorldLast(context);

		});
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			updateConfig();
			//OutlineManager.init();
		});

		ClientTickEvents.END_CLIENT_TICK.register(client-> {
			InformationRender.clear();
			wandActions(client);
			resolveEnviroment(client);
			resolveEntities(client,50);
			UpdateTimers();
			if (openConfigKey.wasPressed()) {
				openConfigScreen(client);
			}
			if (openImageRenderingsConfigKey.wasPressed()) {
				openImageRenderingConfigGUI("image renderings");
			}

		});
		ClientTickEvents.END_WORLD_TICK.register(t->{
			extraActions();
		});
		UseBlockCallback.EVENT.register((player, world, hand, pos) -> {
			warningTime = WARNING_TIME;
			if (world.isClient && player.getStackInHand(hand).getItem() == wand && player.isCreative()) {
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});
		AttackBlockCallback.EVENT.register((player, world, hand, pos,dir) -> {
			warningTime = WARNING_TIME;
			if (world.isClient && player.getStackInHand(hand).getItem() == wand && player.isCreative()) {
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});
	}
	private void openConfigScreen(MinecraftClient client) {
		client.execute(() -> {
			if (client.currentScreen == null) {
				client.setScreen(ScreenGenerator.getConfigScreen(client.currentScreen));
			}
		});
	}
	private static void extraActions(){
		LucidityConfig.CONFIG_HANDLER.instance();
		if(!blockRenderMode.equals(RenderMode.OFF) && MinecraftClient.getInstance().player != null && autoNightVision){
			MinecraftClient.getInstance().player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,10000,5,true,false,false));
        }
	}
	private static void resolveEntities(MinecraftClient client, int scanRadius){
		ClientWorld world = client.world;
		if(world == null){return;}
        assert client.player != null;
		if(renderWardenAttackRange) {
			Box searchBox = new Box(
					client.player.getX() - scanRadius, client.player.getY() - scanRadius, client.player.getZ() - scanRadius,
					client.player.getX() + scanRadius, client.player.getY() + scanRadius, client.player.getZ() + scanRadius
			);
			world.getEntitiesByClass(WardenEntity.class, searchBox, warden -> true)
					.forEach(WardenStateResolver::resolveWardenState);
		}

		if(renderMobChaseRange || renderMobEyeLineConnection){
			MobFollowRangeScanner.onClientTick(scanRadius);
		}
	}
	private static void resolveEnviroment(MinecraftClient client){
		ClientWorld world = client.world;
		if(world == null){return;}
		assert client.player != null;
		PlayerEntity player = client.player;
		if(betterLight || betterBarrier || betterStructureVoid) {
			BetterBarrier.checkForBetterRenderersEnabled();
		}
		if(warningTime > 0){
			BlockHitResult blockBreakingRayCast = getPlayerLookedBlock(player,world);
			//resolveBreakingSituation(player,world,blockBreakingRayCast.getBlockPos());
		}
		if(enableDamageIndicator){
			DamageHandler.PlayerHealthMonitor();
		}
		if(renderTrajectory){
			TrajectoryManager.onClientTick(client);
		}
		if(renderMobSpawn){
			addSpawnDataToInformationRenderer();
		}
		if(enableExplosionVisualizer){
			ExplosionVisualizer.tick(client);
		}
		if(enableWorldEaterHelper) {
			OreResolver.onClientTick();
		}
		if(commandHelper){
			ChatCommandScreenObserver.onClientTick();
		}
		if(arrowcam){
			ArrowCamera.onClientTick();
		}
		soundListener.onClientTick();
		if(renderBlockEvents){
			ClientsideBlockEventManager.onClientTick();
		}
		ImageDataParser.onClientTick();
		//TreeSearchManager.onClientTick();
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
	public static EntityHitResult getPlayerLookedEntity(PlayerEntity player, World world) {
		Entity camera = MinecraftClient.getInstance().getCameraEntity();

		Vec3d start = camera.getCameraPosVec(1.0F);

		Vec3d end = start.add(camera.getRotationVec(1.0F).multiply(player.isCreative()?5:4));

		Box box = player.getBoundingBox().stretch(end.subtract(start)).expand(1.0);
		EntityHitResult result = getEntityCollision(
				world,
				player,
				start,
				end,
				box,
				entity -> entity != player, // 忽略玩家自身
				0.5F
		);

		return result;
	}
	public static EntityHitResult getEntityCollision(
			World world,
			Entity entity,
			Vec3d min,
			Vec3d max,
			Box box,
			Predicate<Entity> predicate,
			float margin
	) {
		double closestDistance = Double.MAX_VALUE;
		Entity closestEntity = null;

		for (Entity target : world.getOtherEntities(entity, box, predicate)) {
			// 扩展包围盒
			Box expandedBox = target.getBoundingBox().expand(margin);

			// 检测射线与包围盒的交点
			Optional<Vec3d> hitPos = expandedBox.raycast(min, max);
			if (hitPos.isPresent()) {
				double distance = min.squaredDistanceTo(hitPos.get());
				if (distance < closestDistance) {
					closestEntity = target;
					closestDistance = distance;
				}
			}
		}

		// 返回最近的命中实体结果
		return closestEntity == null ? null : new EntityHitResult(closestEntity);
	}

}