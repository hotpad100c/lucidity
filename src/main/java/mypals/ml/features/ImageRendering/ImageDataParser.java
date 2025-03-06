package mypals.ml.features.ImageRendering;

import mypals.ml.config.LucidityConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static mypals.ml.Lucidity.LOGGER;
import static mypals.ml.Lucidity.MOD_ID;
import static mypals.ml.config.LucidityConfig.picturesToRender;

public class ImageDataParser {

    public static final String TEMP_TEXTURE_PATH = "textures/temp/";
    private static final String GENERATED_PATH = "assets/" + MOD_ID + "/textures/generated/";
    public static ConcurrentHashMap<String, Map.Entry<Identifier, ImageData>> images = new ConcurrentHashMap<>();

    public static class ImageData {
        public int index;
        public String path;
        public String name;
        public double[] pos;
        public double[] rotation;
        public double[] scale;

        public ImageData(int index, String path, String name, double[] pos, double[] rotation, double[] scale) {
            this.index = index;
            this.path = path;
            this.name = name;
            this.pos = pos;
            this.rotation = rotation;
            this.scale = scale;
        }

        public String getPath() {
            return path;
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
            return String.format("%s;%s;%s;%s;%s",
                    path,
                    name,
                    Arrays.toString(pos),
                    Arrays.toString(rotation),
                    Arrays.toString(scale)
            );
        }
    }

    /**
     * Prepare images for rendering by processing both local and online images.
     */
    public static void prepareImages() {
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();

        // Destroy existing textures
        ((ITextureManagerMixin)textureManager).lucidity$destroyAll(Identifier.of(MOD_ID, TEMP_TEXTURE_PATH));
        images.clear();

        // Prepare new images
        for (String pic : picturesToRender) {
            ImageData data = parse(pic, picturesToRender.indexOf(pic));
            if (data != null) {
                if (data.getPath().startsWith("https://")) {
                    Map.Entry<String, ImageData> converted = prepareOnlineImage(pic, data);
                    if (converted != null) {
                        prepareLocalImage(pic, converted.getKey(), converted.getValue());
                    }
                } else {
                    prepareLocalImage(pic, pic, data);
                }
            }
        }
    }

    /**
     * Prepare a local image and add it to the texture manager.
     */
    public static void prepareLocalImage(String old, String converted, ImageData data) {
        Random random = new Random();
        Identifier imageCreatedId = Identifier.of(MOD_ID, "textures/lost-file.png");
        try {
            if (images.get(data.getName()) != null) {
                String oldName = data.getName();
                data.name = data.getName() + random.nextInt();
                picturesToRender.set(picturesToRender.indexOf(converted), data.toString());
                changeMapKey(images, oldName, data.name);
                LucidityConfig.CONFIG_HANDLER.save();
            }
            imageCreatedId = createTexture(data.getPath(), data.getName());
        }catch (Exception e){
            e.printStackTrace();
        }
        images.put(data.getName(), Map.entry(imageCreatedId, data));
    }

    /**
     * Download an image from a URL and save it locally.
     */
    public static boolean downloadPicture(ImageData data) {
        Path gameDir = FabricLoader.getInstance().getGameDir();
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
        }
    }

    /**
     * Prepare an online image by downloading it and then treating it as a local image.
     */
    public static Map.Entry<String, ImageData> prepareOnlineImage(String pic, ImageData data) {
        Random random = new Random();
        if (images.get(data.getName()) != null) {
            String oldName = data.getName();
            data.name = data.getName() + random.nextInt();
            picturesToRender.set(picturesToRender.indexOf(pic), data.toString());
            changeMapKey(images, oldName, data.name);
            LucidityConfig.CONFIG_HANDLER.save();
        }

        if (downloadPicture(data)) {
            data.path = GENERATED_PATH + data.getName() + ".png";
            picturesToRender.set(picturesToRender.indexOf(pic), data.toString());
            Identifier imageCreatedId = createTexture(data.getPath(), data.getName());
            images.put(data.name, Map.entry(imageCreatedId, data));
            return Map.entry(data.toString(), data);
        }
        images.put(data.name, Map.entry(Identifier.of(MOD_ID, "textures/lost-file.png"), data));
        return null;
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
            return Identifier.of(MOD_ID, "textures/lost-file.png");
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
        if (parts.length != 5) {
            return null;
        }

        String path = parts[0];
        String name = parts[1];
        double[] pos = parseArray(parts[2], 3);
        double[] rotation = parseArray(parts[3], 3);
        double[] scale = parseArray(parts[4], 2);

        return new ImageData(index, path, name, pos, rotation, scale);
    }
}
