package mypals.ml.features.arrowCamera;

import mypals.ml.features.arrowCamera.ArrowCameraEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class ArrowCameraEntityRender extends EntityRenderer<ArrowCameraEntity> {


    public ArrowCameraEntityRender(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(ArrowCameraEntity entity) {
        return Identifier.of("camera", "textures/entity/camera/camera.png");
    }
}
