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
        if (!target.isInvulnerableTo(source)) {
            // 仅计算盔甲影响部分
            if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
                amount = DamageUtil.getDamageLeft(target, amount, source, (float)target.getArmor(), (float)target.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
            }

            // 计算伤害修正（如抗性）
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
        // 获取基础攻击伤害
        float baseDamage = player.isUsingRiptide() ? player.riptideAttackDamage : (float)player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        // 获取玩家持有的武器
        ItemStack weapon = player.getWeaponStack();

        // 获取伤害来源
        DamageSource damageSource = player.getDamageSources().playerAttack(player);

        // 计算附加伤害
        float additionalDamage = 0;
        // 获取武器的攻击力
        float weaponDamage = (float) getWeaponAttackDamage(weapon);
        baseDamage = baseDamage + weaponDamage;

        // 获取攻击冷却进度
        float cooldownProgress = player.getAttackCooldownProgress(0.5F);
        baseDamage *= 0.2F + cooldownProgress * cooldownProgress * 0.8F;
        additionalDamage *= cooldownProgress;

        // 计算暴击伤害
        if (player.isSprinting() && cooldownProgress > 0.9F) {
            baseDamage *= 1.5F;  // 暴击
        }

        // 计算最终伤害
        float finalDamage = baseDamage + additionalDamage;

        // 返回计算出的伤害
        return finalDamage;
    }
    public static double getWeaponAttackDamage(ItemStack itemStack) {
        // 获取物品的属性修饰符组件
        AttributeModifiersComponent attributeModifiersComponent = itemStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        AtomicReference<Double> v = new AtomicReference<>((double) 0);
        // 遍历所有属性修饰符槽
        for (AttributeModifierSlot attributeModifierSlot : AttributeModifierSlot.values()) {
            // 应用每个属性修饰符
            itemStack.applyAttributeModifier(attributeModifierSlot, (attribute, modifier) -> {
                // 检查是否是攻击伤害属性（generic.attackDamage）
                if (EntityAttributes.GENERIC_ATTACK_DAMAGE.equals(attribute)) {
                    v.set(modifier.value());
                }
            });
        }

        // 如果没有找到攻击伤害，可以返回一个默认值（例如物品的默认伤害值）
        return v.get();  // 默认值，依据需要调整
    }
    public static LivingEntity getLookingAtEntity(PlayerEntity player, World world) {
        // 获取玩家的眼睛位置
        Vec3d eyePos = player.getCameraPosVec(1.0F);

        // 获取玩家视线方向
        Vec3d lookDirection = player.getRotationVec(1.0F);

        // 射线长度，通常是 5 或更远
        double distance = 5.0;

        // 获取射线终点
        Vec3d endPos = eyePos.add(lookDirection.x * distance, lookDirection.y * distance, lookDirection.z * distance);

        // 使用射线追踪检测目标
        HitResult hitResult = world.raycast(new RaycastContext(
                eyePos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

        // 如果射线碰到实体，返回该实体
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            return (LivingEntity) entityHitResult.getEntity();
        }

        // 如果没有碰到实体，返回 null
        return null;
    }
}
