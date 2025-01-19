/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package mypals.ml.features.renderer;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public interface IRenderer {
    public static void renderSelectionBox(MatrixStack matrices, Camera camera, BlockPos p1) {

        MinecraftClient client = MinecraftClient.getInstance();

        RenderSystem.disableDepthTest();
        BlockPos origin = p1;

        BlockPos size = p1;
        size = size.subtract(origin);

        matrices.push();

        VertexConsumer consumer = client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getLines());
        matrices.translate(origin.getX() - camera.getPos().x, origin.getY() - camera.getPos().y, origin.getZ() - camera.getPos().z);

        //WorldRenderer.renderFilledBox(matrices, consumer, 0, 0, 0, size.getX(), size.getY(), size.getZ(), 1, 1, 1, 1);
        WorldRenderer.drawBox(matrices, consumer, 0, 0, 0, size.getX(), size.getY(), size.getZ(), 1, 1, 1, 1, 0, 0, 0);

        matrices.pop();
        RenderSystem.enableDepthTest();
    }

}