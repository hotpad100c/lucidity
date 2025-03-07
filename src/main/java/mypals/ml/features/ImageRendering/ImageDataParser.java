package mypals.ml.features.ImageRendering;

import mypals.ml.config.LucidityConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.TheEndBiomeCreator;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static mypals.ml.Lucidity.LOGGER;
import static mypals.ml.Lucidity.MOD_ID;
import static mypals.ml.config.LucidityConfig.picturesToRender;

public class ImageDataParser {
    private static final Object IMAGES_LOCK = new Object();
    private static final Identifier LOST = Identifier.of(MOD_ID, "textures/lost-file.png");
    public static final String TEMP_TEXTURE_PATH = "textures/temp/";
    private static final String GENERATED_PATH = "assets/" + MOD_ID + "/textures/generated/";
    private static final ConcurrentHashMap<String, Map.Entry<Identifier, ImageData>> images = new ConcurrentHashMap<>();

    public static class ImageData {
        public boolean enabled;
        public enum Type{
            IMAGE,
            GIF,
            VIDEO
        };
        public Type type;
        public int index;
        public String path;
        public String name;
        public double[] pos;
        public double[] rotation;
        public double[] scale;

        public ImageData(int index, String path, String name, double[] pos, double[] rotation, double[] scale, boolean enabled) {
            this.index = index;
            this.path = path;
            this.name = name;
            this.pos = pos;
            this.rotation = rotation;
            this.scale = scale;
            this.enabled = enabled;
        }

        public String getPath() {
            return path;
        }
        public boolean isEnabled() {
            return enabled;
        }
        public Type getFileType() {
            return type;
        }
        public String getName() {
            return name;
        }

        public double[] getPos() {
            return pos;
        }

        public int getIndex() {
            return index;
        }

        public double[] getRotation() {
            return rotation;
        }

        public double[] getScale() {
            return scale;
        }

        @Override
        public String toString() {
            return String.format("%s;%s;%s;%s;%s;%s",
                    path,
                    name,
                    Arrays.toString(pos),
                    Arrays.toString(rotation),
                    Arrays.toString(scale),
                    enabled
            );
        }
    }
    public static ConcurrentHashMap<String, Map.Entry<Identifier, ImageData>> getImages() {
        synchronized (IMAGES_LOCK) {
        return images;
        }
    }

    /**
     * Prepare images for rendering by processing both local and online images.
     */
    public static void prepareImages() {
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();

        ArrayList<Identifier> identifiers = new ArrayList<>();
        ConcurrentHashMap<String, Map.Entry<Identifier, ImageData>> newImages = new ConcurrentHashMap<>();
        List<String> newPicturesToRender = picturesToRender;
        new Thread(()->{
            for (int i = 0; i < picturesToRender.size(); i++) {
                String pic = picturesToRender.get(i);
                ImageData data = parse(pic, i);
                if (data != null) {
                    Identifier id;
                    if (data.getPath().startsWith("https://")) {
                        id = prepareOnlineImage(newPicturesToRender, newImages, pic, data);
                    } else {
                        id = prepareLocalImage(newPicturesToRender, newImages, pic, data);
                    }
                    identifiers.add(id);
                }

            }
            synchronized (IMAGES_LOCK) {
                images.clear();
                images.putAll(newImages);
                picturesToRender = newPicturesToRender;
            }

            ((ITextureManagerMixin)textureManager).lucidity$destroyAllExcept(Identifier.of(MOD_ID, TEMP_TEXTURE_PATH), identifiers);
        }).start();

    }

    /**
     * Prepare a local image and add it to the texture manager.
     */
    public static Identifier prepareLocalImage(List<String> picturesToRender, ConcurrentHashMap<String, Map.Entry<Identifier, ImageData>> images, String converted, ImageData data) {
        Random random = new Random();
        Identifier imageCreatedId = LOST;
        try {
            if (images.get(data.getName()) != null) {
                String oldName = data.getName();
                data.name = data.getName() + "_";
                picturesToRender.set(picturesToRender.indexOf(converted), data.toString());
                changeMapKey(images, oldName, data.name);
                LucidityConfig.CONFIG_HANDLER.save();
            }
            imageCreatedId = createTexture(data.getPath(), data.getName());
        }catch (Exception e){
            e.printStackTrace();
        }
        images.put(data.getName(), Map.entry(imageCreatedId, data));
        return imageCreatedId;
    }

    /**
     * Download an image from a URL and save it locally.
     */
    public static Identifier downloadPicture(ImageData data) {
       /* Path gameDir = FabricLoader.getInstance().getGameDir();
        File generatedDir = new File(String.valueOf(gameDir), GENERATED_PATH);
        File file = new File(generatedDir.getPath() + "/" + data.getName() + ".png");
        if (!file.exists()) {
            try (InputStream inputStream = new URL(data.getPath()).openStream()) {
                BufferedImage image = ImageIO.read(inputStream);
                ImageIO.write(image, "png", file);
                System.out.println("Image saved to: " + file.getPath());
                return true;
            } catch (IOException e) {
                System.err.println("Error downloading image: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
        }*/
        String nickName = data.getName();
        try (InputStream inputStream = new URL(data.getPath()).openStream()) {
            TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
            NativeImage image = NativeImage.read(inputStream);
            textureManager.registerTexture(Identifier.of(MOD_ID, TEMP_TEXTURE_PATH + nickName), new NativeImageBackedTexture(image));
            return Identifier.of(MOD_ID, TEMP_TEXTURE_PATH + nickName);
        } catch (IOException e) {
            System.err.println("Error downloading image: " + e.getMessage());
            e.printStackTrace();
            return LOST;
        }
    }

    /**
     * Prepare an online image by downloading it and then treating it as a local image.
     */
    public static Identifier prepareOnlineImage(List<String> picturesToRender,ConcurrentHashMap<String, Map.Entry<Identifier, ImageData>> images,String pic, ImageData data) {
        Random random = new Random();
        if (images.get(data.getName()) != null) {
            String oldName = data.getName();
            data.name = data.getName() + "_";
            picturesToRender.set(picturesToRender.indexOf(pic), data.toString());
            changeMapKey(images, oldName, data.name);
            LucidityConfig.CONFIG_HANDLER.save();
        }
        Identifier imageCreatedId = downloadPicture(data);
        images.put(data.name, Map.entry(imageCreatedId, data));
        return imageCreatedId;
    }

    /**
     * Change the key of a map entry.
     */
    public static <K, V> void changeMapKey(Map<K, V> map, K oldKey, K newKey) {
        if (map.containsKey(oldKey)) {
            V value = map.remove(oldKey);
            map.put(newKey, value);
        }
    }

    /**
     * Create a texture from a file path and register it with the texture manager.
     */
    public static Identifier createTexture(String texturePath, String nickName) {
        File file = new File(texturePath);
        Path gameDir = FabricLoader.getInstance().getGameDir();
        File generatedDir = new File(String.valueOf(gameDir), GENERATED_PATH);
        generatedDir.mkdirs();
        if (!file.exists()) {
            return LOST;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();
        try {
            NativeImage image = NativeImage.read(Files.newInputStream(file.toPath()));
            textureManager.registerTexture(Identifier.of(MOD_ID, TEMP_TEXTURE_PATH + nickName), new NativeImageBackedTexture(image));
        } catch (IOException e) {
            LOGGER.error("Failed to create texture: " + e.getMessage());
        }
        return Identifier.of(MOD_ID, TEMP_TEXTURE_PATH + nickName);
    }

    /**
     * Parse a string into an array of doubles.
     */
    private static double[] parseArray(String arrayString, int expectCount) {
        String[] elements = arrayString.replaceAll("[\\[\\]]", "").split(",");
        return Arrays.copyOf(
                Arrays.stream(elements)
                        .mapToDouble(Double::parseDouble)
                        .toArray(),
                expectCount
        );
    }

    /**
     * Parse image data from a string.
     */
    public static ImageData parse(String input, int index) {
        String[] parts = input.split(";");
        if (parts.length != 6) {
            return null;
        }

        String path = parts[0];
        String name = parts[1];
        double[] pos = parseArray(parts[2], 3);
        double[] rotation = parseArray(parts[3], 3);
        double[] scale = parseArray(parts[4], 2);
        boolean enabled = Boolean.parseBoolean(parts[5]);

        return new ImageData(index, path, name, pos, rotation, scale, enabled);
    }
    public static  Vector2d toBlockScale(float ppb, Vector2d scale, Vector2i size) {

        double blocksX = (size.x * scale.x) / ppb;
        double blocksY = (size.y * scale.y) / ppb;

        return new Vector2d(blocksX, blocksY);
    }
}
