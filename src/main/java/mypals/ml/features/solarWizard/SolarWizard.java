package mypals.ml.features.solarWizard;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class SolarWizard {
    public static boolean shouldActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = MinecraftClient.getInstance().world;
        if(world == null) return false;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return false;
        Hand hand = player.getActiveHand();
        return client.options.useKey.isPressed() && player.getStackInHand(hand).isOf(Items.BLAZE_ROD);
    }
    private static void adjustWorldTime(PlayerEntity player, ClientWorld world) {
        float yaw = player.getYaw();
        long timeOfDay = (long) ((yaw / 360.0) * 24000) % 24000; // 计算新的时间

        world.setTime(timeOfDay,timeOfDay,false);
    }

    public static long calculateOffset(PlayerEntity player, long baseTime) {
        float pitch = player.getPitch(); // 玩家视角角度 (-90° 到 90°)

        // 使用 sin(pitch) 映射太阳角度（确保地面朝向 = 午夜）
        double sunAngle = (1 + Math.sin(Math.toRadians(pitch))) / 2.0;

        // 计算目标时间，确保 0 = 午夜，12000 = 正午
        long desiredTime = (long) (sunAngle * 24000);

        return desiredTime - baseTime; // 计算偏移量
    }
}
