package mypals.ml.mixin;

import mypals.ml.features.betterBarrier.BetterBarrier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Redirect(method = "getBlockParticle", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    public boolean disableBarrierParticles(Set<Item> instance, Object item) {
        if (item == Items.BARRIER && BetterBarrier.shouldRenderBetterBarrier()) return false;
        return instance.contains((Item) item);
    }
}
