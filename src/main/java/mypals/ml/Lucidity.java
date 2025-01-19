package mypals.ml;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.config.LucidityConfig;
import mypals.ml.config.Keybinds;
import mypals.ml.features.damageIndicator.DamageHandler;
import mypals.ml.features.damageIndicator.IndicatorRenderer;
import mypals.ml.features.explosionManage.ExplosionData;
import mypals.ml.features.explosionManage.ExplosiveObjectFinder;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.DamagedEntityData.EntityToDamage;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.ExplosionAffectedObjects;
import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import mypals.ml.features.explosionManage.FakeExplosion;
import mypals.ml.features.renderKeyPresses.KeyPressesManager;
import mypals.ml.features.renderer.InfoRenderer;
import mypals.ml.features.selectiveRendering.WandTooltipRenderer;
import mypals.ml.features.sonicBoomDetection.WardenStateResolver;
import mypals.ml.rendering.InformationRender;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
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
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static mypals.ml.config.LucidityConfig.*;
import static mypals.ml.features.explosionManage.ExplosionSimulateManager.*;
import static mypals.ml.features.renderer.InfoRenderer.render;
import static mypals.ml.features.safeDigging.DiggingSituationResolver.*;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.*;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;
import static mypals.ml.features.selectiveRendering.WandActionsManager.*;
import static mypals.ml.features.worldEaterHelper.oreResolver.scanForMineralsOptimized;

public class Lucidity implements ModInitializer {
	public static Set<BlockPos> blocksToDestroy = new HashSet<>();
	public static Set<Vec3d> explosionCenters = new HashSet<>();
	public static Set<EntityToDamage> entitysToDamage = new HashSet<>();

	public static  Set<SamplePointData> samplePointDatas = new HashSet<>();

	public static Set<ExplosionCastLine> explosionCastedLines = new HashSet<>();

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
		resolveSelectedBlockStatesFromString(LucidityConfig.selectedBlockTypes);
		resolveSelectedEntityTypesFromString(LucidityConfig.selectedEntityTypes);
		resolveSelectedParticleTypesFromString(LucidityConfig.selectedParticleTypes);

		resolveSelectedAreasFromString(LucidityConfig.selectedAreasSaved);
		resolveSelectedWandFromString(LucidityConfig.wand);
		if(blockRenderMode.equals(RenderMode.OFF) && MinecraftClient.getInstance().player!=null)
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
	    WorldRenderEvents.START.register((WorldRenderContext context) -> {

		    if(showInfo) {
			    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
			    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			    RenderSystem.depthMask(false);
			    RenderSystem.enableBlend();
			    RenderSystem.defaultBlendFunc();
			    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			    BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
			    render(context.matrixStack(), context.tickCounter(), buffer);
			    RenderSystem.applyModelViewMatrix();
			    RenderSystem.setShaderColor(1, 1, 1, 1);

			    RenderSystem.disableBlend();
		    }
	    });
	    ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	    	if(MinecraftClient.getInstance() != null && MinecraftClient.getInstance().getWindow() != null) {
		    if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
			if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_E)) {
				setOnOff(!showInfo);
				assert MinecraftClient.getInstance().player != null;
				MinecraftClient.getInstance().player.sendMessage(Text.of("Explosive object render now set to " + showInfo), false);
				KeyBinding.unpressAll();
			}
		    }
	    	}
		loadConfig();

		Keybinds.initialize();
		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			KeyPressesManager.renderPressed(context);
			WandTooltipRenderer.renderWandTooltip(context);
			IndicatorRenderer.renderIndicators(context);
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
		if(enableDamageIndicator){
			DamageHandler.PlayerHealthMonitor();
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
	public static void setOnOff(boolean toggle) {
		LucidityConfig.CONFIG_HANDLER.instance().showInfo = toggle;
		if(toggle)
			LucidityConfig.CONFIG_HANDLER.instance().showBlockDestroyInfo = true;
		LucidityConfig.CONFIG_HANDLER.save();
	}
	private void onClientTick(MinecraftClient client) {
		FixRangeIssue();
		assert MinecraftClient.getInstance() != null;
		//createGlowingBlockDisplay(MinecraftClient.getInstance().world, new BlockPos(0, 0, 0));
		if (showInfo) {
			try {
				explosionCastedLines.clear();
				blocksToDestroy.clear();
				entitysToDamage.clear();
				explosionCenters.clear();
				samplePointDatas.clear();
				if (client.world != null && client.player != null) {
					World world = client.world;
					BlockPos playerPos = client.player.getBlockPos();


					List<ExplosionData> exBlockPos = ExplosiveObjectFinder.findExplosiveBlocksInRange(world, playerPos);
					List<ExplosionData> exEntityPos = ExplosiveObjectFinder.findCrystlesInRange(world, playerPos);
					for (ExplosionData explotion : exBlockPos) {
						Vec3d p_d = new Vec3d(explotion.getPosition().toVector3f());
						Vec3i p_i = new Vec3i((int) p_d.x, (int) p_d.y, (int) p_d.z);
						ExplosionAffectedObjects EAO = simulateExplosiveBlocks(world, new BlockPos(p_i), explotion.getStrength());
						explosionCastedLines.addAll(EAO.getExplotionCastedLines());
						blocksToDestroy.addAll(EAO.getBlocksToDestriy());
						entitysToDamage.addAll(EAO.getEntitysToDamage());
						explosionCenters.addAll(EAO.getExplotionCenters());
						samplePointDatas.addAll(EAO.getSamplePointData());

					}
					for (ExplosionData explosion : exEntityPos) {
						ExplosionAffectedObjects EAO = simulateExplosiveEntitys(world, explosion.getPosition(), explosion.getStrength());
						explosionCastedLines.addAll(EAO.getExplotionCastedLines());
						blocksToDestroy.addAll(EAO.getBlocksToDestriy());
						entitysToDamage.addAll(EAO.getEntitysToDamage());
						explosionCenters.addAll(EAO.getExplotionCenters());
						samplePointDatas.addAll(EAO.getSamplePointData());
					}
					for (FakeExplosion fe : fakeExplosions) {
						ExplosionAffectedObjects EAO = simulateFakeExplosions(world, new Vec3d(fe.x, fe.y, fe.z), fe.power, fe.ignorBlockInside);
						explosionCastedLines.addAll(EAO.getExplotionCastedLines());
						blocksToDestroy.addAll(EAO.getBlocksToDestriy());
						entitysToDamage.addAll(EAO.getEntitysToDamage());
						explosionCenters.addAll(EAO.getExplotionCenters());
						samplePointDatas.addAll(EAO.getSamplePointData());
					}
					InfoRenderer.setCastedLines(explosionCastedLines);
					InfoRenderer.setBlocksToDamage(blocksToDestroy);
					InfoRenderer.setEntitysToDamage(entitysToDamage);
					InfoRenderer.setExplotionCenters(explosionCenters);
					InfoRenderer.setSamplePointData(samplePointDatas);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void FixRangeIssue()
	{
		if(LucidityConfig.CONFIG_HANDLER.instance().Xmax < LucidityConfig.CONFIG_HANDLER.instance().Xmin)
		{
			LucidityConfig.CONFIG_HANDLER.instance().Xmax = LucidityConfig.CONFIG_HANDLER.instance().Xmin;
			LucidityConfig.CONFIG_HANDLER.save();
		}
		if(LucidityConfig.CONFIG_HANDLER.instance().Ymax < LucidityConfig.CONFIG_HANDLER.instance().Ymin)
		{
			LucidityConfig.CONFIG_HANDLER.instance().Ymax = LucidityConfig.CONFIG_HANDLER.instance().Ymin;
			LucidityConfig.CONFIG_HANDLER.save();
		}
		if(LucidityConfig.CONFIG_HANDLER.instance().Zmax < LucidityConfig.CONFIG_HANDLER.instance().Zmin)
		{
			LucidityConfig.CONFIG_HANDLER.instance().Zmax = LucidityConfig.CONFIG_HANDLER.instance().Zmin;
			LucidityConfig.CONFIG_HANDLER.save();
		}
		if(LucidityConfig.CONFIG_HANDLER.instance().LayerMax < LucidityConfig.CONFIG_HANDLER.instance().LayerMin)
		{
			LucidityConfig.CONFIG_HANDLER.instance().LayerMax = LucidityConfig.CONFIG_HANDLER.instance().LayerMin + 1;
			LucidityConfig.CONFIG_HANDLER.save();
		}
	}
}