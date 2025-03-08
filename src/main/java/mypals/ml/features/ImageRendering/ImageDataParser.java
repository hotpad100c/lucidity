package mypals.ml.features.ImageRendering;

import mypals.ml.config.LucidityConfig;
import mypals.ml.features.ImageRendering.configuration.ImageEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
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

public class ImageDataParser {

    public static final String TEMP_TEXTURE_PATH = "textures/temp/";
    private static final String GENERATED_PATH = "assets/" + MOD_ID + "/textures/generated/";
    private static final Identifier LOST = Identifier.of(MOD_ID, "textures/lost-file.png");

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

            ImageEntry entry = parse(pic, i);
            if(entry != null)
                images.put(entry.name, entry);
        }
    }

    public static Identifier prepareImage(String path, String name) {
        Identifier imageCreatedId = createTexture(path, name);
        return imageCreatedId;
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

        try {
            NativeImage image;
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
            textureManager.registerTexture(generatedPath, new NativeImageBackedTexture(image));
        } catch (IOException e) {
            LOGGER.error("Failed to create texture from {}: {}", source, e.getMessage());
            e.printStackTrace();
        }

        return generatedPath;
    }

    public static Identifier requestIdentifier(ImageEntry imageEntry) {
        return imageEntry.isReady()?imageEntry.getTexture():LOST;
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
        if (parts.length != 5) {
            return null;
        }

        String path = parts[0];
        String name = parts[1];
        double[] pos = parseArray(parts[2], 3);
        double[] rotation = parseArray(parts[3], 3);
        double[] scale = parseArray(parts[4], 2);
        Identifier generatedID = LOST;
        if(detectMediaType(path).equals(MediaTypeDetector.MediaType.IMAGE))
            generatedID = prepareImage(path, name);

        return new ImageEntry(true, index, name, path, generatedID, pos, rotation, scale) {
            @Override
            protected void onClicked() {

            }
        };
    }
    public static String detectImageFormat(InputStream inputStream) throws IOException {
        // 标记流以便重置
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }
        inputStream.mark(32); // 标记前 32 字节，足够读取文件头

        byte[] header = new byte[32];
        int bytesRead = inputStream.read(header);
        inputStream.reset(); // 重置流以便后续使用

        if (bytesRead < 8) {
            return "UNKNOWN"; // 文件太小，无法判断
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
            return "PNG";
        }

        // JPEG: FF D8
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
            return "JPEG";
        }

        // WebP: RIFF xxxx WEBP
        if (header[0] == (byte) 0x52 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x46 &&
                header[8] == (byte) 0x57 && header[9] == (byte) 0x45 && header[10] == (byte) 0x42 && header[11] == (byte) 0x50) {
            return "WEBP";
        }

        // SVG: 检查前几个字节是否以 <?xml 或 <svg 开头
        String headerStr = new String(header, 0, Math.min(bytesRead, 16), "UTF-8").toLowerCase();
        if (headerStr.startsWith("<?xml") || headerStr.startsWith("<svg")) {
            return "SVG";
        }

        return "UNKNOWN";
    }
    public static Vector2d toBlockScale(float ppb, Vector2d scale, Vector2i size) {

        double blocksX = (size.x * scale.x) / ppb;
        double blocksY = (size.y * scale.y) / ppb;

        return new Vector2d(blocksX, blocksY);
    }
}

