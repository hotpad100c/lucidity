package mypals.ml.mixin.vanilla;

import mypals.ml.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.*;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow @Nullable protected abstract <T extends ParticleEffect> Particle createParticle(T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ);

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void disableAllParticles(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir)
    {
        if (!particleRenderMode.equals(SelectiveRenderingManager.RenderMode.OFF))
        {
            if(!shouldRenderParticle(parameters.getType(),new Vec3d(x,y,z))){
                Particle particle = this.createParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
                cir.setReturnValue(particle);
                cir.cancel();
            }
        }
    }

}
