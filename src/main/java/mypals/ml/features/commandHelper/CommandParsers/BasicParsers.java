package mypals.ml.features.commandHelper.CommandParsers;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import static java.lang.Double.parseDouble;

public class BasicParsers {
    public static int parseCoordinateInt(String str, Entity entity, int dir){
        return (int) Math.floor(parseCoordinate(str,entity,dir));
    }
    public static double parseCoordinate(String str, Entity entity, int dir) {
        if (entity == null) return 0.0;

        // 处理 "~"（世界坐标）
        if (str.startsWith("~")) {
            double base = switch (dir) {
                case 0 -> entity.getX();
                case 1 -> entity.getY();
                case 2 -> entity.getZ();
                default -> 0;
            };
            double offset = str.equals("~") ? 0.0 : parseDouble(str.substring(1));
            return base + offset;
        }

        // 处理 "^"（本地坐标）
        if (str.startsWith("^")) {
            // 获取视角矩阵的三个方向向量
            Vec3d forward = getViewForward(entity); // 前向量
            Vec3d left = getViewLeft(entity);       // 左向量
            Vec3d up = getViewUp(entity);           // 上向量

            double offset = str.equals("^") ? 0.0 : parseDouble(str.substring(1));

            return switch (dir) {
                case 0 -> entity.getX() + forward.x * offset + left.x * offset + up.x * offset;
                case 1 -> entity.getY() + forward.y * offset + left.y * offset + up.y * offset;
                case 2 -> entity.getZ() + forward.z * offset + left.z * offset + up.z * offset;
                default -> 0.0;
            };
        }

        // 解析为普通的浮点数
        return parseDouble(str);
    }
    private static Vec3d getViewForward(Entity entity) {
        float yaw = (float) Math.toRadians(entity.getYaw());
        float pitch = (float) Math.toRadians(entity.getPitch());

        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);

        return new Vec3d(x, y, z);
    }

    private static Vec3d getViewLeft(Entity entity) {
        float yaw = (float) Math.toRadians(entity.getYaw());

        double x = Math.cos(yaw);
        double z = Math.sin(yaw);

        return new Vec3d(x, 0, -z);
    }

    private static Vec3d getViewUp(Entity entity) {
        float yaw = (float) Math.toRadians(entity.getYaw());
        float pitch = (float) Math.toRadians(entity.getPitch());

        double x = -Math.sin(yaw) * Math.sin(pitch);
        double y = Math.cos(pitch);
        double z = Math.cos(yaw) * Math.sin(pitch);

        return new Vec3d(x, y, z);
    }

}
