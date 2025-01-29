package mypals.ml.features.explosionVisualizer.simulate;

import mypals.ml.features.explosionVisualizer.data.ExplosionData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ExplosionSimulateManager{
    public static ExplosionData.ExplosionAffectedObjects simulateExplosiveBlocks(World world, BlockPos pos, float explosionPower) {
        Vec3d explosionPos = pos.toCenterPos();

        ExplosionSimulator simulator = new ExplosionSimulator(true, world, (float) explosionPos.getX(), (float) explosionPos.getY(), (float) explosionPos.getZ(), explosionPower);
        simulator.simulate();
        return simulator.getAffected();


    }
    public static ExplosionData.ExplosionAffectedObjects simulateExplosiveEntitys(World world, Vec3d pos, float explosionPower) {
        Vec3d explosionPos = pos;

        ExplosionSimulator simulator = new ExplosionSimulator(false, world, (float) explosionPos.getX(), (float) explosionPos.getY(), (float) explosionPos.getZ(), explosionPower);
        simulator.simulate();
        return simulator.getAffected();
    }
    public static ExplosionData.ExplosionAffectedObjects simulateFakeExplosions(World world, Vec3d pos, float explosionPower, boolean ignor) {
        Vec3d explosionPos = pos;

        ExplosionSimulator simulator = new ExplosionSimulator(ignor, world, (float) explosionPos.getX(), (float) explosionPos.getY(), (float) explosionPos.getZ(), explosionPower);
        simulator.simulate();
        return simulator.getAffected();
    }
}
