package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {
    @Shadow @Final protected T handler;

    @Shadow protected int backgroundHeight;

    @Shadow @Final protected Text playerInventoryTitle;

    @Shadow protected int playerInventoryTitleX;

    @Shadow protected int playerInventoryTitleY;

    @Shadow protected int titleX;

    @Shadow protected int titleY;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @WrapMethod(method = "drawForeground")
    private void renderComparatorOutput(DrawContext context, int mouseX, int mouseY, Operation<Void> original) {
        if (this.client == null || this.client.player == null) {
            return;
        }

        if (this.handler instanceof GenericContainerScreenHandler ||
                this.handler instanceof FurnaceScreenHandler ||
                this.handler instanceof HopperScreenHandler ||
                this.handler instanceof Generic3x3ContainerScreenHandler ||
                this.handler instanceof BrewingStandScreenHandler ||
                this.handler instanceof ShulkerBoxScreenHandler ||
                this.handler instanceof BlastFurnaceScreenHandler ||
                this.handler instanceof SmokerScreenHandler) {

            ScreenHandler screenHandler = this.handler;

            float totalFullness = 0.0F; // 所有槽位的满度之和
            int totalSlots = 0; // 槽位总数

            // 遍历槽位计算总满度
            for (Slot slot : screenHandler.slots) {
                if (slot.inventory instanceof PlayerInventory) {
                    continue; // 跳过玩家物品栏的槽位
                }
                totalSlots++;
                ItemStack itemStack = slot.getStack();
                if (!itemStack.isEmpty()) {
                    // 计算单个槽位的满度并累加
                    totalFullness += (float) itemStack.getCount() / (float) itemStack.getMaxCount();
                }
            }


            float averageFullness = totalFullness > 0 ? totalFullness / totalSlots : 0.0F;
            int comparatorOutput = totalFullness > 0 ? (int) Math.floor(1 + averageFullness * 14) : 0;

            String outputText = "(C: " + comparatorOutput + ")";
            context.drawText(this.textRenderer, this.title.getString() + outputText, this.titleX, this.titleY, 4210752, false);
        } else if(this.handler instanceof CrafterScreenHandler crafterScreenHandler){
            int affectSlots = 0;
            for (Slot slot : crafterScreenHandler.slots) {
                if (slot.inventory instanceof PlayerInventory) {
                    continue;
                }
                if(crafterScreenHandler.isSlotDisabled(slot.id) || !slot.getStack().isEmpty()) {
                    affectSlots++;
                }
            }
            String outputText = "(C: " + affectSlots + ")";
            context.drawText(this.textRenderer, this.title.getString() + outputText, this.titleX, this.titleY, 4210752, false);

        }else if(this.handler instanceof LecternScreenHandler lecternScreenHandler){
            int currentPage = lecternScreenHandler.getPage();
            int pageCount = getPageCount(lecternScreenHandler.getBookItem());
            float f = pageCount > 1 ? (float)currentPage / ((float)pageCount - 1.0F) : 1.0F;
            int result = MathHelper.floor(f * 14.0F) + 1;
            String outputText = "(C: " + result + ")";
            context.drawText(this.textRenderer, this.title.getString() + outputText, this.titleX, this.titleY, 4210752, false);
        }else {
            context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false);
        }
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 4210752, false);
    }
    private static int getPageCount(ItemStack stack) {
        WrittenBookContentComponent writtenBookContentComponent = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (writtenBookContentComponent != null) {
            return writtenBookContentComponent.pages().size();
        } else {
            WritableBookContentComponent writableBookContentComponent = stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
            return writableBookContentComponent != null ? writableBookContentComponent.pages().size() : 0;
        }
    }

}
