package com.iamlordst.realitybending.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RealityItem extends Item {
    public enum Power { FORUM_PHANTOM_MASK, REALITY_CHECK, PLOT_ARMOR, LAG_SPIKE,
        POCKET_DIMENSION, ADMIN_WAND, CHAOS_DICE, MEME_BEACON, GRAVITY_BOOTS,
        KI_BLAST, KAMEHAMEHA, X_RAY_VISION, MIND_READING, TIME_WARP, PHASE_SHIFT, VOID_PULSE }

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
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
                return true;
            }
            case REALITY_CHECK -> {
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16), e -> e != player);
                if (targets.isEmpty()) return false;
                for (LivingEntity target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                }
                return true;
            }
            case PLOT_ARMOR -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 3));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 4));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
                return true;
            }
            case LAG_SPIKE -> {
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(12), e -> e != player);
                if (targets.isEmpty()) return false;
                for (LivingEntity target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 220, 4));
                    target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 220, 3));
                }
                return true;
            }
            case POCKET_DIMENSION -> {
                Vec3 look = player.getLookAngle().normalize();
                Vec3 destination = player.position().add(look.scale(10 + level.random.nextInt(9)));
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
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10), e -> e != player);
                if (targets.isEmpty()) return false;
                for (LivingEntity target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 2));
                    target.setDeltaMovement(target.getDeltaMovement().add(0, 0.35, 0));
                    target.hurtMarked = true;
                }
                return true;
            }
            case CHAOS_DICE -> {
                switch (level.random.nextInt(7)) {
                    case 0 -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 3));
                    case 1 -> player.addEffect(new MobEffectInstance(MobEffects.JUMP, 400, 4));
                    case 2 -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 2));
                    case 3 -> player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300, 0));
                    case 4 -> player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0));
                    case 5 -> player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3));
                    default -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2));
                }
                return true;
            }
            case MEME_BEACON -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2));
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16), e -> e != player);
                for (LivingEntity target : targets) target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
                return true;
            }
            case GRAVITY_BOOTS -> {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 400, 4));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
                return true;
            }
            case KI_BLAST -> {
                Vec3 look = player.getLookAngle().normalize();
                Vec3 origin = player.getEyePosition().add(look.scale(0.8));
                SmallFireball blast = new SmallFireball(level, player, look);
                blast.setPos(origin.x, origin.y, origin.z);
                blast.setSecondsOnFire(0);
                level.addFreshEntity(blast);
                player.swing(hand, true);
                return true;
            }
            case KAMEHAMEHA -> {
                Vec3 start = player.getEyePosition().add(player.getLookAngle().scale(1.0));
                Vec3 direction = player.getLookAngle().normalize();
                Vec3 end = start.add(direction.scale(32));
                BlockHitResult blockHit = level.clip(new net.minecraft.world.level.ClipContext(start, end,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, player));
                Vec3 beamEnd = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : end;
                double length = start.distanceTo(beamEnd);
                for (double d = 0; d < length; d += 0.35) {
                    Vec3 point = start.add(direction.scale(d));
                    level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 2, .08, .08, .08, 0.01);
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, point.x, point.y, point.z, 1, .04, .04, .04, 0.01);
                }
                AABB beamBox = new AABB(start, beamEnd).inflate(1.2);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, beamBox,
                    e -> e != player && e.isAlive() && beamDistanceSquared(e, start, direction, length) < 2.2);
                for (LivingEntity target : targets) {
                    target.hurt(level.damageSources().magic(), 18.0F);
                    target.setDeltaMovement(target.getDeltaMovement().add(direction.scale(1.8)));
                    target.hurtMarked = true;
                }
                level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.GHAST_SHOOT,
                    net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 0.65F);
                return true;
            }
            case X_RAY_VISION -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 500, 0));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 500, 0));
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(32), e -> e != player);
                for (LivingEntity target : targets) target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 180, 0));
                player.displayClientMessage(Component.literal("X-RAY PULSE: nearby living entities revealed. True ore-through-wall vision isn't supported yet."), true);
                return true;
            }
            case MIND_READING -> {
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(18), e -> e != player);
                if (targets.isEmpty()) {
                    player.displayClientMessage(Component.literal("TELEPATHY: no minds detected nearby."), true);
                } else {
                    StringBuilder message = new StringBuilder("TELEPATHY: ");
                    int count = Math.min(4, targets.size());
                    for (int i = 0; i < count; i++) {
                        if (i > 0) message.append(" • ");
                        LivingEntity target = targets.get(i);
                        message.append(target.getDisplayName().getString()).append(" [").append((int) target.getHealth()).append(" HP]");
                    }
                    player.displayClientMessage(Component.literal(message.toString()), true);
                }
                return true;
            }
            case TIME_WARP -> {
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16), e -> e != player);
                if (targets.isEmpty()) return false;
                for (LivingEntity target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 6));
                    target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 160, 4));
                    target.setDeltaMovement(target.getDeltaMovement().scale(0.1));
                }
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 2));
                return true;
            }
            case PHASE_SHIFT -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 3));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
                player.noPhysics = true;
                player.displayClientMessage(Component.literal("PHASE SHIFT! You can pass through obstacles briefly."), true);
                return true;
            }
            case VOID_PULSE -> {
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(12), e -> e != player);
                for (LivingEntity target : targets) {
                    Vec3 away = target.position().subtract(player.position()).normalize();
                    target.setDeltaMovement(away.scale(2.8).add(0, 1.2, 0));
                    target.hurtMarked = true;
                    level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + target.getBbHeight() * .5,
                        target.getZ(), 24, .5, .5, .5, .35);
                }
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 2));
                return true;
            }
        }
        return false;
    }

    private static double beamDistanceSquared(LivingEntity entity, Vec3 start, Vec3 direction, double maxLength) {
        Vec3 relative = entity.position().add(0, entity.getBbHeight() * 0.5, 0).subtract(start);
        double along = relative.dot(direction);
        if (along < 0 || along > maxLength) return Double.MAX_VALUE;
        return relative.subtract(direction.scale(along)).lengthSqr();
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }
}
