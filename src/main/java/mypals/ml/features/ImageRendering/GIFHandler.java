package mypals.ml.features.ImageRendering;

import mypals.ml.Lucidity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static mypals.ml.Lucidity.*;
import static mypals.ml.features.ImageRendering.ImageDataParser.LOST;
import static mypals.ml.features.ImageRendering.ImageDataParser.TEMP_TEXTURE_PATH;

public class GIFHandler {
    public static class GifFrameData {
        public List<Identifier> identifiers;
        public List<Integer> delays;

        public GifFrameData(List<Identifier> identifiers, List<Integer> delays) {
            this.identifiers = identifiers;
            this.delays = delays;
        }
    }

    public static GifFrameData createGifTextures(String source, String baseName) {
        List<Identifier> identifiers = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();
        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();

        ImageInputStream imageInputStream = null;
        ImageReader reader = null;
        InputStream inputStream = null;

        try {
            if (source.startsWith("http://") || source.startsWith("https://")) {
                URL imageUrl = new URL(source);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(5000);

                String contentType = connection.getContentType();
                if (contentType == null || !contentType.toLowerCase().contains("gif")) {
                    LOGGER.warn("URL {} 不指向 GIF 文件 (Content-Type: {})", source, contentType);
                    identifiers.add(LOST);
                    connection.disconnect();
                    return new GifFrameData(identifiers, delays);
                }

                inputStream = new BufferedInputStream(connection.getInputStream());
                byte[] data = inputStream.readAllBytes();
                imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
                connection.disconnect();
            } else {
                File file = new File(source);
                if (!file.exists() || !file.isFile()) {
                    LOGGER.warn("文件 {} 不存在或不是文件", source);
                    identifiers.add(LOST);
                    return new GifFrameData(identifiers, delays);
                }
                inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));
                byte[] data = inputStream.readAllBytes();
                imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
            }

            reader = getGifReader(imageInputStream);
            if (reader == null) {
                identifiers.add(LOST);
                return new GifFrameData(identifiers, delays);
            }

            int frameCount = reader.getNumImages(true);
            BufferedImage previousFrame = null;

            for (int i = 0; i < frameCount; i++) {
                BufferedImage frame = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);
                String metaFormat = metadata.getNativeMetadataFormatName();
                IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);

                IIOMetadataNode imgDescr = (IIOMetadataNode) root.getElementsByTagName("ImageDescriptor").item(0);
                int x = Integer.parseInt(imgDescr.getAttribute("imageLeftPosition"));
                int y = Integer.parseInt(imgDescr.getAttribute("imageTopPosition"));

                IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
                String disposalMethod = gce.getAttribute("disposalMethod");
                int delay = Integer.parseInt(gce.getAttribute("delayTime")) * 10;

                BufferedImage fullFrame = mergeFrames(previousFrame, frame, disposalMethod, reader, i, x, y);
                NativeImage nativeImage = convertBufferedImageToNativeImage(fullFrame);
                Identifier frameIdentifier = Identifier.of(MOD_ID, TEMP_TEXTURE_PATH + baseName + "_frame_" + i);
                identifiers.add(frameIdentifier);
                delays.add(delay);

                if (!disposalMethod.equals("restoreToBackgroundColor")) {
                    previousFrame = copyBufferedImage(fullFrame); // 保留当前帧作为下一帧的基础
                } else {
                    previousFrame = null;
                }

                NativeImage finalNativeImage = nativeImage;
                Identifier finalFrameIdentifier = frameIdentifier;
                MinecraftClient.getInstance().execute(() -> {
                    textureManager.registerTexture(finalFrameIdentifier, new NativeImageBackedTexture(finalNativeImage));
                });
            }

        } catch (IOException e) {
            LOGGER.error("无法从 {} 创建 GIF 纹理: {}", source, e.getMessage());
            e.printStackTrace();
            identifiers.add(LOST);
        } finally {
            try {
                if (reader != null) reader.dispose();
                if (imageInputStream != null) imageInputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                LOGGER.error("关闭资源时出错: {}", e.getMessage());
            }
        }

        if (identifiers.isEmpty()) {
            identifiers.add(LOST);
        }
        return new GifFrameData(identifiers, delays);
    }

    private static ImageReader getGifReader(ImageInputStream imageInputStream) {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        if (!readers.hasNext()) {
            LOGGER.error("未找到 GIF的ImageReader");
            return null;
        }
        ImageReader reader = readers.next();
        reader.setInput(imageInputStream);
        return reader;
    }

    private static BufferedImage mergeFrames(BufferedImage previousFrame, BufferedImage currentFrame, String disposalMethod, ImageReader reader, int index, int x, int y) throws IOException {
        int width = reader.getWidth(0);
        int height = reader.getHeight(0);
        BufferedImage fullFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        if (previousFrame != null && !disposalMethod.equals("restoreToBackgroundColor")) {
            fullFrame.getGraphics().drawImage(previousFrame, 0, 0, null);
        } else if (disposalMethod.equals("restoreToBackgroundColor")) {
            IIOMetadata metadata = reader.getStreamMetadata();
            if (metadata != null) {
                IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
                IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GlobalColorTable").item(0);
                if (gce != null) {
                    fullFrame.getGraphics().setColor(new java.awt.Color(0, 0, 0, 0)); // 默认透明
                    fullFrame.getGraphics().fillRect(0, 0, width, height);
                }
            }
        }

        fullFrame.getGraphics().drawImage(currentFrame, x, y, null);
        return fullFrame;
    }

    private static BufferedImage copyBufferedImage(BufferedImage source) {
        if (source == null) return null;
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage(source, 0, 0, null);
        return copy;
    }

    private static NativeImage convertBufferedImageToNativeImage(BufferedImage bufferedImage) {
        NativeImage nativeImage = new NativeImage(
                NativeImage.Format.RGBA,
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                false
        );

        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                int argb = bufferedImage.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                nativeImage.setColor(x, y, abgr);
            }
        }

        return nativeImage;
    }
}
