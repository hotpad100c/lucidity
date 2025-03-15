package mypals.ml.features.ImageRendering;

import mypals.ml.Lucidity;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;

public class VideoDecoder {
    private final FFmpegFrameGrabber grabber;
    private final Java2DFrameConverter converter;
    private final String videoPath;

    public VideoDecoder(String videoPath) {
        this.videoPath = videoPath;
        this.grabber = new FFmpegFrameGrabber(videoPath);
        this.converter = new Java2DFrameConverter();
        try {
            grabber.start();
        } catch (Exception e) {
            Lucidity.LOGGER.error("Failed to start FFmpegFrameGrabber for {}: {}", videoPath, e.getMessage());
            throw new RuntimeException("Video decoding initialization failed", e);
        }
    }

    public VideoFrame getNextKeyFrame() {
        try {
            Frame frame;
            do {
                frame = grabber.grabKeyFrame(); // 只获取关键帧以减少负担
                if (frame == null) return null;
            } while (frame.image == null); // 跳过非图像帧

            long timestampMs = (long) (grabber.getTimestamp() / 1000.0); // 微秒转毫秒
            BufferedImage image = converter.convert(frame);
            return new VideoFrame(image, timestampMs);
        } catch (Exception e) {
            Lucidity.LOGGER.error("Error grabbing key frame from {}: {}", videoPath, e.getMessage());
            return null;
        }
    }

    public long getDuration() {
        return (long) (grabber.getLengthInTime() / 1000.0);
    }

    public boolean hasNextFrame() {
        try {
            return grabber.getFrameNumber() < grabber.getLengthInFrames();
        } catch (Exception e) {
            return false;
        }
    }

    public void close() {
        try {
            grabber.stop();
            grabber.release();
        } catch (Exception e) {
            Lucidity.LOGGER.error("Error closing video decoder for {}: {}", videoPath, e.getMessage());
        }
    }
}

class VideoFrame {
    private final BufferedImage image;
    private final long timestamp;

    public VideoFrame(BufferedImage image, long timestamp) {
        this.image = image;
        this.timestamp = timestamp;
    }

    public BufferedImage getImage() {
        return image;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
