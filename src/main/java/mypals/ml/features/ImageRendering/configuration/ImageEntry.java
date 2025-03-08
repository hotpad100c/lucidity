package mypals.ml.features.ImageRendering.configuration;

import mypals.ml.features.ImageRendering.ImageDataParser;
import mypals.ml.features.ImageRendering.ImageRenderer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class ImageEntry {
    private boolean selected;
    private final int index;
    public Identifier textureID;
    //public ImageDataParser.ImageData data;
    public boolean ready;
    public String path;
    public String name;
    public double[] pos;
    public double[] rotation;
    public double[] scale;

    public ImageEntry(boolean ready, @Nullable int index, @Nullable String name, @Nullable String orgPath, @Nullable Identifier texturePath, double[] pos, double[] rotation, double[] scale) {
        this.ready = ready;
        this.index = index;
        this.name = name;
        this.path = orgPath;
        this.textureID = texturePath;
        this.pos = pos;
        this.rotation = rotation;
        this.scale = scale;
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
    public boolean isReady() {
        return ready;
    }
    /*public ImageDataParser.ImageData getData() {
        return data;
    }*/
    public int getIndex() {
        return index;
    }
    public double[] getPos() {
        return pos;
    }
    public double[] getRotation() {
        return rotation;
    }
    public double[] getScale() {
        return scale;
    }
    public String getPath() {
        return path;
    }
    public Identifier getTexture() {
        return textureID;
    }

    protected abstract void onClicked();

}
