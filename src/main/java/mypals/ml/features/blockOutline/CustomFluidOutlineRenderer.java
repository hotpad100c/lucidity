package mypals.ml.features.blockOutline;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class CustomFluidOutlineRenderer {

    public static void renderFluidOutline(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, MatrixStack matrixStack) {

        matrixStack.push();
        matrixStack.translate(-pos.getX(), -pos.getY(), -pos.getZ());

        boolean isLava = fluidState.isIn(FluidTags.LAVA);
        Sprite[] sprites = new Sprite[]{ModelBaker.LAVA_FLOW.getSprite()};
        int color = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(world, pos);
        float r = (float) (color >> 16 & 0xFF) / 255.0F;
        float g = (float) (color >> 8 & 0xFF) / 255.0F;
        float b = (float) (color & 0xFF) / 255.0F;

        // 获取周围方块和流体状态
        BlockState downState = world.getBlockState(pos.offset(Direction.DOWN));
        FluidState downFluidState = downState.getFluidState();
        BlockState upState = world.getBlockState(pos.offset(Direction.UP));
        FluidState upFluidState = upState.getFluidState();
        BlockState northState = world.getBlockState(pos.offset(Direction.NORTH));
        FluidState northFluidState = northState.getFluidState();
        BlockState southState = world.getBlockState(pos.offset(Direction.SOUTH));
        FluidState southFluidState = southState.getFluidState();
        BlockState westState = world.getBlockState(pos.offset(Direction.WEST));
        FluidState westFluidState = westState.getFluidState();
        BlockState eastState = world.getBlockState(pos.offset(Direction.EAST));
        FluidState eastFluidState = eastState.getFluidState();

        // 判断哪些面需要渲染
        boolean renderUp = !isSameFluid(fluidState, upFluidState);
        boolean renderDown = shouldRenderSide(world, pos, fluidState, blockState, Direction.DOWN, downFluidState)
                && !isSideCovered(world, pos, Direction.DOWN, 0.8888889F, downState);
        boolean renderNorth = shouldRenderSide(world, pos, fluidState, blockState, Direction.NORTH, northFluidState);
        boolean renderSouth = shouldRenderSide(world, pos, fluidState, blockState, Direction.SOUTH, southFluidState);
        boolean renderWest = shouldRenderSide(world, pos, fluidState, blockState, Direction.WEST, westFluidState);
        boolean renderEast = shouldRenderSide(world, pos, fluidState, blockState, Direction.EAST, eastFluidState);

        if (renderUp || renderDown || renderEast || renderWest || renderNorth || renderSouth) {
            float downBrightness = world.getBrightness(Direction.DOWN, true);
            float upBrightness = world.getBrightness(Direction.UP, true);
            float northBrightness = world.getBrightness(Direction.NORTH, true);
            float westBrightness = world.getBrightness(Direction.WEST, true);
            Fluid fluid = fluidState.getFluid();
            float fluidHeight = getFluidHeight(world, fluid, pos, blockState, fluidState);
            float hNW, hSW, hSE, hNE;

            if (fluidHeight >= 1.0F) {
                hNW = 1.0F;
                hSW = 1.0F;
                hSE = 1.0F;
                hNE = 1.0F;
            } else {
                float northHeight = getFluidHeight(world, fluid, pos.north(), northState, northFluidState);
                float southHeight = getFluidHeight(world, fluid, pos.south(), southState, southFluidState);
                float eastHeight = getFluidHeight(world, fluid, pos.east(), eastState, eastFluidState);
                float westHeight = getFluidHeight(world, fluid, pos.west(), westState, westFluidState);
                hNW = calculateFluidHeight(world, fluid, fluidHeight, northHeight, westHeight, pos.offset(Direction.NORTH).offset(Direction.WEST));
                hNE = calculateFluidHeight(world, fluid, fluidHeight, northHeight, eastHeight, pos.offset(Direction.NORTH).offset(Direction.EAST));
                hSE = calculateFluidHeight(world, fluid, fluidHeight, southHeight, eastHeight, pos.offset(Direction.SOUTH).offset(Direction.EAST));
                hSW = calculateFluidHeight(world, fluid, fluidHeight, southHeight, westHeight, pos.offset(Direction.SOUTH).offset(Direction.WEST));
            }

            // 调整坐标为世界坐标
            float x = pos.getX();
            float y = pos.getY();
            float z = pos.getZ();
            float offset = 0.001F;
            float downOffset = renderDown ? 0.001F : 0.0F;

            // 渲染顶面
            if (renderUp && !isSideCovered(world, pos, Direction.UP, Math.min(Math.min(hNW, hSW), Math.min(hSE, hNE)), upState)) {
                hNW -= 0.001F;
                hSW -= 0.001F;
                hSE -= 0.001F;
                hNE -= 0.001F;
                Vec3d velocity = fluidState.getVelocity(world, pos);
                float u0, v0, u1, v1, u2, v2, u3, v3;

                if (velocity.x == 0.0 && velocity.z == 0.0) {
                    Sprite sprite = sprites[0];
                    u0 = sprite.getFrameU(0.0F);
                    v0 = sprite.getFrameV(0.0F);
                    u1 = u0;
                    v1 = sprite.getFrameV(1.0F);
                    u2 = sprite.getFrameU(1.0F);
                    v2 = v1;
                    u3 = u2;
                    v3 = v0;
                } else {
                    Sprite sprite = sprites[0];
                    float angle = (float) MathHelper.atan2(velocity.z, velocity.x) - (float) (Math.PI / 2);
                    float sin = MathHelper.sin(angle) * 0.25F;
                    float cos = MathHelper.cos(angle) * 0.25F;
                    float center = 0.5F;
                    u0 = sprite.getFrameU(center + (-cos - sin));
                    v0 = sprite.getFrameV(center + -cos + sin);
                    u1 = sprite.getFrameU(center + -cos + sin);
                    v1 = sprite.getFrameV(center + cos + sin);
                    u2 = sprite.getFrameU(center + cos + sin);
                    v2 = sprite.getFrameV(center + (cos - sin));
                    u3 = sprite.getFrameU(center + (cos - sin));
                    v3 = sprite.getFrameV(center + (-cos - sin));
                }

                float uAvg = (u0 + u1 + u2 + u3) / 4.0F;
                float vAvg = (v0 + v1 + v2 + v3) / 4.0F;
                float deltaUV = sprites[0].getAnimationFrameDelta();
                u0 = MathHelper.lerp(deltaUV, u0, uAvg);
                u1 = MathHelper.lerp(deltaUV, u1, uAvg);
                u2 = MathHelper.lerp(deltaUV, u2, uAvg);
                u3 = MathHelper.lerp(deltaUV, u3, uAvg);
                v0 = MathHelper.lerp(deltaUV, v0, vAvg);
                v1 = MathHelper.lerp(deltaUV, v1, vAvg);
                v2 = MathHelper.lerp(deltaUV, v2, vAvg);
                v3 = MathHelper.lerp(deltaUV, v3, vAvg);

                int light = getLight(world, pos);
                float rUp = upBrightness * r;
                float gUp = upBrightness * g;
                float bUp = upBrightness * b;

                vertex(vertexConsumer, x + 0.0F, y + hNW, z + 0.0F, rUp, gUp, bUp, u0, v0, light, matrixStack);
                vertex(vertexConsumer, x + 0.0F, y + hSW, z + 1.0F, rUp, gUp, bUp, u1, v1, light, matrixStack);
                vertex(vertexConsumer, x + 1.0F, y + hSE, z + 1.0F, rUp, gUp, bUp, u2, v2, light, matrixStack);
                vertex(vertexConsumer, x + 1.0F, y + hNE, z + 0.0F, rUp, gUp, bUp, u3, v3, light, matrixStack);

                if (fluidState.canFlowTo(world, pos.up())) {
                    vertex(vertexConsumer, x + 0.0F, y + hNW, z + 0.0F, rUp, gUp, bUp, u0, v0, light, matrixStack);
                    vertex(vertexConsumer, x + 1.0F, y + hNE, z + 0.0F, rUp, gUp, bUp, u3, v3, light, matrixStack);
                    vertex(vertexConsumer, x + 1.0F, y + hSE, z + 1.0F, rUp, gUp, bUp, u2, v2, light, matrixStack);
                    vertex(vertexConsumer, x + 0.0F, y + hSW, z + 1.0F, rUp, gUp, bUp, u1, v1, light, matrixStack);
                }
            }

            // 渲染底面
            if (renderDown) {
                float uMin = sprites[0].getMinU();
                float uMax = sprites[0].getMaxU();
                float vMin = sprites[0].getMinV();
                float vMax = sprites[0].getMaxV();
                int light = getLight(world, pos.down());
                float rDown = downBrightness * r;
                float gDown = downBrightness * g;
                float bDown = downBrightness * b;

                vertex(vertexConsumer, x, y + downOffset, z + 1.0F, rDown, gDown, bDown, uMin, vMax, light, matrixStack);
                vertex(vertexConsumer, x, y + downOffset, z, rDown, gDown, bDown, uMin, vMin, light, matrixStack);
                vertex(vertexConsumer, x + 1.0F, y + downOffset, z, rDown, gDown, bDown, uMax, vMin, light, matrixStack);
                vertex(vertexConsumer, x + 1.0F, y + downOffset, z + 1.0F, rDown, gDown, bDown, uMax, vMax, light, matrixStack);
            }

            // 渲染侧面
            int light = getLight(world, pos);
            for (Direction direction : Direction.Type.HORIZONTAL) {
                float h1, h2, x1, x2, z1, z2;
                boolean shouldRenderSide;
                switch (direction) {
                    case NORTH:
                        h1 = hNW;
                        h2 = hNE;
                        x1 = x;
                        x2 = x + 1.0F;
                        z1 = z + 0.001F;
                        z2 = z + 0.001F;
                        shouldRenderSide = renderNorth;
                        break;
                    case SOUTH:
                        h1 = hSE;
                        h2 = hSW;
                        x1 = x + 1.0F;
                        x2 = x;
                        z1 = z + 1.0F - 0.001F;
                        z2 = z + 1.0F - 0.001F;
                        shouldRenderSide = renderSouth;
                        break;
                    case WEST:
                        h1 = hSW;
                        h2 = hNW;
                        x1 = x + 0.001F;
                        x2 = x + 0.001F;
                        z1 = z + 1.0F;
                        z2 = z;
                        shouldRenderSide = renderWest;
                        break;
                    default:
                        h1 = hNE;
                        h2 = hSE;
                        x1 = x + 1.0F - 0.001F;
                        x2 = x + 1.0F - 0.001F;
                        z1 = z;
                        z2 = z + 1.0F;
                        shouldRenderSide = renderEast;
                }

                if (shouldRenderSide && !isSideCovered(world, pos, direction, Math.max(h1, h2), world.getBlockState(pos.offset(direction)))) {
                    Sprite sprite = sprites[0];

                    float u0 = sprite.getFrameU(0.0F);
                    float u1 = sprite.getFrameU(0.5F);
                    float v0 = sprite.getFrameV((1.0F - h1) * 0.5F);
                    float v1 = sprite.getFrameV((1.0F - h2) * 0.5F);
                    float v2 = sprite.getFrameV(0.5F);
                    float brightness = direction.getAxis() == Direction.Axis.Z ? northBrightness : westBrightness;
                    float rSide = upBrightness * brightness * r;
                    float gSide = upBrightness * brightness * g;
                    float bSide = upBrightness * brightness * b;

                    vertex(vertexConsumer, x1, y + h1, z1, rSide, gSide, bSide, u0, v0, light, matrixStack);
                    vertex(vertexConsumer, x2, y + h2, z2, rSide, gSide, bSide, u1, v1, light, matrixStack);
                    vertex(vertexConsumer, x2, y + downOffset, z2, rSide, gSide, bSide, u1, v2, light, matrixStack);
                    vertex(vertexConsumer, x1, y + downOffset, z1, rSide, gSide, bSide, u0, v2, light, matrixStack);
                }
            }
        }
        matrixStack.pop();
    }

    private static void vertex(VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, int n,MatrixStack matrixStack) {
        vertexConsumer.vertex(matrixStack.peek().getPositionMatrix(), f, g, h).color(i, j, k, 1.0F).texture(l, m).light(n).normal(0.0F, 1.0F, 0.0F);
    }


    private static float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (fluid.matchesType(fluidState.getFluid())) {
            BlockState blockState2 = world.getBlockState(pos.up());
            return fluid.matchesType(blockState2.getFluidState().getFluid()) ? 1.0F : fluidState.getHeight();
        } else {
            return !blockState.isSolid() ? 0.0F : -1.0F;
        }
    }

    private static float calculateFluidHeight(BlockRenderView world, Fluid fluid, float originHeight, float northSouthHeight, float eastWestHeight, BlockPos pos) {
        if (!(eastWestHeight >= 1.0F) && !(northSouthHeight >= 1.0F)) {
            float[] fs = new float[2];
            if (eastWestHeight > 0.0F || northSouthHeight > 0.0F) {
                float f = getFluidHeight(world, fluid, pos);
                if (f >= 1.0F) {
                    return 1.0F;
                }

                addHeight(fs, f);
            }

            addHeight(fs, originHeight);
            addHeight(fs, eastWestHeight);
            addHeight(fs, northSouthHeight);
            return fs[0] / fs[1];
        } else {
            return 1.0F;
        }
    }
    private static void addHeight(float[] weightedAverageHeight, float height) {
        if (height >= 0.8F) {
            weightedAverageHeight[0] += height * 10.0F;
            weightedAverageHeight[1] += 10.0F;
        } else if (height >= 0.0F) {
            weightedAverageHeight[0] += height;
            weightedAverageHeight[1]++;
        }
    }

    private static float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return getFluidHeight(world, fluid, pos, blockState, blockState.getFluidState());
    }

    private static boolean shouldRenderSide(BlockRenderView world, BlockPos pos, FluidState fluidState, BlockState blockState, Direction direction, FluidState neighborFluidState) {
        return !(isSameFluid(fluidState, neighborFluidState) &&
                !blockState.isSideSolidFullSquare(world, pos, direction) ) ||
                !OutlineManager.blockToRenderer.containsKey(pos.offset(direction));
    }

    private static boolean isSameFluid(FluidState fluidState, FluidState other) {
        return fluidState.getFluid().matchesType(other.getFluid());
    }

    private static boolean isSideCovered(BlockRenderView world, BlockPos pos, Direction direction, float height, BlockState neighborState) {
        return neighborState.isSideSolidFullSquare(world, pos.offset(direction), direction.getOpposite()) && height >= 1.0F;
    }
    private static int getLight(BlockRenderView world, BlockPos pos) {
        int i = WorldRenderer.getLightmapCoordinates(world, pos);
        int j = WorldRenderer.getLightmapCoordinates(world, pos.up());
        int k = i & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        int l = j & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        int m = i >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        int n = j >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        return (k > l ? k : l) | (m > n ? m : n) << 16;
    }
}