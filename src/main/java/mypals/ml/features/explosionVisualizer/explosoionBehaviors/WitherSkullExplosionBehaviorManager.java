package mypals.ml.features.explosionVisualizer.explosoionBehaviors;

import net.minecraft.block.BlockState;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WitherSkullExplosionBehaviorManager extends EntityExplosionBehaviorManager {
    public WitherSkullEntity witherSkull;
    public float getEffectiveExplosionResistance(World world, BlockPos pos, BlockState blockState, float max) {
        return witherSkull.isCharged() && WitherEntity.canDestroy(blockState) ? Math.min(0.8F, max) : max;
    }
}
