package mypals.ml.features.ImageRendering;

import mypals.ml.Lucidity;
import mypals.ml.features.ImageRendering.configuration.MediaEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.DynamicTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static mypals.ml.features.ImageRendering.ImageDataParser.LOADING;

public class VideoHandler {
    private final List<Identifier> frameTextures = new ArrayList<>();
    private final List<Long> frameTimestamps = new ArrayList<>();
    private final long duration;
    private final MediaEntry mediaEntry;
    private boolean isPreloading = false;
    private boolean initializationFailed = false;

    public VideoHandler(MediaEntry mediaEntry) {
        this.mediaEntry = mediaEntry;
        this.duration = initializeVideo();
    }

    private long initializeVideo() {
        AtomicLong videoDuration = new AtomicLong();
        CompletableFuture.runAsync(() -> {
            try {
                isPreloading = true;
                VideoDecoder decoder = new VideoDecoder(mediaEntry.getPath());
                videoDuration.set(decoder.getDuration());

                while (decoder.hasNextFrame()) {
                    VideoFrame frame = decoder.getNextKeyFrame();
                    if (frame == null) break;
                    Identifier texture = convertToMinecraftTexture(frame);
                    frameTextures.add(texture);
                    frameTimestamps.add(frame.getTimestamp());
                }
                decoder.close();
            } catch (Exception e) {
                Lucidity.LOGGER.error("Failed to preload video {}: {}", mediaEntry.getPath(), e.getMessage());
                initializationFailed = true;
            } finally {
                isPreloading = false;
            }
        });
        return videoDuration.get(); // 返回视频时长，失败时为 0
    }
    private Identifier convertToMinecraftTexture(VideoFrame frame) {
        BufferedImage image = frame.getImage();
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                nativeImage.setColor(x, y, image.getRGB(x, y));
            }
        }
        NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
        return MinecraftClient.getInstance().getTextureManager()
                .registerDynamicTexture("video_frame_" + frame.getTimestamp(), texture);
    }

    public Identifier getFrameAtTime(long timeMs) {
        if (initializationFailed || isPreloading || frameTimestamps.isEmpty()) {
            return LOADING;
        }
        int index = binarySearchFrame(timeMs);
        return frameTextures.get(index);
    }

    private int binarySearchFrame(long timeMs) {
        int left = 0;
        int right = frameTimestamps.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            long midTime = frameTimestamps.get(mid);
            if (midTime == timeMs) return mid;
            if (midTime < timeMs) left = mid + 1;
            else right = mid - 1;
        }
        return right < 0 ? 0 : right; // 返回最接近的较早帧
    }

    public long getDuration() {
        return duration;
    }

    public Identifier getFrameTexture(int index) {
        return frameTextures.get(Math.min(index, frameTextures.size() - 1));
    }
}

