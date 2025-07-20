package mypals.ml.features.selectiveRendering;

import mypals.ml.config.LucidityConfig;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static mypals.ml.Lucidity.*;
import static mypals.ml.config.Keybinds.*;
import static mypals.ml.config.LucidityConfig.*;
import static mypals.ml.features.selectiveRendering.IntersectionResolver.cutBox;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.*;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.selectedBlockTypes;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.selectedEntityTypes;
import static mypals.ml.features.selectiveRendering.SelectiveRenderingManager.wand;

public class WandActionsManager {
    public static BlockPos pos1;
    public static BlockPos pos2;
    public static int SELECT_COOLDOWN = 5;
    public static int selectCoolDown = SELECT_COOLDOWN;

    public static boolean deleteMode = false;
    public enum WandApplyToMode {
        APPLY_TO_BLOCKS("config.lucidity.wand.apply_to_blocks","textures/gui/rendering_mode/wand_mode_blocks.png"),
        APPLY_TO_ENTITIES("config.lucidity.wand.apply_to_entities","textures/gui/rendering_mode/wand_mode_entities.png"),
        APPLY_TO_PARTICLES("config.lucidity.wand.apply_to_particles","textures/gui/rendering_mode/wand_mode_particles.png");
        private final String translationKey;
        private final String icon;

        WandApplyToMode(String translationKey, String icon) {
            this.translationKey = translationKey;
            this.icon = icon;
        }
        public String getTranslationKey() {
            return translationKey;
        }
        public String getIcon() {
            return icon;
        }
    }
    public static WandApplyToMode wandApplyToMode = WandApplyToMode.APPLY_TO_BLOCKS;
    public static String resolveWandMode(int index) {
        WandApplyToMode[] modes = WandApplyToMode.values();

        if (index >= 0 && index < modes.length) {
            wandApplyToMode = modes[index];
            return modes[index].getTranslationKey();
        }
        return "-";
    }
    public static void selectingAction(BlockPos pos, Hand hand, PlayerEntity player, Boolean isFirstPoint) {
        if (isFirstPoint) {
            pos1 = pos;
            player.swingHand(hand);
            player.playSound(SoundEvents.BLOCK_CHAIN_PLACE);
            if(pos1 != null){
                player.playSound(SoundEvents.BLOCK_CHAIN_PLACE);
            }
        } else{
            pos2 = pos;
            player.swingHand(hand);
            player.playSound(SoundEvents.BLOCK_CHAIN_PLACE);
            if(pos2 != null){
                player.playSound(SoundEvents.BLOCK_CHAIN_PLACE);
            }
        }
    }
    public static void addAreaAction(BlockPos pos, Hand hand, PlayerEntity player, World world) {
        if (pos1 != null && pos2 !=null) {
            LucidityConfig.CONFIG_HANDLER.instance();
            LucidityConfig.selectedAreasSaved.add(pos1.getX() + "," + pos1.getY() + "," + pos1.getZ() + ":"
                    + pos2.getX() + "," + pos2.getY() + "," + pos2.getZ());
            LucidityConfig.CONFIG_HANDLER.save();
            onConfigUpdated();
            pos1 = null;
            pos2 = null;
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE);
        }else{
            player.sendMessage(Text.literal(Text.translatable("config.lucidity.cant_add_area").getString()), true);
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value());
        }
    }
    public static void cutAreaAction(PlayerEntity player) {
        if (pos1 != null && pos2 !=null) {
            LucidityConfig.CONFIG_HANDLER.instance();
            AreaBox cutBox = new AreaBox(pos1,pos2, Color.RED,0.2f,true);
            List<AreaBox> remainingBoxes = new ArrayList<>();
            AtomicBoolean deletedSomething = new AtomicBoolean(false);

            selectedAreas.forEach(targetArea->{
                remainingBoxes.addAll(cutBox(targetArea, cutBox));
                try {
                    LucidityConfig.selectedAreasSaved.remove(selectedAreas.indexOf(targetArea));
                    selectedAreas.remove(targetArea);
                }catch (Exception e){
                    System.out.println("SelectedAreas in config file is not same with current selectedAreas in-game!");
                }
                deletedSomething.set(true);
            });
            if(deletedSomething.get()){
                remainingBoxes.forEach(box->{
                    LucidityConfig.selectedAreasSaved.add(box.minPos.getX() + "," + box.minPos.getY() + "," + box.minPos.getZ() + ":"
                            + box.maxPos.getX() + "," + box.maxPos.getY() + "," + box.maxPos.getZ());
                });
                LucidityConfig.CONFIG_HANDLER.save();
                onConfigUpdated();
            }
            pos1 = null;
            pos2 = null;
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value());
        }else{
            player.sendMessage(Text.literal(Text.translatable("config.lucidity.cant_add_area").getString()), true);
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value());
        }
    }
    public static void selectBlockTypeAction(BlockPos pos, Hand hand, PlayerEntity player, World world) {
        LucidityConfig.CONFIG_HANDLER.instance();
        String id = Registries.BLOCK.getId(world.getBlockState(pos).getBlock()).toString();
        if(world.getBlockState(pos).getBlock() == Blocks.AIR ){return;}
        if (selectedBlockTypes.get(Registries.BLOCK.getRawId(world.getBlockState(pos).getBlock())) == null) {
            LucidityConfig.selectedBlockTypes.add(id);
            player.sendMessage(Text.literal(Text.translatable("config.lucidity.add_type").getString() + id), true);
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE);
        } else {
            LucidityConfig.selectedBlockTypes.remove(id);
            player.sendMessage(Text.literal(Text.translatable("config.lucidity.removed_type").getString() + id), true);
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value());
        }
        player.swingHand(hand);
        LucidityConfig.CONFIG_HANDLER.save();
        onConfigUpdated();
    }
    public static void selectEntityTypeAction(Entity target, Hand hand, PlayerEntity player, World world) {
        LucidityConfig.CONFIG_HANDLER.instance();
        if(target == null ){return;}
        String id = Registries.ENTITY_TYPE.getId(target.getType()).toString();
        if (!selectedEntityTypes.contains(Registries.ENTITY_TYPE.getRawId(target.getType()))) {
            LucidityConfig.selectedEntityTypes.add(id);
            player.sendMessage(Text.literal(Text.translatable("config.lucidity.add_type").getString() + id), true);
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE);
        } else {
            LucidityConfig.selectedEntityTypes.remove(id);
            player.sendMessage(Text.literal(Text.translatable("config.lucidity.removed_type").getString() + id), true);
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value());
        }
        player.swingHand(hand);
        LucidityConfig.CONFIG_HANDLER.save();
        onConfigUpdated();
    }
    public static void switchRenderMod(boolean increase){
        LucidityConfig.CONFIG_HANDLER.instance();
        if(selectCoolDown > 0){return;}
        switch (wandApplyToMode){
            case WandApplyToMode.APPLY_TO_BLOCKS -> {
                if (increase) {
                    renderModeBlock = renderModeBlock == RenderMode.values().length - 1 ? 0 : renderModeBlock + 1;
                } else {
                    renderModeBlock = renderModeBlock == 0 ? RenderMode.values().length - 1 : renderModeBlock - 1;
                }
                resolveSelectiveBlockRenderingMode(renderModeBlock);
            }
            case WandApplyToMode.APPLY_TO_ENTITIES -> {
                if (increase) {
                    renderModeEntity = renderModeEntity == RenderMode.values().length - 1 ? 0 : renderModeEntity + 1;
                } else {
                    renderModeEntity = renderModeEntity == 0 ? RenderMode.values().length - 1 : renderModeEntity - 1;
                }
                resolveSelectiveEntityRenderingMode(renderModeEntity);
            }
            case WandApplyToMode.APPLY_TO_PARTICLES -> {
                if (increase) {
                    renderModeParticle = renderModeParticle == RenderMode.values().length - 1 ? 0 : renderModeParticle + 1;
                } else {
                    renderModeParticle = renderModeParticle == 0 ? RenderMode.values().length - 1 : renderModeParticle - 1;
                }
                resolveSelectiveParticleRenderingMode(renderModeParticle);
            }
        }
        LucidityConfig.CONFIG_HANDLER.save();
        if(wandApplyToMode == WandApplyToMode.APPLY_TO_BLOCKS){
            onConfigUpdated();
        }
        selectCoolDown = SELECT_COOLDOWN;
    }
    public static void switchWandMod(boolean increase){
        LucidityConfig.CONFIG_HANDLER.instance();
        if (increase) {
            wandApplyMode = wandApplyMode == WandApplyToMode.values().length - 1 ? 0 : wandApplyMode + 1;
        } else {
            wandApplyMode = wandApplyMode == 0 ? WandApplyToMode.values().length - 1 : wandApplyMode - 1;
        }

        //MinecraftClient.getInstance().player.sendMessage(Text.literal(Text.translatable(resolveWandMode(wandApplyMode)).getString()), true);
        LucidityConfig.CONFIG_HANDLER.save();
        onConfigUpdated();
        resolveWandMode(wandApplyMode);
    }
    public static void clearArea(BlockPos pos, Hand hand, PlayerEntity player, World world){
        ItemStack heldItem = player.getStackInHand(hand);
        if((pos1 != null || pos2 != null)) {
            pos1 = null;
            pos2 = null;
            player.sendMessage(Text.literal(Text.translatable("config.lucidity.clear").getString()), true);
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value());
        }
    }
    public static List<AreaBox> getAreasToDelete(BlockPos pos, boolean delete){
        LucidityConfig.CONFIG_HANDLER.instance();
        AtomicBoolean deletedSomething = new AtomicBoolean(false);
        List<AreaBox> areas = new ArrayList<>();
        selectedAreas.forEach(area->{
            if(isInsideArea(Vec3d.of(pos),area)){
                areas.add(area);
                if(delete){
                    try {
                        LucidityConfig.selectedAreasSaved.remove(selectedAreas.indexOf(area));
                    }catch (Exception e){
                        System.out.println("SelectedAreas in config file is not same with current selectedAreas in-game!");
                    }
                    deletedSomething.set(true);
                }
            }
        });
        if(deletedSomething.get()){
            LucidityConfig.CONFIG_HANDLER.save();
            onConfigUpdated();
        }
        return areas;
    }
    private static void getMouseScroll() {
        if(MinecraftClient.getInstance().getWindow() == null){return;}
        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        GLFW.glfwSetScrollCallback(windowHandle, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                handleMouseWheel(yoffset);
            }
        });
    }

    private static void handleMouseWheel(double scroll) {
        /*if (scroll > 0) {
            System.out.println("滚轮向上滚动！");
        } else if (scroll < 0) {
            System.out.println("滚轮向下滚动！");
        }*/
    }
    public static void wandActions(MinecraftClient client){
        if(client.world == null){return;}
        boolean shouldSelect = client.player.getMainHandStack().getItem() == wand || (selectInSpectator && client.player.isSpectator());
        deleteMode = false;
        //getMouseScroll();
        if(selectCoolDown <= 0 && shouldSelect && client.player != null){
            if (deleteArea.isPressed()) {
                deleteMode = true;
                BlockHitResult blockRayCast = getPlayerLookedBlock(client.player, client.world);
                if(client.options.useKey.isPressed()){
                    getAreasToDelete(blockRayCast.getBlockPos(),true);
                    client.player.swingHand(client.player.getActiveHand());
                }
                else if (pos1 != null && pos2 != null && client.options.attackKey.isPressed()) {
                    cutAreaAction(client.player);
                    selectCoolDown = SELECT_COOLDOWN;
                    client.player.swingHand(client.player.getActiveHand());
                }
            } else if (client.options.attackKey.isPressed()) {
                if (client.options.sneakKey.isPressed()) {
                    clearArea(client.player.getBlockPos(), client.player.getActiveHand(), client.player, client.world);
                    selectCoolDown = SELECT_COOLDOWN;
                } else if (switchRenderMode.isPressed()){
                    switchRenderMod(false);
                    selectCoolDown = SELECT_COOLDOWN;
                }else if (addArea.isPressed()){
                    addAreaAction(client.player.getBlockPos(), client.player.getActiveHand(), client.player, client.world);
                    selectCoolDown = SELECT_COOLDOWN;
                }else {
                    BlockHitResult blockBreakingRayCast = getPlayerLookedBlock(client.player, client.world);
                    selectingAction(blockBreakingRayCast.getBlockPos(), client.player.getActiveHand(), client.player, true);
                    selectCoolDown = SELECT_COOLDOWN;
                }
            }
            else if (client.options.pickItemKey.isPressed()) {
                if (switchRenderMode.isPressed()){
                    switchWandMod(true);
                    selectCoolDown = SELECT_COOLDOWN;
                }
            }
            else if (client.options.useKey.isPressed()) {
                if (switchRenderMode.isPressed()){
                    switchRenderMod(true);
                    selectCoolDown = SELECT_COOLDOWN;
                }else if (addArea.isPressed()){
                    if(wandApplyToMode == WandApplyToMode.APPLY_TO_BLOCKS){
                        BlockHitResult blockBreakingRayCast = getPlayerLookedBlock(client.player, client.world);
                        selectBlockTypeAction(blockBreakingRayCast.getBlockPos(), client.player.getActiveHand(), client.player, client.world);
                    }
                    if(wandApplyToMode == WandApplyToMode.APPLY_TO_ENTITIES){
                        EntityHitResult entityHitResult = getPlayerLookedEntity(client.player, client.world);
                        selectEntityTypeAction(entityHitResult.getEntity(), client.player.getActiveHand(), client.player, client.world);
                    }
                    selectCoolDown = SELECT_COOLDOWN;
                } else {
                    BlockHitResult blockBreakingRayCast = getPlayerLookedBlock(client.player, client.world);
                    selectingAction(blockBreakingRayCast.getBlockPos(), client.player.getActiveHand(), client.player, false);
                    selectCoolDown = SELECT_COOLDOWN;
                }
            }
        }
    }
}
