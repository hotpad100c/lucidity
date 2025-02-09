package mypals.ml.features.explosionVisualizer.simulate;

import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionAffectedObjects;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ExplosionSimulateManager {

    public static ExplosionAffectedObjects simulateExplosiveBlocks(World world, BlockPos pos, float explosionPower) {
        Vec3d explosionPos = pos.toCenterPos();

        ExplosionSimulator simulator = new ExplosionSimulator(null,true, world, (float) explosionPos.getX(), (float) explosionPos.getY(), (float) explosionPos.getZ(), explosionPower);
        simulator.simulate();
        return simulator.getAffected();


    }
    public static ExplosionAffectedObjects simulateExplosiveEntitys(Entity thisEntity, World world, Vec3d pos, float explosionPower) {
        Vec3d explosionPos = pos;

        ExplosionSimulator simulator = new ExplosionSimulator(thisEntity,false, world, (float) explosionPos.getX(), (float) explosionPos.getY(), (float) explosionPos.getZ(), explosionPower);
        simulator.simulate();
        return simulator.getAffected();
    }
    public static ExplosionAffectedObjects simulateFakeExplosions(World world, Vec3d pos, float explosionPower, boolean ignor) {
        Vec3d explosionPos = pos;

        ExplosionSimulator simulator = new ExplosionSimulator(null,ignor, world, (float) explosionPos.getX(), (float) explosionPos.getY(), (float) explosionPos.getZ(), explosionPower);
        simulator.simulate();
        return simulator.getAffected();
    }

}
