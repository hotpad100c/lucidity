package mypals.ml.features.ImageRendering.configuration;

import mypals.ml.features.ImageRendering.ImageDataParser;
import mypals.ml.features.ImageRendering.ImageRenderer;
import net.minecraft.util.Identifier;

public abstract class ImageEntry {
    private boolean selected;
    private final int index;
    public String name;
    public Identifier texturePath;
    public ImageDataParser.ImageData data;


    public ImageEntry(int index, String name, Identifier texturePath, ImageDataParser.ImageData data) {
        this.index = index;
        this.name = name;
        this.texturePath = texturePath;
        this.data = data;
    }
    public void setSelected(boolean selected){
        this.selected = selected;
    }
    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public ImageDataParser.ImageData getData() {
        return data;
    }

    public Identifier getTexture() {
        return texturePath;
    }

    protected abstract void onClicked();

}
