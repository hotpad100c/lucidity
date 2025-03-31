package mypals.ml.features.arrowCamera;

import mypals.ml.Lucidity;
import mypals.ml.features.arrowCamera.ArrowCameraEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ArrowCamera {

	public static ArrowCamera instance;
	public static final Identifier ARROW_CAMERA_ID = Identifier.of(Lucidity.MOD_ID, "arrow-camera");
	public static final RegistryKey<EntityType<?>> ARROW_CAMERA_KEY = RegistryKey.of(Registries.ENTITY_TYPE.getKey(), ARROW_CAMERA_ID);

	public static final EntityType<ArrowCameraEntity> ARROW_CAMERA_ENTITY = Registry.register(
			Registries.ENTITY_TYPE,
			ARROW_CAMERA_ID,
			EntityType.Builder.<ArrowCameraEntity>create(ArrowCameraEntity::new, SpawnGroup.CREATURE).dimensions(0f, 0f).build(ARROW_CAMERA_KEY)
	);

	private static ArrowCameraEntity camera;
	private static boolean hideGUI;
	public static int fovSetting;
	public static int thirdPersonView;
	private static final Set<Entity> trackedEntities = new HashSet<>();
	public ArrowCamera() {
		instance = this;  // ✅ 在构造函数中赋值
	}
	public static void onClientTick(){
		MinecraftClient client = MinecraftClient.getInstance();
		ClientWorld world = client.world;

		if(client.options.sneakKey.wasPressed() && camera != null) {
			stopArrowCam();
		}
		if (world != null && client.player != null) {
			for (Entity entity : world.getEntities()) {
				if (entity instanceof ProjectileEntity arrow && !trackedEntities.contains(entity)) {
					if (client.options.sprintKey.isPressed() && arrow.getOwner() == client.player) {
						startArrowCam(arrow);
					}
					trackedEntities.add(entity);
				}
			}
		}
		if (camera != null && camera.isRemoved()) {
			stopArrowCam();
		}

	}
	public static void onInitialize() {
		EntityRendererRegistry.register(ARROW_CAMERA_ENTITY, ArrowCameraEntityRender::new);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			stopArrowCam();
			System.out.println("Player disconnected from the server.");
		});
	}
	public static void startArrowCam(ProjectileEntity arrow) {
		if (!isInArrowCam()) {
			camera = new ArrowCameraEntity(ARROW_CAMERA_ENTITY, arrow.getWorld());
			camera.setTarget(arrow);
			camera.setPos(arrow.getPos().x, arrow.getPos().y, arrow.getPos().z);

			MinecraftClient mc = MinecraftClient.getInstance();
			ClientWorld world = mc.world;

			if (world != null) {
				world.addEntity(camera);

				GameOptions options = mc.options;

				hideGUI = options.hudHidden;
				fovSetting = options.getFov().getValue();
				if(options.getPerspective().isFirstPerson()){
					thirdPersonView = 0;
				}else{
					thirdPersonView = options.getPerspective().isFrontView()?1:2;
				}
				options.setPerspective(Perspective.THIRD_PERSON_BACK);
				options.hudHidden = true;
				options.getFov().setValue(options.getFov().getValue()+1);

				mc.setCameraEntity(camera);
			} else {
				camera = null;
			}
		}
	}

	public static void stopArrowCam() {
		if (isInArrowCam()) {
			MinecraftClient mc = MinecraftClient.getInstance();
			GameOptions options = mc.options;

			options.hudHidden = hideGUI;
			options.getFov().setValue(fovSetting);
			if(ArrowCamera.instance.thirdPersonView==0){
				options.setPerspective(Perspective.FIRST_PERSON);
			}else{
				options.setPerspective(ArrowCamera.instance.thirdPersonView == 1 ? Perspective.THIRD_PERSON_BACK : Perspective.THIRD_PERSON_FRONT);
			}
			mc.setCameraEntity(mc.player);
			if (camera != null && !camera.isRemoved()) {
				camera.discard();
			}
			camera = null;
		}
	}
	public static boolean isInArrowCam() {
		return camera != null;
	}

	public boolean isArrowInGround(ProjectileEntity projectile) {
		if(projectile instanceof ArrowEntity || projectile instanceof SpectralArrowEntity || projectile instanceof TridentEntity) {
			MinecraftClient client = MinecraftClient.getInstance();
			if(client == null){return true;}

			Vec3d start = projectile.getPos();
			Vec3d end = start.add(projectile.getVelocity().normalize().multiply(projectile.getVelocity().length()));

            BlockHitResult result = client.world.raycast(new RaycastContext(
					start,
					end,
					RaycastContext.ShapeType.COLLIDER,
					RaycastContext.FluidHandling.NONE,
					projectile
			));

			return result.getType() == HitResult.Type.BLOCK;
		}else{
			return false;
		}
	}
}