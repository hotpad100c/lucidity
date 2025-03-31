package mypals.ml.features.damageIndicator;

import mypals.ml.config.LucidityConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class DamageHandler {
    private static float lastHealth = 114514.0F;
    public static class Indicator {
        @Nullable
        public Vec3d pos;
        public boolean isSourceMovable;
        @Nullable
        public DamageSource source;
        public long lifeTime;
        public Indicator(@Nullable Vec3d pos, @Nullable DamageSource source, boolean isSourceMovable , long lifeTime){
            this.pos = pos;
            this.source = source;
            this.isSourceMovable = isSourceMovable;
            this.lifeTime = lifeTime;
        }
    }
    public static ArrayList<Indicator> indicaors = new ArrayList<>();

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
            Indicator i = new Indicator(pos, damageSource,isSourceMovable,client.world.getTime() + LucidityConfig.damageIndicatorLifeTime);
            indicaors.add(i);
        }
        updateIndicators();
    }
    public static void updateIndicators(){
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.world == null) return;
        long currentTime = client.world.getTime();
        for(int i = indicaors.size() - 1; i >= 0; i--){
            Indicator indicaor = indicaors.get(i);
            if(indicaor.lifeTime < currentTime){
                indicaors.remove(i);
            }
            if(indicaor.isSourceMovable){
                if(indicaor.source.getSource() == null) return;
                indicaor.pos = indicaor.source.getSource().getPos();
            }
        }
    }
    public static float calculateTargetDamage(DamageSource source, float amount, LivingEntity target) {
        if (!target.isAlwaysInvulnerableTo(source)) {
            if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
                amount = DamageUtil.getDamageLeft(target, amount, source, (float)target.getArmor(), (float)target.getAttributeValue(EntityAttributes.ATTACK_DAMAGE));
            }

            amount = modifyAppliedDamage(source, amount, target);

            return Math.max(amount - target.getAbsorptionAmount(), 0.0F);
        }
        return 0;
    }

    private static float modifyAppliedDamage(DamageSource source, float amount, LivingEntity target) {
        if (source.isIn(DamageTypeTags.BYPASSES_EFFECTS)) {
            return amount;
        }

        // 只处理抗性效果（如果有）
        if (target.hasStatusEffect(StatusEffects.RESISTANCE) && !source.isIn(DamageTypeTags.BYPASSES_RESISTANCE)) {
            int i = (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f = amount * (float)j;
            amount = Math.max(f / 25.0F, 0.0F);
        }

        // 直接返回处理后的伤害
        return amount;
    }
    public static float calculatePlayerDamage(PlayerEntity player, Entity target) {
        float baseDamage = player.isUsingRiptide() ? player.riptideAttackDamage : (float)player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        ItemStack weapon = player.getWeaponStack();

        float additionalDamage = 0;
        float weaponDamage = (float) getWeaponAttackDamage(weapon);
        baseDamage = baseDamage + weaponDamage;
        float cooldownProgress = player.getAttackCooldownProgress(0.5F);
        baseDamage *= 0.2F + cooldownProgress * cooldownProgress * 0.8F;
        additionalDamage *= cooldownProgress;

        if (player.isSprinting() && cooldownProgress > 0.9F) {
            baseDamage *= 1.5F;  // 暴击
        }

        float finalDamage = baseDamage + additionalDamage;

        // 返回计算出的伤害
        return finalDamage;
    }
    public static double getWeaponAttackDamage(ItemStack itemStack) {
        AtomicReference<Double> v = new AtomicReference<>((double) 0);
        for (AttributeModifierSlot attributeModifierSlot : AttributeModifierSlot.values()) {
            itemStack.applyAttributeModifier(attributeModifierSlot, (attribute, modifier) -> {
                if (EntityAttributes.ATTACK_DAMAGE.equals(attribute)) {
                    v.set(modifier.value());
                }
            });
        }


        return v.get();
    }
}
