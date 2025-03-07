package mypals.ml.mixin;

import mypals.ml.features.ImageRendering.ITextureManagerMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(TextureManager.class)
public abstract class TextureManagerMixin  implements ITextureManagerMixin {
    @Shadow @Final private Map<Identifier, AbstractTexture> textures;

    @Shadow public abstract void destroyTexture(Identifier id);

    @Shadow @Final private static Logger LOGGER;

    @Override
    public void lucidity$destroyAll(ArrayList<Identifier> identifiers) {
        identifiers.forEach(this::destroyTexture);

    }
    @Override
    public ArrayList<Identifier> lucidity$getAll(Identifier identifier) {
        String targetPath = identifier.getPath();
        ArrayList<Identifier> needsToRemove = new ArrayList<>();
        this.textures.forEach(((identifier1, abstractTexture) -> {
            String path = identifier1.getPath();
            boolean bl = path.startsWith(targetPath);
            if(bl){
                LOGGER.info("Destroyed " + identifier1.getPath());
                needsToRemove.add(identifier1);
            }
        }));
        return needsToRemove;
    }
    @Override
    public void lucidity$destroyAllExcept(Identifier identifierPath, ArrayList<Identifier> identifiers) {
        ArrayList<Identifier> needsToRemove = new ArrayList<>();
        this.textures.forEach(((current, abstractTexture) -> {
            String targetPath = identifierPath.getPath();
            String path = current.getPath();
            boolean bl = path.startsWith(targetPath) && !identifiers.contains(current);
            if(bl){
                needsToRemove.add(current);
            }
        }));
        needsToRemove.forEach(this::destroyTexture);

    }

}
