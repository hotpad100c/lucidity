package mypals.ml.features.arrowCamera;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class ArrowCameraEntityRender extends EntityRenderer<ArrowCameraEntity, ArrowCameraEntityModel> {


    public ArrowCameraEntityRender(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public ArrowCameraEntityModel createRenderState() {
        return null;
    }
}