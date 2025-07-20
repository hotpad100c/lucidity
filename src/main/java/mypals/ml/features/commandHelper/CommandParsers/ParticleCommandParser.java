package mypals.ml.features.commandHelper.CommandParsers;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.lang.Double.parseDouble;
import static mypals.ml.features.commandHelper.CommandParsers.BasicParsers.parseCoordinate;

public class ParticleCommandParser {

    public static void parseParticleCommand(String command) {
        Map<String, Object> parameters = parseParticleCommandParameters(command);
        if (parameters == null) return;

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
            return;
        }

        if (count == 0) {
            double dx = delta[0] * speed;
            double dy = delta[1] * speed;
            double dz = delta[2] * speed;

            world.addParticle(particle,forceMode,false, position[0], position[1], position[2], dx, dy, dz);
        } else {
            for (int i = 0; i < count; i++) {
                double offsetX = gaussianOffset(position[0], delta[0]);
                double offsetY = gaussianOffset(position[1], delta[1]);
                double offsetZ = gaussianOffset(position[2], delta[2]);

                double dx = gaussianOffset(0, speed);
                double dy = gaussianOffset(0, speed);
                double dz = gaussianOffset(0, speed);

                world.addParticle(particle, forceMode,false, offsetX, offsetY, offsetZ, dx, dy, dz);
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
                .getOrThrow(RegistryKeys.PARTICLE_TYPE);

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

        parameters.put("name", parts[1]);

        PlayerEntity playerEntity = MinecraftClient.getInstance().player;
        double[] position = new double[]{0.0, 0.0, 0.0};
        if (parts.length > 2) position[0] = parseCoordinate(parts[2],playerEntity,0);
        if (parts.length > 3) position[1] = parseCoordinate(parts[3],playerEntity,1);
        if (parts.length > 4) position[2] = parseCoordinate(parts[4],playerEntity,2);
        parameters.put("position", position);

        double[] delta = new double[]{0.0, 0.0, 0.0};
        if (parts.length > 5) delta[0] = parseCoordinate(parts[5],playerEntity,0);
        if (parts.length > 6) delta[1] = parseCoordinate(parts[6],playerEntity,1);
        if (parts.length > 7) delta[2] = parseCoordinate(parts[7],playerEntity,2);
        parameters.put("delta", delta);

        double speed = 0.0;
        if (parts.length > 8) speed = parseDouble(parts[8]);
        parameters.put("speed", speed);
        int count = 1;
        if (parts.length > 9) count = parseInt(parts[9]);
        parameters.put("count", count);
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
            return 1;
        }
    }
}