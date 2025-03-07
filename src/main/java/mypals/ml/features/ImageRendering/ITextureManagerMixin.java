package mypals.ml.features.ImageRendering;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ITextureManagerMixin {
    default void lucidity$destroyAll(ArrayList<Identifier> identifier) {}
    default void lucidity$destroyAllExcept(Identifier identifier, ArrayList<Identifier> identifiers) {}

    default ArrayList<Identifier> lucidity$getAll(Identifier identifier) {
        return new ArrayList<>();
    }
}

