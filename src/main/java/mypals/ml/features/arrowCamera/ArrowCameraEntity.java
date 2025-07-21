package mypals.ml.features.arrowCamera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import static mypals.ml.features.arrowCamera.ArrowCamera.fovSetting;

public class ArrowCameraEntity extends Entity {
    private int deathDelay = 40;
    private ProjectileEntity target;

    private final float cameraYawOffset = 0.0F;
    private final float cameraPitchOffset = 0.0F;


    public ArrowCameraEntity(EntityType<? extends Entity> type, ProjectileEntity arrow) {
        this(type, arrow.getWorld());
        this.setNoGravity(true);
        this.setTarget(arrow);
        this.setPosition(arrow.getX(), arrow.getY(), arrow.getZ());
        //this.updatePosition(arrow.getX(), arrow.getY(), arrow.getZ());
        this.setVelocity(arrow.getVelocity());
        this.setYaw(arrow.getYaw());
        this.setPitch(arrow.getPitch());
    }

    public ArrowCameraEntity(EntityType<? extends Entity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }
    @Override
    public void tick() {
        if(target == null ) {
            this.discard();
            return;
        }
        super.tick();

        if (ArrowCamera.instance.isArrowInGround(target) || !target.isAlive() || !isChunkLoaded()) {
            this.setVelocity(0, 0, 0);

            if (--deathDelay <= 0) {
                ArrowCamera.stopArrowCam();
            } else if (deathDelay < 20) {
                MinecraftClient mc = MinecraftClient.getInstance();
                ClientWorld world = mc.world;
                GameOptions options = mc.options;

                if(ArrowCamera.thirdPersonView ==0){
                    options.setPerspective(Perspective.FIRST_PERSON);
                }else{
                    options.setPerspective(ArrowCamera.thirdPersonView == 2 ? Perspective.THIRD_PERSON_BACK : Perspective.THIRD_PERSON_FRONT);
                }
                var player = MinecraftClient.getInstance().player;
                if (player != null) {
                    double lerpFactor = Math.sin((1.0 - deathDelay / 20.0) * Math.PI / 2.0);

                    double newX = this.getX() + (player.getX() - this.getX()) * lerpFactor;
                    double newY = this.getY() + (player.getEyePos().getY() - this.getY()) * lerpFactor;
                    double newZ = this.getZ() + (player.getZ() - this.getZ()) * lerpFactor;
                    int newFov = (int) (options.getFov().getValue() - (fovSetting - options.getFov().getValue()) * lerpFactor);
                    options.getFov().setValue(newFov);
                    this.setPos(newX, newY, newZ);
                }
            }else if(deathDelay > 20){
                double offset = 0.01;
                double backX = Math.cos(Math.toRadians(this.getYaw()+90)) * offset;
                double backZ = Math.sin(Math.toRadians(this.getYaw()+90)) * offset;
                double backY = Math.sin(Math.toRadians(this.getPitch()+90)) * offset;

                this.setPos(this.getX() + backX, this.getY() + backY, this.getZ() + backZ);

                this.setYaw(this.getYaw());
                this.setPitch(this.getPitch());
            }
        } else {
            double offset = 0;
            double velX = target.getVelocity().x;
            double velY = target.getVelocity().y;
            double velZ = target.getVelocity().z;

            this.setPos(
                    target.getPos().x - velX * offset,
                    (target.getPos().y - velY * offset)+1,
                    target.getPos().z - velZ * offset
            );
            this.setVelocity(target.getVelocity());

            float newYaw = (float) (Math.atan2(velZ, velX) * (180.0 / Math.PI)) - 90.0F;
            float newPitch = (float) -(Math.atan2(velY, Math.sqrt(velX * velX + velZ * velZ)) * (180.0 / Math.PI));

            this.setYaw(newYaw);
            this.setPitch(newPitch);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    private boolean isChunkLoaded() {
        ChunkPos chunkPos = new ChunkPos(new BlockPos(this.getBlockPos()));
        return this.getWorld().isChunkLoaded(chunkPos.x, chunkPos.z);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        ArrowCamera.stopArrowCam();
    }

    public void setTarget(ProjectileEntity arrow) {
        this.target = arrow;
    }

    public ProjectileEntity getTarget() {
        return this.target;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean shouldRender(double distance) {
        return false;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }
}
