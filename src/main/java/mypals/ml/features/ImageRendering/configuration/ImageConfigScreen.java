package mypals.ml.features.ImageRendering.configuration;

import mypals.ml.config.LucidityConfig;
import mypals.ml.features.ImageRendering.ITextureManagerMixin;
import mypals.ml.features.ImageRendering.ImageDataParser;
import mypals.ml.features.ImageRendering.ImageRenderer;
import mypals.ml.features.ImageRendering.MediaTypeDetector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static mypals.ml.Lucidity.MOD_ID;
import static mypals.ml.config.LucidityConfig.picturesToRender;
import static mypals.ml.features.ImageRendering.ImageDataParser.*;

public class ImageConfigScreen extends Screen {
    protected int x = 1;
    protected int y = 1;

    private float rotX = 0;
    private float rotY = 0;
    private float rotZ = 0;

    public TextFieldWidget pathF;
    public TextFieldWidget nameF;

    public TextFieldWidget posXF;
    public TextFieldWidget posYF;
    public TextFieldWidget posZF;

    public TextFieldWidget scaleXF;
    public TextFieldWidget scaleYF;

    public SliderWidget rotXF;
    public SliderWidget rotYF;
    public SliderWidget rotZF;

    private float scaleX = 1;
    private float scaleY = 1;

    private final int PREVIEW_SCALE = 5;

    public MediaEntry currentImage;
    private ImageConfigScreen instance;
    public ScrollableWidget scrollableWidget1;
    public ButtonWidget saveButton;
    public ButtonWidget lookAtPlayerButton;
    public ButtonWidget moveToPlayerButton;
    public ButtonWidget cancelButton;
    private static final Identifier BG_TEXTURE = Identifier.of(MOD_ID,"textures/gui/config/image_config.png");
    public ImageConfigScreen(Text title) {
        super(title);
    }

    public static double mapToZeroOne(double v){
        return v/360;
    }
    public double[] getNormalizedSize(double orgWidth, double orgHeight, double scaleX, double scaleY){
        double maxTargetDimension = Math.max(scaleX, scaleY);
        double normalizedTargetWidth = scaleX / maxTargetDimension;
        double normalizedTargetHeight = scaleY / maxTargetDimension;

        double[] output = new double[2];
        output[0] = normalizedTargetWidth;
        output[1] = normalizedTargetHeight;
        return output;
    }
    @Override
    protected void init() {
        setInstance(this);
        for (int i = 0; i < ImageDataParser.images.size(); i++) {
            Map.Entry<String, MediaEntry> entry = ImageDataParser.images.entrySet().stream().toList().get(i);
            ImageDataParser.images.put(entry.getKey(), entry.getValue());

        }

        this.addDrawableChild(scrollableWidget1 = new ScrollableWidget(0, 5, this.width - (this.width/2),this.height-10,ScreenTexts.EMPTY) {
            final int boxWidth = this.width - 10;
            final int boxHeight = 50;
            final int spacing = 5;
            @Override
            protected int getContentsHeightWithPadding() {
                return (boxHeight + spacing) * ImageDataParser.images.size() - spacing;
            }

            @Override
            protected double getDeltaYPerScroll() {
                return 0;
            }

            @Override
            protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                int index = 0;
                // 调整鼠标Y坐标以考虑滚动
                double adjustedMouseY = mouseY + this.getScrollY();

                for (MediaEntry entry : ImageDataParser.images.values()) {
                    int x = 5;
                    int y = boxHeight / 4 + (boxHeight + spacing) * index;
                    int cx = (boxWidth) / 2;
                    int cy = y + 30;

                    int[] pictureSize = getTextureSize(requestIdentifier(entry));

                    double[] normalizedScale = getNormalizedSize(pictureSize[0], pictureSize[1],
                            entry.getScale()[0],entry.getScale()[1]);

                    float ppb = pictureSize[0] / (float) PREVIEW_SCALE;

                    try {
                        ImageRenderer.renderPicture(context.getMatrices(), requestIdentifier(entry),
                                new Vec3d(cx + (cx / 1.5) - 10, cy-5, 10),
                                new Vec3d(0, 180, 180),
                                new Vector2d(normalizedScale[0] * 9, normalizedScale[1] * 9),
                                ppb, 15720000, OverlayTexture.DEFAULT_UV, (int) delta,true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    boolean isMouseOver = mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight;

                    context.fill(x, y, x + boxWidth, y + boxHeight, entry.isSelected()? 0x19E0E0E0 : 0x19000000);

                    int borderColor = isMouseOver ? Color.WHITE.getRGB() : Color.GRAY.getRGB(); // 鼠标悬停时变黄
                    context.drawBorder(x, y, boxWidth, boxHeight, borderColor);

                    context.drawText(MinecraftClient.getInstance().textRenderer, entry.getName(), x + 5, y + 5, 0xFFFFFF, false);

                    context.drawText(MinecraftClient.getInstance().textRenderer, entry.getType().getKey(), x + 5, y + 20, 0xFFFFFF, false);

                    Vector2d blockScale = toBlockScale(LucidityConfig.pixelsPerBlock, new Vector2d(entry.getScale()[0],entry.getScale()[1]), new Vector2i(pictureSize[0], pictureSize[1]));

                    context.drawText(MinecraftClient.getInstance().textRenderer, "[ " + String.format("%.2f", blockScale.x) + ", " + String.format("%.2f", blockScale.y) + " ] Block(s)", x + 5, y+40, 0xFFFFFF, false);


                    index++;
                }
            }
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                int index = 0;


                double adjustedMouseY = mouseY + this.getScrollY();

                for (MediaEntry entry : ImageDataParser.images.values()) {
                    entry.setSelected(false);
                    int x = 5;
                    int y = boxHeight / 4 + (boxHeight + spacing) * index;
                    if (mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight) {
                        entry.onClicked(getInstance());
                        return true;
                    }
                    index++;
                }

                return super.mouseClicked(mouseX, mouseY, button);
            }
            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {

            }
            
        });

        this.addDrawableChild(saveButton = ButtonWidget.builder(Text.literal("Save"), button -> {
                    saveData();
        })
                .dimensions(this.width / 2 + 10 , this.height - (this.height - 50), 100, 20)
                .tooltip(Tooltip.of(Text.literal("Save")))
                .build());
        this.addDrawableChild(cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button -> {
                    super.close();
                })
                .dimensions(this.width / 2 + 10 , this.height - (this.height - 75), 100, 20)
                .tooltip(Tooltip.of(Text.literal("Cancel")))
                .build());
        this.addDrawableChild(lookAtPlayerButton = ButtonWidget.builder(Text.literal("LookToPlayer"), button -> {
                    float yaw = client.player.getYaw() % 360;
                    yaw = yaw < 0 ? yaw + 360 : yaw;

                    float pitch = client.player.getPitch() % 360;
                    pitch = pitch < 0 ? pitch + 360 : pitch;

                    rotXF.setValue(mapToZeroOne(pitch));
                    rotYF.setValue(mapToZeroOne(yaw));

                })
                .dimensions(this.width / 2 + 10 , this.height - (this.height - 100), 100, 20)
                .tooltip(Tooltip.of(Text.literal("LookToPlayer")))
                .build());
        this.addDrawableChild(moveToPlayerButton = ButtonWidget.builder(Text.literal("MoveToPlayer"), button -> {
            Vec3d pos = client.player.getPos();
            posXF.setText(String.valueOf(pos.x));
                    posYF.setText(String.valueOf(pos.y));
                    posZF.setText(String.valueOf(pos.z));
        })
                .dimensions(this.width / 2 + 10 , this.height - (this.height - 125), 100, 20)
                .tooltip(Tooltip.of(Text.literal("MoveToPlayer")))
                .build());

        this.addDrawableChild(pathF = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                this.width / 2 + 10, this.height - (this.height -10), 100, 15,
                Text.literal("")));
        pathF.setMaxLength(1145141);
        this.addDrawableChild(nameF = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                this.width / 2 + 115, this.height - (this.height -10), 100, 15,
                Text.literal("")));
        this.addDrawableChild(scaleXF = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                this.width / 2 + 10, this.height / 2 + 40, 100, 10,
                Text.literal("1")));
        this.addDrawableChild(scaleYF = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                this.width / 2 + 10, this.height / 2 + 55, 100, 10,
                Text.literal("1")));

        this.addDrawableChild(posXF = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                this.width / 2 + 120, this.height / 2 + 25, 100, 10,
                Text.literal("")));
        this.addDrawableChild(posYF = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                this.width / 2 + 120, this.height / 2 + 40, 100, 10,
                Text.literal("")));
        this.addDrawableChild(posZF = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                this.width / 2 + 120, this.height / 2 + 55, 100, 10,
                Text.literal("")));

        setUpListeners();
        this.addDrawableChild(rotXF = new SliderWidget(this.width / 2 + 10, this.height / 2 + 70, 200, 10, ScreenTexts.EMPTY, 0.0) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                rotX = (float) mapValueToDegree(value);
                this.setMessage(Text.translatable("Rotation X : " + rotX));
                //this.setMessage(Text.translatable("jigsaw_block.levels", JigsawBlockScreen.this.generationDepth));
            }

            @Override
            protected void applyValue() {
                //JigsawBlockScreen.this.generationDepth = MathHelper.floor(MathHelper.clampedLerp(0.0, 20.0, this.value));
            }
        });
        this.addDrawableChild(rotYF = new SliderWidget(this.width / 2 + 10, this.height / 2 + 85, 200, 10, ScreenTexts.EMPTY, 0.0) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                rotY = (float) mapValueToDegree(value);
                this.setMessage(Text.translatable("Rotation Y : " + rotY));
                //this.setMessage(Text.translatable("jigsaw_block.levels", JigsawBlockScreen.this.generationDepth));
            }

            @Override
            protected void applyValue() {
                //JigsawBlockScreen.this.generationDepth = MathHelper.floor(MathHelper.clampedLerp(0.0, 20.0, this.value));
            }
        });
        this.addDrawableChild(rotZF = new SliderWidget(this.width / 2 + 10, this.height / 2 + 100, 200, 10, ScreenTexts.EMPTY, 0.0) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                rotZ = (float) mapValueToDegree(value);
                this.setMessage(Text.translatable("Rotation Z : " + rotZ));
                //this.setMessage(Text.translatable("jigsaw_block.levels", JigsawBlockScreen.this.generationDepth));
            }

            @Override
            protected void applyValue() {
                //JigsawBlockScreen.this.generationDepth = MathHelper.floor(MathHelper.clampedLerp(0.0, 20.0, this.value));
            }
        });
        scaleXF.active = false;
        scaleYF.active = false;
        posXF.active = false;
        posYF.active = false;
        posZF.active = false;
        rotXF.active = false;
        rotYF.active = false;
        rotZF.active = false;
        nameF.active = false;
        //cancelButton.active = false;
        saveButton.active = false;
        moveToPlayerButton.active = false;
        lookAtPlayerButton.active = false;

    }
    public ImageConfigScreen getIncetanse(){
        return this;
    }
    public int[] getTextureSize(Identifier textureId){
        AbstractTexture texture = client.getTextureManager().getTexture(textureId);
        int[] size = new int[2];
        size[0] = 16;
        size[1] = 16;

        if(texture instanceof NativeImageBackedTexture nativeTexture) {
            NativeImage image = nativeTexture.getImage();
            assert image != null;
            size[0] = image.getWidth();
            size[1] = image.getHeight();
        }else {
            try {
                Optional<NativeImage> imageOptional = client.getResourceManager()
                        .getResource(textureId)
                        .map(resource -> {
                            try (var inputStream = resource.getInputStream()) {
                                return NativeImage.read(inputStream);
                            } catch (IOException e) {
                                return null;
                            }
                        });
                if (imageOptional.isPresent()) {
                    NativeImage image = imageOptional.get();
                    size[0] = image.getWidth();
                    size[1] = image.getHeight();
                    image.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }
    public void setUpListeners(){
        scaleXF.setChangedListener(s -> {
            float oldScale = scaleX;
            try {
                if (!s.isEmpty() && !s.isBlank())
                    scaleX = Float.parseFloat(s);
                else
                    scaleX = 1;
            }catch (Exception e){
                scaleX = oldScale;
            }
        });
        scaleYF.setChangedListener(s -> {
            float oldScale = scaleY;
            try {
                if (!s.isEmpty() && !s.isBlank())
                    scaleY = Float.parseFloat(s);
                else
                    scaleY = 1;
            }catch (Exception e){
                scaleY = oldScale;
            }
        });

    }
    public static double mapValueToDegree(double v) {
        return (int)(v * 360);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        //renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Scale"),this.width / 2 + 23, this.height / 2 + 30, 0xffffff);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Position"),this.width / 2 + 140, this.height / 2 + 15, 0xffffff);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Path"),this.width / 2 + 23, this.height - (this.height -30), 0xffffff);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Name"),this.width / 2 + 125, this.height - (this.height -30), 0xffffff);


    }
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY,float delta) {
        int cx = (this.width) / 2;
        int cy = (this.height) / 2;
        this.applyBlur();
        context.getMatrices().push();
        context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        if(currentImage != null) {
            Identifier texturePath = requestIdentifier(images.get(currentImage.getName()));


            double[] normalizedScale = getNormalizedSize(getTextureSize(texturePath)[0], getTextureSize(texturePath)[1],
                    scaleX, scaleY);

            float ppb = getTextureSize(texturePath)[0] / (float) PREVIEW_SCALE;

            try {
                ImageRenderer.renderPicture(context.getMatrices(), texturePath
                        , new Vec3d(cx + (cx / 1.45), cy - 30, 1000)
                        , new Vec3d(rotX, rotY+180, rotZ + 180)
                        , new Vector2d(normalizedScale[0] * 20, normalizedScale[1] * 20),
                        ppb, 15720000, OverlayTexture.DEFAULT_UV, (int) delta,true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        context.getMatrices().pop();
    }
    public ImageConfigScreen getInstance(){
        return this;
    }
    public void reParse(){
        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();

        ImageData data = new ImageData(currentImage.getIndex(),
                pathF.getText(),
                nameF.getText(),
                currentImage.getPos(),
                currentImage.getRotation(),
                currentImage.getScale()
        );
        Identifier[] oldIds = currentImage.textureIDs;
        Identifier newID = LOADING;

        parse(data.toString(),currentImage.getIndex());

        currentImage.textureIDs = new Identifier[]{newID};
        currentImage.ready = false;
        images.get(currentImage.name).textureIDs = new Identifier[]{newID};
        images.get(currentImage.name).ready = false;
        for(Identifier id : oldIds){
            textureManager.destroyTexture(id);
        }
    }
    public void saveData(){

        if(currentImage != null){
            String origName = currentImage.getName();
            if(origName != nameF.getText()){
                images.get(origName).name = nameF.getText();
                currentImage.name = nameF.getText();
                changeMapKey(images, origName, nameF.getText());
                currentImage.index = images.get(nameF.getText()).getIndex();
                reParse();
            }
            if(currentImage.path != pathF.getText()){

                reParse();
                images.get(currentImage.name).path = pathF.getText();
            }

            currentImage.path = pathF.getText();

            double[] pos = new double[3];
            double[] rot = new double[3];
            double[] scal = new double[2];

            pos[0] = Double.parseDouble(posXF.getText().isEmpty()?currentImage.getPos()[0]+"":posXF.getText());
            pos[1] = Double.parseDouble(posYF.getText().isEmpty()?currentImage.getPos()[1]+"":posYF.getText());
            pos[2] = Double.parseDouble(posZF.getText().isEmpty()?currentImage.getPos()[2]+"":posZF.getText());

            rot[0] = rotX;
            rot[1] = rotY;
            rot[2] = rotZ;

            scal[0] = Double.parseDouble(scaleXF.getText().isEmpty()?currentImage.getScale()[0]+"":scaleXF.getText());
            scal[1] = Double.parseDouble(scaleYF.getText().isEmpty()?currentImage.getScale()[0]+"":scaleYF.getText());

            ImageDataParser.ImageData newData = new ImageDataParser.ImageData(currentImage.getIndex(),
                    pathF.getText(),
                    nameF.getText(),
                    pos,
                    rot,
                    scal
            );

            images.get(nameF.getText()).pos = pos;
            images.get(nameF.getText()).rotation = rot;
            images.get(nameF.getText()).scale = scal;

            picturesToRender.set(currentImage.getIndex(),newData.toString());
            LucidityConfig.CONFIG_HANDLER.save();
        }
    }
    @Override
    public void close() {
        saveData();
        super.close();
    }

    public void setInstance(ImageConfigScreen instance) {
        this.instance = instance;
    }
}
