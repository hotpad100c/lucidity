package mypals.ml.features.damageIndicator;

import mypals.ml.config.LucidityConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DamageHandler {
    private static float lastHealth = 114514.0F;
    public static class Indicaor{
        @Nullable
        public Vec3d pos;
        public boolean isSourceMovable;
        @Nullable
        public DamageSource source;
        public long lifeTime;
        public Indicaor(@Nullable Vec3d pos, @Nullable DamageSource source,boolean isSourceMovable ,long lifeTime){
            this.pos = pos;
            this.source = source;
            this.isSourceMovable = isSourceMovable;
            this.lifeTime = lifeTime;
        }
    }
    public static ArrayList<Indicaor> indicaors = new ArrayList<>();

    public static void PlayerHealthMonitor(){
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        LucidityConfig.CONFIG_HANDLER.instance();
        float currentHealth = player.getHealth();
        boolean damaged = lastHealth != 114514.0F && currentHealth < lastHealth;
        lastHealth = currentHealth;
        if(damaged){
            DamageSource damageSource = player.getRecentDamageSource();
            if(damageSource == null || client.world == null) return;
            Vec3d pos = damageSource.getPosition()!= null?damageSource.getPosition():player.getPos();
            boolean isSourceMovable = damageSource.getSource() != null;
            Indicaor i = new Indicaor(pos, damageSource,isSourceMovable,client.world.getTime() + LucidityConfig.damageIndicatorLifeTime);
            indicaors.add(i);
        }
        updateIndicators();
    }
    public static void updateIndicators(){
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.world == null) return;
        long currentTime = client.world.getTime();
        for(int i = indicaors.size() - 1; i >= 0; i--){
            Indicaor indicaor = indicaors.get(i);
            if(indicaor.lifeTime < currentTime){
                indicaors.remove(i);
            }
            if(indicaor.isSourceMovable){
                if(indicaor.source.getSource() == null) return;
                indicaor.pos = indicaor.source.getSource().getPos();
            }
        }
    }
}
