package com.iamlordst.realitybending.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RealityItem extends Item {
    public enum Power { FORUM_PHANTOM_MASK, REALITY_CHECK, PLOT_ARMOR, LAG_SPIKE,
        POCKET_DIMENSION, ADMIN_WAND, CHAOS_DICE, MEME_BEACON, GRAVITY_BOOTS }

    private final Power power;
    private final int cooldown;

    public RealityItem(Power power, int cooldown, Properties properties) {
        super(properties);
        this.power = power;
        this.cooldown = cooldown;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (activate(level, player)) {
            player.getCooldowns().addCooldown(this, cooldown);
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.65F, 0.9F + level.random.nextFloat() * 0.3F);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    private boolean activate(Level level, Player player) {
        switch (power) {
            case FORUM_PHANTOM_MASK -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
                return true;
            }
            case REALITY_CHECK -> {
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(12), e -> e != player);
                if (targets.isEmpty()) return false;
                for (LivingEntity target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0));
                }
                return true;
            }
            case PLOT_ARMOR -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 2));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                return true;
            }
            case LAG_SPIKE -> {
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(9), e -> e != player);
                if (targets.isEmpty()) return false;
                for (LivingEntity target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                    target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 160, 2));
                }
                return true;
            }
            case POCKET_DIMENSION -> {
                Vec3 look = player.getLookAngle().normalize();
                double distance = 8 + level.random.nextInt(9);
                Vec3 destination = player.position().add(look.scale(distance));
                BlockPos base = BlockPos.containing(destination);
                for (int dy = 2; dy >= -2; dy--) {
                    BlockPos feet = base.offset(0, dy, 0);
                    if (level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                        && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                        && !level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty()) {
                        player.teleportTo(feet.getX() + .5, feet.getY(), feet.getZ() + .5);
                        player.fallDistance = 0;
                        return true;
                    }
                }
                return false;
            }
            case ADMIN_WAND -> {
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(7), e -> e != player);
                if (targets.isEmpty()) return false;
                for (LivingEntity target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50, 1));
                }
                return true;
            }
            case CHAOS_DICE -> {
                switch (level.random.nextInt(6)) {
                    case 0 -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2));
                    case 1 -> player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300, 3));
                    case 2 -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
                    case 3 -> player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
                    case 4 -> player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
                    default -> player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 2));
                }
                return true;
            }
            case MEME_BEACON -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(10), e -> e != player);
                for (LivingEntity target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                }
                return true;
            }
            case GRAVITY_BOOTS -> {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300, 3));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 180, 0));
                return true;
            }
        }
        return false;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }
}
