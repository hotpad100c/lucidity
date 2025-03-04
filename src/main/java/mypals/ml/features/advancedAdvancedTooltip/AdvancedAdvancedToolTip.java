package mypals.ml.features.advancedAdvancedTooltip;

import mypals.ml.config.LucidityConfig;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.block.Block;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static mypals.ml.config.LucidityConfig.advancedAdvancedToolTips;

public class AdvancedAdvancedToolTip {
    public static final List<String> DEFAULT_TOOLTIP_OPTIONS = Arrays.asList(
            "mapColorProvider",
            "collidable",
            "soundGroup",
            "luminance",
            "resistance",
            "hardness",
            "toolRequired",
            "randomTicks",
            "slipperiness",
            "velocityMultiplier",
            "jumpVelocityMultiplier",
            "lootTableKey",
            "opaque",
            "burnable",
            "forceSolid",
            "pistonBehavior",
            "solidBlockPredicate",
            "instrument",
            "replaceable",
            "dynamicBounds"
    );
    public static void onInitialize(){
        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {

            if(itemStack.getItem() instanceof BlockItem blockItem && advancedAdvancedToolTips){
                List<Text> additionalToolTips = new ArrayList<>();
                additionalToolTips.add(Text.of(""));
                if(blockItem == null) return;
                LucidityConfig.blockToolTipAttributes.forEach(s -> {
                    checkAttributes(s,blockItem).forEach(s1 -> {
                        additionalToolTips.add(Text.of(s1));
                    });

                });
                list.addAll(additionalToolTips);
            }
        });
    }
    public static List<String> getSounds(BlockSoundGroup soundGroup){
        List<String> properties = new ArrayList<>();
        properties.add("-  "+soundGroup.getBreakSound().getId());
        properties.add("-  "+soundGroup.getFallSound().getId());
        properties.add("-  "+soundGroup.getHitSound().getId());
        properties.add("-  "+soundGroup.getPlaceSound().getId());
        properties.add("-  "+soundGroup.getStepSound().getId());
        return properties;
    }
    public static String getPistonBehaviorTranslation(PistonBehavior behavior) {
        switch (behavior) {
            case NORMAL:
                return Text.translatable("piston_behavior.normal").getString();
            case DESTROY:
                return Text.translatable("piston_behavior.destroy").getString();
            case BLOCK:
                return Text.translatable("piston_behavior.block").getString();
            case IGNORE:
                return Text.translatable("piston_behavior.ignore").getString();
            case PUSH_ONLY:
                return Text.translatable("piston_behavior.push_only").getString();
            default:
                return "?";
        }
    }

    public static List<String> getTools(Block block) {
        List<String> allowedtools = new ArrayList();
        String need = Text.translatable("tools.need").getString();
        String minable = Text.translatable("tools.mineable").getString();
        String tool = Text.translatable("tools.tool").getString();

        block.getRegistryEntry().streamTags().filter((tag) ->
                tag.id().getPath().startsWith("mineable/")).forEach((tag) -> {
                    String path = tag.id().getPath().replace("mineable/", "");
                    String mineable = path.substring(0, 1).toUpperCase();
                    allowedtools.add(mineable + path.substring(1));
                });
        block.getRegistryEntry().streamTags().filter((tag) ->
                tag.id().getPath().startsWith("needs_")).forEach((tag) -> {
                    String path = tag.id().getPath().replace("needs_", "").replace("_tool", "");
                    String required = path.substring(0, 1).toUpperCase();
                    allowedtools.add(required + path.substring(1));
                });
        return allowedtools;
    }

    public static List<String> checkAttributes(String currentAttribute, BlockItem blockItem) {
        // Iterate over the attributes and trigger the corresponding action based on the attribute
        Block block =  blockItem.getBlock();
        Block.Settings settings = block.getSettings();
        List<String> properties = new ArrayList<>();
        switch (currentAttribute) {
            case "mapColorProvider":
                properties.add( Text.translatable("block.mapColorProvider").getString() + ": "
                        + settings.mapColorProvider.apply(block.getDefaultState()).color);
                break;
            case "collidable":
                properties.add( Text.translatable("block.collidable").getString() + ": " + settings.collidable);
                break;
            case "soundGroup":
                properties.add( Text.translatable("block.soundGroup").getString());
                properties.addAll(getSounds(settings.soundGroup));
                break;
            case "luminance":
                properties.add( Text.translatable("block.luminance").getString() + ": " + block.getDefaultState().getLuminance());
                break;
            case "resistance":
                properties.add( Text.translatable("block.resistance").getString() + ": " + settings.resistance);
break;
            case "hardness":
                properties.add( Text.translatable("block.hardness").getString() + ": " + settings.hardness);
break;
            case "toolRequired":
                properties.add( Text.translatable("block.toolRequired").getString() + ": " + getTools(block));
break;
            case "randomTicks":
                properties.add( Text.translatable("block.randomTicks").getString() + ": " + settings.randomTicks);
break;
            case "slipperiness":
                properties.add( Text.translatable("block.slipperiness").getString() + ": " + settings.slipperiness);
break;
            case "velocityMultiplier":
                properties.add( Text.translatable("block.velocityMultiplier").getString() + ": " + settings.velocityMultiplier);
break;
            case "jumpVelocityMultiplier":
                properties.add( Text.translatable("block.jumpVelocityMultiplier").getString() + ": " +
                        settings.jumpVelocityMultiplier);
break;
            case "lootTableKey":
                properties.add( Text.translatable("block.lootTableKey").getString() + ": " + settings.lootTableKey);
break;
            case "opaque":
                properties.add( Text.translatable("block.opaque").getString() + ": " + settings.opaque);
break;
            case "burnable":
                properties.add( Text.translatable("block.burnable").getString() + ": " + settings.burnable);
break;
            case "forceSolid":
                properties.add( Text.translatable("block.forceSolid").getString() + ": " + settings.forceSolid);
break;
            case "pistonBehavior":
                properties.add( Text.translatable("block.pistonBehavior").getString() + ": " +
                        getPistonBehaviorTranslation(settings.pistonBehavior));
break;
            case "blockBreakParticles":
                properties.add( Text.translatable("block.blockBreakParticles").getString() + ": " + settings.blockBreakParticles);
break;
            case "instrument":
                properties.add( Text.translatable("block.instrument").getString() + ": " + settings.instrument.asString());
break;
            case "replaceable":
                properties.add( Text.translatable("block.replaceable").getString() + ": " + settings.replaceable);
break;
            case "solidBlockPredicate":
                properties.add( Text.translatable("block.solidBlockPredicate").getString() + ": " + block.getDefaultState().shouldBeSolid());
break;
            case "dynamicBounds":
                properties.add( Text.translatable("block.dynamicBounds").getString() + ": " + settings.dynamicBounds);
break;
            default:

        }
        return properties;
    }
}
