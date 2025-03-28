package mypals.ml.features.commandHelper.CommandParsers;

import mypals.ml.features.selectiveRendering.AreaBox;
import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.CubeShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.lang.Double.parseDouble;
import static mypals.ml.features.commandHelper.CommandParsers.BasicParsers.parseCoordinate;

public class ParticleCommandParser {

    public static void parseParticleCommand(String command) {
        Map<String, Object> parameters = parseParticleCommandParameters(command);
        if (parameters == null) return;

        // 提取解析结果
        String name = (String) parameters.get("name");
        double[] position = (double[]) parameters.get("position");
        double[] delta = (double[]) parameters.get("delta");
        double speed = (double) parameters.get("speed");
        int count = (int) parameters.get("count");
        boolean forceMode = (boolean) parameters.get("forceMode");

        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null || name == null || position.length < 3 || delta.length < 3) return;

        ParticleEffect particle = getParticleByName(name);
        if (particle == null) {
            System.out.println("Invalid particle name: " + name);
            return;
        }

        if (count == 0) {
            // `count = 0` 时，delta 作为运动方向
            double dx = delta[0] * speed;
            double dy = delta[1] * speed;
            double dz = delta[2] * speed;

            world.addParticle(particle, forceMode, position[0], position[1], position[2], dx, dy, dz);
        } else {
            for (int i = 0; i < count; i++) {
                // 生成符合正态分布的随机偏移
                double offsetX = gaussianOffset(position[0], delta[0]);
                double offsetY = gaussianOffset(position[1], delta[1]);
                double offsetZ = gaussianOffset(position[2], delta[2]);

                // 额外参数也服从正态分布，均值 0，标准差为 speed
                double dx = gaussianOffset(0, speed);
                double dy = gaussianOffset(0, speed);
                double dz = gaussianOffset(0, speed);

                world.addParticle(particle, forceMode, offsetX, offsetY, offsetZ, dx, dy, dz);
            }
        }
    }

    private static double gaussianOffset(double mean, double stddev) {
        Random random = new Random();
        return mean + random.nextGaussian() * Math.abs(stddev);
    }

    private static ParticleEffect getParticleByName(String name) {
        Registry<ParticleType<?>> particleRegistry = MinecraftClient.getInstance()
                .getNetworkHandler()
                .getRegistryManager()
                .get(RegistryKeys.PARTICLE_TYPE);

        Identifier id = Identifier.of(name);
        ParticleType<?> type = particleRegistry.get(id);

        if (type == null) return null;
        return (ParticleEffect) type;
    }

    public static Map<String, Object> parseParticleCommandParameters(String command) {
        Map<String, Object> parameters = new HashMap<>();
        String[] parts = command.trim().split("\\s+");

        if (parts.length < 2 || !parts[0].equalsIgnoreCase("/particle")) {
            System.out.println("Invalid command format. Expected 'particle <name> <args>'");
            return null;
        }

        parameters.put("name", parts[1]); // 粒子名称

        PlayerEntity playerEntity = MinecraftClient.getInstance().player;
        // 解析位置，默认值为[0.0, 0.0, 0.0]
        double[] position = new double[]{0.0, 0.0, 0.0};
        if (parts.length > 2) position[0] = parseCoordinate(parts[2],playerEntity,0);
        if (parts.length > 3) position[1] = parseCoordinate(parts[3],playerEntity,1);
        if (parts.length > 4) position[2] = parseCoordinate(parts[4],playerEntity,2);
        parameters.put("position", position);

        // 解析增量，默认值为[0.0, 0.0, 0.0]
        double[] delta = new double[]{0.0, 0.0, 0.0};
        if (parts.length > 5) delta[0] = parseCoordinate(parts[5],playerEntity,0);
        if (parts.length > 6) delta[1] = parseCoordinate(parts[6],playerEntity,1);
        if (parts.length > 7) delta[2] = parseCoordinate(parts[7],playerEntity,2);
        parameters.put("delta", delta);

        // 解析速度，默认值为 0.0
        double speed = 0.0;
        if (parts.length > 8) speed = parseDouble(parts[8]);
        parameters.put("speed", speed);

        // 解析数量，默认值为 1
        int count = 1;
        if (parts.length > 9) count = parseInt(parts[9]);
        parameters.put("count", count);

        // 解析 force|normal，默认值为 normal
        boolean forceMode = false;
        if (parts.length > 10) {
            String mode = parts[10];
            forceMode = "force".equalsIgnoreCase(mode);
        }
        parameters.put("forceMode", forceMode);

        return parameters;
    }
    private static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            System.out.println("Invalid integer format: " + str);
            return 1; // 默认值
        }
    }
}
