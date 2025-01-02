package mypals.ml.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    public void hasGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(stack != null && stack.getItem() == wand){
            cir.setReturnValue(true);
        }
    }
}
