package mypals.ml.features.ImageRendering;
import mypals.ml.Lucidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;


public class MediaTypeDetector {
    public enum MediaType {
        IMAGE, GIF, VIDEO, UNKNOWN
    }

    /**
     * 判断文件是图片、GIF 还是视频
     * @param source 本地路径或 URL
     * @return MediaType 枚举值
     */
    public static MediaType detectMediaType(String source) {
        // 初步通过扩展名判断
        MediaType typeByExtension = guessMediaTypeByExtension(source);
        Lucidity.LOGGER.debug("Guessed media type by extension for {}: {}", source, typeByExtension);

        try {
            if (source.startsWith("http://") || source.startsWith("https://")) {
                // 处理 URL
                URL url = new URL(source);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(5000);

                // 检查 Content-Type
                String contentType = connection.getContentType();
                MediaType typeByContentType = guessMediaTypeByContentType(contentType);
                Lucidity.LOGGER.debug("Guessed media type by Content-Type for {}: {}", source, typeByContentType);

                // 如果 Content-Type 明确是视频或 GIF，直接返回
                if (typeByContentType == MediaType.VIDEO || typeByContentType == MediaType.GIF) {
                    connection.disconnect();
                    return typeByContentType;
                }

                // 进一步检查文件头
                try (InputStream inputStream = connection.getInputStream()) {
                    MediaType typeByHeader = detectMediaTypeByHeader(inputStream);
                    Lucidity.LOGGER.debug("Detected media type by header for {}: {}", source, typeByHeader);
                    return typeByHeader;
                } finally {
                    connection.disconnect();
                }
            } else {
                // 处理本地文件
                File file = new File(source);
                if (!file.exists() || !file.isFile()) {
                    Lucidity.LOGGER.warn("File {} does not exist or is not a file", source);
                    return MediaType.UNKNOWN;
                }
                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                    MediaType typeByHeader = detectMediaTypeByHeader(inputStream);
                    Lucidity.LOGGER.debug("Detected media type by header for {}: {}", source, typeByHeader);
                    return typeByHeader;
                }
            }
        } catch (IOException e) {
            Lucidity.LOGGER.error("Failed to detect media type for {}: {}", source, e.getMessage());
            return MediaType.UNKNOWN;
        }
    }

    private static MediaType guessMediaTypeByExtension(String source) {
        String lowerSource = source.toLowerCase();
        if (lowerSource.endsWith(".png") || lowerSource.endsWith(".jpg") || lowerSource.endsWith(".jpeg") ||
                lowerSource.endsWith(".webp") || lowerSource.endsWith(".bmp")) {
            return MediaType.IMAGE;
        } else if (lowerSource.endsWith(".gif")) {
            return MediaType.GIF;
        } else if (lowerSource.endsWith(".mp4") || lowerSource.endsWith(".avi") || lowerSource.endsWith(".mov")) {
            return MediaType.VIDEO;
        } else {
            return MediaType.UNKNOWN;
        }
    }

    private static MediaType guessMediaTypeByContentType(String contentType) {
        if (contentType == null) {
            return MediaType.UNKNOWN;
        }
        contentType = contentType.toLowerCase();
        if (contentType.contains("image/gif")) {
            return MediaType.GIF;
        } else if (contentType.contains("image/")) {
            return MediaType.IMAGE;
        } else if (contentType.contains("video/")) {
            return MediaType.VIDEO;
        } else {
            return MediaType.UNKNOWN;
        }
    }

    private static MediaType detectMediaTypeByHeader(InputStream inputStream) throws IOException {
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }
        inputStream.mark(32);
        byte[] header = new byte[32];
        int bytesRead = inputStream.read(header);
        inputStream.reset();

        if (bytesRead < 8) {
            return MediaType.UNKNOWN;
        }

        // GIF: 47 49 46 38 (GIF8)
        if (header[0] == (byte) 0x47 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x38) {
            return MediaType.GIF;
        }

        // 图片格式
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
            return MediaType.IMAGE;
        }
        // JPEG: FF D8
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
            return MediaType.IMAGE;
        }
        // WebP: 52 49 46 46 xxxx 57 45 42 50 (RIFF xxxx WEBP)
        if (header[0] == (byte) 0x52 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x46 &&
                header[8] == (byte) 0x57 && header[9] == (byte) 0x45 && header[10] == (byte) 0x42 && header[11] == (byte) 0x50) {
            return MediaType.IMAGE;
        }
        // BMP: 42 4D (BM)
        if (header[0] == (byte) 0x42 && header[1] == (byte) 0x4D) {
            return MediaType.IMAGE;
        }

        // 视频格式
        // MP4/MOV: 00 00 00 xx 66 74 79 70 (ftyp)
        if (header[4] == (byte) 0x66 && header[5] == (byte) 0x74 && header[6] == (byte) 0x79 && header[7] == (byte) 0x70) {
            return MediaType.VIDEO;
        }
        // AVI: 52 49 46 46 xxxx 41 56 49 (RIFF xxxx AVI)
        if (header[0] == (byte) 0x52 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x46 &&
                header[8] == (byte) 0x41 && header[9] == (byte) 0x56 && header[10] == (byte) 0x49) {
            return MediaType.VIDEO;
        }

        return MediaType.UNKNOWN;
    }
}