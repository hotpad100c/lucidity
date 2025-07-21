package mypals.ml.mixin;

import com.mojang.brigadier.ParseResults;
import mypals.ml.features.pastBlockEvents.ClientsideBlockEventManager;
import mypals.ml.features.OreFinder.MineralFinder;
import mypals.ml.features.OreFinder.OreResolver;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static mypals.ml.config.LucidityConfig.enableWorldEaterHelper;
import static mypals.ml.config.LucidityConfig.renderBlockEvents;
import static mypals.ml.features.OreFinder.MineralFinder.isExposedMineral;
import static mypals.ml.features.OreFinder.OreResolver.tryAddToRecordedOreListOrRemove;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkMixin {

    @Shadow private ClientWorld world;


    @Inject(method = "onBlockEvent", at = @At("HEAD"))
    public void onBlockEvent(BlockEventS2CPacket packet, CallbackInfo ci) {
        if(renderBlockEvents) {
            ClientsideBlockEventManager.addSyncedBlockEvent(packet.getPos(), packet.getBlock(), packet.getType(), packet.getData());
        }
    }
    @Inject(method = "onChunkData", at = @At("RETURN"))
    private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci)
    {
        MinecraftClient.getInstance().execute(()->{
            if(enableWorldEaterHelper) {
                int cx = packet.getChunkX();
                int cz = packet.getChunkZ();
                WorldChunk worldChunk = this.world.getChunkManager().getWorldChunk(cx, cz);

                if (worldChunk != null)
                {
                    BlockPos.Mutable pos = new BlockPos.Mutable();
                    ChunkSection[] sections = worldChunk.getSectionArray();
                    for (int i = 0; i < sections.length; i++)
                    {
                        ChunkSection section = sections[i];
                        if (section != null && !section.isEmpty())
                        {
                            for (int x = 0; x < 16; x++)
                            {
                                for (int y = 0; y < 16; y++)
                                {
                                    for (int z = 0; z < 16; z++)
                                    {
                                        pos.set(x + worldChunk.getPos().getStartX(), y + (this.world.sectionIndexToCoord(i) << 4), z + worldChunk.getPos().getStartZ());

                                        if(isExposedMineral(this.world, pos)) {
                                            Block block = world.getBlockState(pos).getBlock();
                                            boolean haveOreNearBy = false;
                                            Color color = MineralFinder.MINERAL_BLOCKS.get(block);
                                            for (Direction direction : Direction.values()) {
                                                if (OreResolver.recordedOres.containsKey(pos.offset(direction)) && OreResolver.recordedOres.get(pos.offset(direction)).equals(color)) {
                                                    haveOreNearBy = true;
                                                }
                                            }
                                            if(!haveOreNearBy) {
                                                OreResolver.recordedOres.put(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), color);
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

    }
    @Inject(method = "onBlockUpdate", at = @At("HEAD"))
    public void onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        if(enableWorldEaterHelper) {
            MinecraftClient.getInstance().execute(()-> {
                BlockPos pos = packet.getPos();
                tryAddToRecordedOreListOrRemove(pos);
            });
        }
    }
    @Inject(method = "onGameJoin", at = @At("HEAD"))
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        OreResolver.recordedOres.clear();
    }
    @Inject(method = "onUnloadChunk", at = @At("HEAD"))
    private void onUnloadChunk(UnloadChunkS2CPacket packet, CallbackInfo ci)
    {
        if(enableWorldEaterHelper) {
            int cx = packet.pos().x;
            int cz = packet.pos().z;
            WorldChunk worldChunk = this.world.getChunkManager().getWorldChunk(cx, cz);
            if (worldChunk != null) {
                BlockPos.Mutable pos = new BlockPos.Mutable();
                ChunkSection[] sections = worldChunk.getSectionArray();
                for (int i = 0; i < sections.length; i++) {
                    ChunkSection section = sections[i];
                    if (section != null && !section.isEmpty()) {
                        for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < 16; y++) {
                                for (int z = 0; z < 16; z++) {

                                    pos.set(x + worldChunk.getPos().getStartX(), y + (this.world.sectionIndexToCoord(i) << 4), z + worldChunk.getPos().getStartZ());

                                    OreResolver.recordedOres.remove(pos);

                                }
                            }
                        }
                    }
                }
            }
        }
    }


}
