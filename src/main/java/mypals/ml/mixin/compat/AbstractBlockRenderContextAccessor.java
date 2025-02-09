package mypals.ml.mixin.compat;

import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBlockRenderContext.class)
public interface AbstractBlockRenderContextAccessor {
    @Accessor("pos")
    BlockPos getPos();
    @Accessor("level")
    BlockRenderView getLevel();
}
