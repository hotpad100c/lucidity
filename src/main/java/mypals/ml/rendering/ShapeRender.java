package mypals.ml.rendering;
import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.features.selectiveRendering.WandActionsManager;
import mypals.ml.rendering.shapes.CubeShape;
import mypals.ml.rendering.shapes.LineShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ShapeRender {
    public static double lastTickPosX = 0;
    public static double lastTickPosY = 0;
    public static double lastTickPosZ = 0;

    public static void renderSelectionBox(MatrixStack matrices, Camera camera,float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        BlockPos origin = WandActionsManager.pos1;

        HitResult result = client.cameraEntity.raycast(player.getAbilities().creativeMode ? 5.0F : 4.5F, 0, false);
        BlockPos pos = WandActionsManager.pos2 != null ? WandActionsManager.pos2 : (result.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) result).getBlockPos() : BlockPos.ofFloored(result.getPos()));
        if(pos != null){
            CubeShape.drawSingle(matrices, pos, 0.01f, tickDelta, Color.blue, 0.2f,true);
        }
        if(origin != null){
            CubeShape.drawSingle(matrices, origin, 0.01f, tickDelta, Color.red, 0.2f,true);
        }
        pos = pos.subtract(origin);

        origin = origin.add(pos.getX() < 0 ? 1 : 0, pos.getY() < 0 ? 1 : 0, pos.getZ() < 0 ? 1 : 0);
        pos = pos.add(pos.getX() >= 0 ? 1 : -1, pos.getY() >= 0 ? 1 : -1, pos.getZ() >= 0 ? 1 : -1);

        lastTickPosX = camera.getPos().getX();
        lastTickPosY = camera.getPos().getY();
        lastTickPosZ = camera.getPos().getZ();
        float x = (float) (origin.getX() - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
        float y = (float) (origin.getY() - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
        float z = (float) (origin.getZ() - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));

        matrices.push();

        VertexConsumer consumer = client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getLines());
        matrices.translate(x, y, z);

        VertexRendering.drawBox(matrices, consumer, 0, 0, 0, pos.getX(), pos.getY(), pos.getZ(), 1, 1, 1, 1, 0, 0, 0);

        matrices.pop();
    }
    public static VertexConsumerProvider.Immediate getVertexConsumer() {
        return MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
    }
}
