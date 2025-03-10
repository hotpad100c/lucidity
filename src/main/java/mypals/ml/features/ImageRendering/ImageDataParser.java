package mypals.ml.features.ImageRendering;

import mypals.ml.config.LucidityConfig;
import mypals.ml.features.ImageRendering.configuration.ImageConfigScreen;
import mypals.ml.features.ImageRendering.configuration.ImageEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.joml.Vector2d;
import org.joml.Vector2i;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static mypals.ml.Lucidity.LOGGER;
import static mypals.ml.Lucidity.MOD_ID;
import static mypals.ml.config.LucidityConfig.picturesToRender;
import static mypals.ml.features.ImageRendering.MediaTypeDetector.detectMediaType;
import static mypals.ml.features.ImageRendering.configuration.ImageConfigScreen.mapToZeroOne;

public class ImageDataParser {

    public static final String TEMP_TEXTURE_PATH = "textures/temp/";
    private static final String GENERATED_PATH = "assets/" + MOD_ID + "/textures/generated/";
    public static final Identifier LOST = Identifier.of(MOD_ID, "textures/lost-file.png");
    public static final Identifier LOADING = Identifier.of(MOD_ID, "textures/loading.png");


    public static ArrayList<ImageEntry> readyToMerge = new ArrayList<>();
    public static ConcurrentHashMap<String, ImageEntry> images = new ConcurrentHashMap<>();

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

    public static void onClientTick(){
        mergeImages();
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
        for (int i = 0; i < picturesToRender.size(); i++ ) {
            String pic  = picturesToRender.get(i);
            resolveRepeatedName(i);
            putToImages(parse(pic, i));
        }
    }
    public static void putToImages(ImageEntry imageEntry) {
        if(imageEntry != null) {
            images.put(imageEntry.getName(), imageEntry);
        }
    }
    public static void putToReadyToMerge(ImageEntry imageEntry) {
        if(imageEntry != null) {
            readyToMerge.add(imageEntry);
        }
    }
    public static void mergeImages() {
        for (ImageEntry image : readyToMerge) {
            images.put(image.getName(), image);
        }
        readyToMerge.clear();
    }

    public static Identifier prepareImage(String path, String name) {
        return createTexture(path, name);
    }

    public static void resolveRepeatedName(int index) {
        String picture = picturesToRender.get(index);
        String[] parts = picture.split(";");
        String oldName = parts[1];
        if (images.get(oldName) != null) {
            String newName = oldName + "_";

            String newPath = parts[0] + ";" + newName + ";" + parts[2] + ";" + parts[3] + ";" + parts[4];
            changeMapKey(images, oldName, newName);
            picturesToRender.set(index, newPath);
            LucidityConfig.CONFIG_HANDLER.save();
        }
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
     * Creates a texture from either a URL or a local file path and registers it.
     * @param source The URL (e.g., "https://example.com/image.png") or local file path (e.g., "path/to/image.png")
     * @param name The name for the generated texture (used in Identifier)
     * @return The Identifier of the registered texture
     */
    public static Identifier createTexture(String source, String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();
        Identifier generatedPath = LOST;
        NativeImage image = null;
        try {
            if (source.startsWith("http://") || source.startsWith("https://")) {
                // 处理 URL
                URL imageUrl = new URL(source);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0"); // 模拟浏览器
                connection.setConnectTimeout(5000); // 设置超时

                String contentType = connection.getContentType();
                if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                    LOGGER.warn("URL {} does not point to an image (Content-Type: {})", source, contentType);
                    connection.disconnect();
                    return generatedPath;
                }

                try (InputStream inputStream = connection.getInputStream()) {
                    image = convertToNativeImage(inputStream);
                } finally {
                    connection.disconnect();
                }
            } else {
                // 处理本地文件
                File file = new File(source);
                if (!file.exists() || !file.isFile()) {
                    LOGGER.warn("File {} does not exist or is not a file", source);
                    return generatedPath;
                }
                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                    image = convertToNativeImage(inputStream);
                }
            }

            // 注册纹理
            generatedPath = Identifier.of(MOD_ID, TEMP_TEXTURE_PATH + name);
            Identifier finalGeneratedPath = generatedPath;
            NativeImage finalImage = image;

            MinecraftClient.getInstance().execute(()-> textureManager.registerTexture(finalGeneratedPath, new NativeImageBackedTexture(finalImage)));

        } catch (IOException e) {
            LOGGER.error("Failed to create texture from {}: {}", source, e.getMessage());
            e.printStackTrace();
            Map.entry(null, generatedPath);
        }

        return generatedPath;
    }

    public static Identifier requestIdentifier(ImageEntry imageEntry) {
        return imageEntry.isReady()?imageEntry.getTexture():LOADING;
    }

    private static NativeImage convertToNativeImage(InputStream inputStream) throws IOException {
        // 读取图片为 BufferedImage
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        if (bufferedImage == null) {
            throw new IOException("Unable to decode image");
        }

        // 转换为 PNG 格式的字节流
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", pngOutputStream);
        byte[] pngBytes = pngOutputStream.toByteArray();

        // 将 PNG 字节流转换为 NativeImage
        try (ByteArrayInputStream pngInputStream = new ByteArrayInputStream(pngBytes)) {
            return NativeImage.read(pngInputStream);
        }
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
    public static ImageEntry parse(String picture, int index) {
        String[] parts = picture.split(";");

        if (parts.length < 5) {
            return null;
        }

        String path = parts[0];
        String name = parts[1];

        double[] pos = parseArray(parts[2], 3);
        double[] rotation = parseArray(parts[3], 3);
        double[] scale = parseArray(parts[4], 2);

        ImageEntry initialEntry = new ImageEntry(
                false, // isReady = false
                index,
                name,
                path,
                LOADING,
                pos,
                rotation,
                scale
        ) {
            @Override
            protected void onClicked(ImageConfigScreen imageConfigScreen) {
                System.out.println("You clicked " + this.getName());
                if(imageConfigScreen != null) {
                    imageConfigScreen.currentImage = this;
                    this.setSelected(true);
                    imageConfigScreen.scaleXF.active = true;
                    imageConfigScreen.scaleYF.active = true;
                    imageConfigScreen.posXF.active = true;
                    imageConfigScreen.posYF.active = true;
                    imageConfigScreen.posZF.active = true;
                    imageConfigScreen.rotXF.active = true;
                    imageConfigScreen.rotYF.active = true;
                    imageConfigScreen.rotZF.active = true;
                    imageConfigScreen.nameF.active = true;
                    imageConfigScreen.cancelButton.active = true;
                    imageConfigScreen.saveButton.active = true;
                    imageConfigScreen.moveToPlayerButton.active = true;
                    imageConfigScreen.lookAtPlayerButton.active = true;

                    imageConfigScreen.pathF.setText(this.getPath());

                    imageConfigScreen.rotXF.setValue(mapToZeroOne(Double.parseDouble(String.valueOf(this.getRotation()[0]))));
                    imageConfigScreen.rotYF.setValue(mapToZeroOne(Double.parseDouble(String.valueOf(this.getRotation()[1]))));
                    imageConfigScreen.rotZF.setValue(mapToZeroOne(Double.parseDouble(String.valueOf(this.getRotation()[2]))));

                    imageConfigScreen.scaleXF.setText(String.valueOf(this.getScale()[0]));
                    imageConfigScreen.scaleYF.setText(String.valueOf(this.getScale()[1]));

                    imageConfigScreen.posXF.setText(String.valueOf(this.getPos()[0]));
                    imageConfigScreen.posYF.setText(String.valueOf(this.getPos()[1]));
                    imageConfigScreen.posZF.setText(String.valueOf(this.getPos()[2]));

                    imageConfigScreen.nameF.setText(this.name);

                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                }
            }
        };

        // Start a new thread for the actual processing
        new Thread(() -> {
            try {
                // Generate the ID
                Identifier image = LOST ;
                if (detectMediaType(path).equals(MediaTypeDetector.MediaType.IMAGE)) {
                    image = prepareImage(path, name);
                }

                // Create the final ImageEntry with isReady = true
                ImageEntry finalEntry = new ImageEntry(
                        true,  // isReady = true
                        index,
                        name,
                        path,
                        image,
                        pos,
                        rotation,
                        scale
                ) {
                    @Override
                    protected void onClicked(ImageConfigScreen imageConfigScreen) {
                        System.out.println("You clicked " + this.getName());
                        if(imageConfigScreen != null) {
                            imageConfigScreen.currentImage = this;
                            this.setSelected(true);
                            imageConfigScreen.scaleXF.active = true;
                            imageConfigScreen.scaleYF.active = true;
                            imageConfigScreen.posXF.active = true;
                            imageConfigScreen.posYF.active = true;
                            imageConfigScreen.posZF.active = true;
                            imageConfigScreen.rotXF.active = true;
                            imageConfigScreen.rotYF.active = true;
                            imageConfigScreen.rotZF.active = true;
                            imageConfigScreen.nameF.active = true;
                            imageConfigScreen.cancelButton.active = true;
                            imageConfigScreen.saveButton.active = true;
                            imageConfigScreen.moveToPlayerButton.active = true;
                            imageConfigScreen.lookAtPlayerButton.active = true;

                            imageConfigScreen.pathF.setText(this.getPath());

                            imageConfigScreen.rotXF.setValue(mapToZeroOne(Double.parseDouble(String.valueOf(this.getRotation()[0]))));
                            imageConfigScreen.rotYF.setValue(mapToZeroOne(Double.parseDouble(String.valueOf(this.getRotation()[1]))));
                            imageConfigScreen.rotZF.setValue(mapToZeroOne(Double.parseDouble(String.valueOf(this.getRotation()[2]))));

                            imageConfigScreen.scaleXF.setText(String.valueOf(this.getScale()[0]));
                            imageConfigScreen.scaleYF.setText(String.valueOf(this.getScale()[1]));

                            imageConfigScreen.posXF.setText(String.valueOf(this.getPos()[0]));
                            imageConfigScreen.posYF.setText(String.valueOf(this.getPos()[1]));
                            imageConfigScreen.posZF.setText(String.valueOf(this.getPos()[2]));

                            imageConfigScreen.nameF.setText(this.name);

                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                        }
                    }
                };
                // Put the completed entry into the images list
                images.put(finalEntry.getName(), finalEntry);

            } catch (Exception e) {
                e.printStackTrace();
                // Handle any errors that occur during processing
            }
        }).start();

        // Return the initial "not ready" entry immediately
        return initialEntry;
    }
    public static Vector2d toBlockScale(float ppb, Vector2d scale, Vector2i size) {

        double blocksX = (size.x * scale.x) / ppb;
        double blocksY = (size.y * scale.y) / ppb;

        return new Vector2d(blocksX, blocksY);
    }
}

