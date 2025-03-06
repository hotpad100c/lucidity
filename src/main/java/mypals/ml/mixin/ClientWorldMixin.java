package mypals.ml.mixin;

import mypals.ml.features.betterBarrier.BetterBarrier;
import mypals.ml.features.OreFinder.MineralFinder;
import mypals.ml.features.OreFinder.OreResolver;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Set;

import static mypals.ml.config.LucidityConfig.enableWorldEaterHelper;
import static mypals.ml.features.OreFinder.MineralFinder.isExposedMineral;
import static mypals.ml.features.OreFinder.OreResolver.tryAddToRecordedOreListOrRemove;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Redirect(method = "getBlockParticle", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    public boolean disableBarrierParticles(Set<Item> instance, Object item) {
        if (item == Items.BARRIER && BetterBarrier.shouldRenderBetterBarrier()) return false;
        return instance.contains((Item) item);
    }
    @Inject(method = "setBlockState", at = @At("RETURN"))
    public void setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {

        if(enableWorldEaterHelper) {
            for (Direction direction : Direction.values()) {

                BlockPos pos2 = pos.offset(direction);
                tryAddToRecordedOreListOrRemove(pos2);
                if (isExposedMineral(MinecraftClient.getInstance().world, pos2)) {
                    Block block = MinecraftClient.getInstance().world.getBlockState(pos2).getBlock();
                    boolean haveOreNearBy = false;
                    Color color = MineralFinder.MINERAL_BLOCKS.get(block);
                    for (Direction direction2 : Direction.values()) {
                        if (OreResolver.recordedOres.containsKey(pos2.offset(direction2)) && OreResolver.recordedOres.get(pos2.offset(direction2)).equals(color)) {
                            haveOreNearBy = true;
                        }
                    }
                    if (!haveOreNearBy)
                        OreResolver.recordedOres.put(pos2, color);
                } else {
                    OreResolver.recordedOres.remove(pos2);
                }
            }
        }

    }
}
