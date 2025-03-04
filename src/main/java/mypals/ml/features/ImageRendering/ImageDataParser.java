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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
    public static ConcurrentHashMap<String, Map.Entry<Identifier,ImageData>> images = new ConcurrentHashMap<>();
    public static class ImageData {
        public int index;
        public String path;
        public String name;
        public double[] pos;
        public double[] rotation;
        public double[] scale;
        //public Vector2d orgSize;

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
    public static void prepareImages() {

        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();

        for (String nickName : images.keySet()) {
            textureManager.destroyTexture(Identifier.of(MOD_ID, TEMP_TEXTURE_PATH + nickName));
        }
        images.clear();
        for (String pic : picturesToRender) {
            ImageDataParser.ImageData data = parse(pic, picturesToRender.indexOf(pic));
            if (data != null) {
                if(data.getPath().startsWith("https://")){
                    Map.Entry<String,ImageData> converted = prepareOnlineImage(pic,data);
                    if(converted!=null){
                        prepareLocalImage(converted.getKey(),converted.getValue());
                    }
                }else{
                    prepareLocalImage(pic, data);
                }
            }
        }
    }
    public static void prepareLocalImage(String pic, ImageData data){
        Random random = new Random();
        if (images.get(data.getName()) != null) {
            String oldName = data.getName();
            data.name = data.getName() + random.nextInt();
            picturesToRender.set(picturesToRender.indexOf(pic), data.toString());
            changeMapKey(images,oldName,data.name);
            LucidityConfig.CONFIG_HANDLER.save();
        }
        Identifier imageCreatedId = createTexture(data.getPath(), data.getName());
        Map.Entry<Identifier, ImageData> imageSet = Map.entry(imageCreatedId, data);

        images.put(data.getName(), imageSet);
    }
    public static boolean downloadPicture(ImageData data){
        Path gameDir = FabricLoader.getInstance().getGameDir();
        File generatedDir = new File(String.valueOf(gameDir), GENERATED_PATH);
        File file = new File(generatedDir.getPath()+"/"+data.getName() + ".png");
        if(!file.exists()){
            try {
                // 创建 URL 对象
                URL url = new URL(data.getPath());
                String destinationFile = generatedDir +"/"+ data.getName() + ".png";
                // 打开输入流
                InputStream inputStream = url.openStream();

                // 读取输入流中的图像数据
                BufferedImage image = ImageIO.read(inputStream);

                // 关闭输入流
                inputStream.close();

                // 将图像写入本地文件
                ImageIO.write(image, "png", new File(destinationFile));
                System.out.println("图片已经保存到: " + destinationFile);
                return true;
            } catch (IOException e) {
                System.err.println("下载图片时发生错误: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }else{
            return true;
        }
    }
    public static Map.Entry<String,ImageData> prepareOnlineImage(String pic, ImageData data){
        Random random = new Random();
        if (images.get(data.getName()) != null) {
            String oldName = data.getName();
            data.name = data.getName() + random.nextInt();
            picturesToRender.set(picturesToRender.indexOf(pic), data.toString());
            changeMapKey(images,oldName,data.name);
            LucidityConfig.CONFIG_HANDLER.save();
        }

        Path gameDir = FabricLoader.getInstance().getGameDir();
        File generatedDir = new File(String.valueOf(gameDir), GENERATED_PATH);
        String destinationFile = generatedDir +"/"+ data.getName() + ".png";

        if(downloadPicture(data)){
            data.path = destinationFile;
            picturesToRender.set(picturesToRender.indexOf(pic), data.toString());
            pic = data.toString();

            Identifier imageCreatedId = createTexture(destinationFile, data.getName());
            Map.Entry<Identifier, ImageData> imageSet = Map.entry(imageCreatedId, data);
            images.put(data.name,imageSet);
            return Map.entry(pic,data);
        }
        images.put(data.name,Map.entry(Identifier.of(MOD_ID,"textures/lost-file.png"), data));
        return null;
    }
    public static <K, V> void changeMapKey(Map<K, V> map, K oldKey, K newKey) {
        if (map.containsKey(oldKey)) {
            V value = map.remove(oldKey);
            map.put(newKey, value);
        }
    }
    public static Identifier createTexture(String texturePath,String nickName){
        File file = new File(texturePath);
        Path gameDir = FabricLoader.getInstance().getGameDir();

        File generatedDir = new File(String.valueOf(gameDir), GENERATED_PATH);
        if (!generatedDir.exists()) {
            generatedDir.mkdirs();
        }
        if (!file.exists()) {
            return Identifier.of(MOD_ID,"textures/lost-file.png");
        }
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();
        //String textureId = file.getName();
        try {
            NativeImage image = NativeImage.read(Files.newInputStream(file.toPath()));
            textureManager.registerTexture(Identifier.of(MOD_ID, TEMP_TEXTURE_PATH+nickName), new NativeImageBackedTexture(image));
        }catch (Exception e){
            LOGGER.info(e.toString());
        }
        return Identifier.of(MOD_ID, TEMP_TEXTURE_PATH+nickName);
    }
    private static double[] parseArray(String arrayString, int expectCount) {
        String[] elements = arrayString.replaceAll("[\\[\\]]", "").split(",");
        return Arrays.copyOf(
                Arrays.stream(elements)
                        .mapToDouble(Double::parseDouble)
                        .toArray(),
                expectCount
        );
    }
    public static ImageData parse(String input, int index) {
        String[] parts = input.split(";");
        if (parts.length != 5) {
            return null;
        }

        String path = parts[0];
        String name = parts[1];
        double[] pos = parseArray(parts[2],3);
        double[] rotation = parseArray(parts[3],3);
        double[] scale = parseArray(parts[4],2);

        return new ImageData(index, path, name, pos, rotation, scale);
    }
    public static void test() {
        /*String input = "<path/to/image>;<image_name>;[1,2,3];[4,5,6];[7,8]";
        ImageData imageData = parse(input);
        System.out.println(imageData);*/
    }
}
