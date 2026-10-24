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
import net.minecraft.world.item.UseAnim;
import com.iamlordst.realitybending.registry.ModSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RealityItem extends Item {
    public enum Power { FORUM_PHANTOM_MASK, REALITY_CHECK, PLOT_ARMOR, LAG_SPIKE,
        POCKET_DIMENSION, ADMIN_WAND, CHAOS_DICE, MEME_BEACON, GRAVITY_BOOTS,
        KI_BLAST, KAMEHAMEHA, X_RAY_VISION, MIND_READING, TIME_WARP, PHASE_SHIFT, VOID_PULSE, BLACK_HOLE, REALITY_RIFT, LIGHTNING_SPEAR }

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
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);

        // Ki Blast and Kamehameha are channelled: hold to charge, release to fire.
        if (power == Power.KI_BLAST || power == Power.KAMEHAMEHA) {
            player.startUsingItem(hand);
            if (!level.isClientSide) {
                level.playSound(null, player.blockPosition(),
                    power == Power.KI_BLAST ? ModSounds.KI_CHARGE.get() : ModSounds.KAMEHAMEHA_CHARGE.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return InteractionResultHolder.consume(stack);
        }

        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        if (activate(level, player)) {
            player.getCooldowns().addCooldown(this, cooldown);
            playPowerSound(level, player);
            spawnCastEffect(level, player);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return power == Power.KI_BLAST || power == Power.KAMEHAMEHA ? 72000 : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return power == Power.KAMEHAMEHA ? UseAnim.SPYGLASS
            : power == Power.KI_BLAST ? UseAnim.BOW : UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, net.minecraft.world.entity.LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player) || !(level instanceof ServerLevel server) || remainingUseDuration % 2 != 0) return;
        int elapsed = getUseDuration(stack) - remainingUseDuration;
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = look.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 0.01) side = new Vec3(1, 0, 0);
        side = side.normalize();
        Vec3 up = look.cross(side).normalize();
        Vec3 center = player.getEyePosition().add(look.scale(0.85));
        double radius = 0.25 + Math.min(0.65, elapsed / 35.0);
        for (int i = 0; i < 8; i++) {
            double angle = elapsed * 0.24 + i * Math.PI / 4.0;
            Vec3 point = center.add(side.scale(Math.cos(angle) * radius)).add(up.scale(Math.sin(angle) * radius));
            server.sendParticles(power == Power.KI_BLAST ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK,
                point.x, point.y, point.z, 1, .015, .015, .015, .01);
        }
        server.sendParticles(power == Power.KI_BLAST ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.WAX_ON,
            center.x, center.y, center.z, 2, .12, .12, .12, .015);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity, int timeLeft) {
        if (level.isClientSide || !(entity instanceof Player player)) return;
        int chargeTicks = getUseDuration(stack) - timeLeft;
        if (chargeTicks < 5 || player.getCooldowns().isOnCooldown(this)) return;
        player.getCooldowns().addCooldown(this, cooldown);
        if (power == Power.KI_BLAST) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 origin = player.getEyePosition().add(look.scale(0.9));
            SmallFireball blast = new SmallFireball(level, player, look.x, look.y, look.z);
            blast.setPos(origin.x, origin.y, origin.z);
            level.addFreshEntity(blast);
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.END_ROD, origin.x, origin.y, origin.z, 18, .16, .16, .16, .08);
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, origin.x, origin.y, origin.z, 12, .12, .12, .12, .04);
            }
            level.playSound(null, player.blockPosition(), ModSounds.KI_RELEASE.get(),
                net.minecraft.sounds.SoundSource.PLAYERS, 1.25F, 1.0F);
        } else if (power == Power.KAMEHAMEHA) {
            fireKamehameha(level, player, Math.min(1.0, chargeTicks / 50.0));
        }
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
                SmallFireball blast = new SmallFireball(level, player, look.x, look.y, look.z);
                blast.setPos(origin.x, origin.y, origin.z);
                blast.setSecondsOnFire(0);
                level.addFreshEntity(blast);
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
                    if (level instanceof ServerLevel serverLevel) serverLevel.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 2, .08, .08, .08, 0.01);
                    if (level instanceof ServerLevel serverLevel) serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, point.x, point.y, point.z, 1, .04, .04, .04, 0.01);
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
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 3));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
                player.displayClientMessage(Component.literal("PHASE SHIFT! You are briefly cloaked and move at supernatural speed."), true);
                return true;
            }
            case VOID_PULSE -> {
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(12), e -> e != player);
                for (LivingEntity target : targets) {
                    Vec3 away = target.position().subtract(player.position()).normalize();
                    target.setDeltaMovement(away.scale(2.8).add(0, 1.2, 0));
                    target.hurtMarked = true;
                    if (level instanceof ServerLevel serverLevel) serverLevel.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + target.getBbHeight() * .5,
                        target.getZ(), 24, .5, .5, .5, .35);
                }
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 2));
                return true;
            }
            case BLACK_HOLE -> {
                Vec3 center = player.getEyePosition().add(player.getLookAngle().normalize().scale(8));
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(center, center).inflate(10), e -> e != player);
                for (LivingEntity target : targets) {
                    Vec3 pull = center.subtract(target.position().add(0, target.getBbHeight() * .5, 0));
                    double distance = Math.max(1.0, pull.length());
                    target.setDeltaMovement(pull.normalize().scale(Math.min(2.8, 12.0 / distance)));
                    target.hurtMarked = true;
                }
                if (level instanceof ServerLevel server) {
                    for (int i = 0; i < 36; i++) {
                        double angle = i * Math.PI / 9.0;
                        double radius = 1.2 + (i % 4) * .35;
                        server.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x + Math.cos(angle) * radius,
                            center.y + Math.sin(angle) * radius, center.z, 2, .08, .08, .08, .02);
                    }
                    server.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 60, 1.1, 1.1, 1.1, .25);
                }
                level.playSound(null, BlockPos.containing(center), net.minecraft.sounds.SoundEvents.WARDEN_SONIC_CHARGE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.2F, .55F);
                return true;
            }
            case REALITY_RIFT -> {
                Vec3 look = player.getLookAngle().normalize();
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(24), e -> e != player && e.isAlive());
                LivingEntity target = targets.stream()
                    .filter(e -> e.position().subtract(player.position()).dot(look) > 2)
                    .min((a, b) -> Double.compare(a.distanceToSqr(player), b.distanceToSqr(player)))
                    .orElse(null);
                if (target == null) return false;
                Vec3 behind = target.position().subtract(target.getLookAngle().normalize().scale(2.0));
                BlockPos feet = BlockPos.containing(behind);
                if (!level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                    || !level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                    || level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty()) {
                    behind = target.position().add(0, 0, 2);
                    feet = BlockPos.containing(behind);
                }
                player.teleportTo(feet.getX() + .5, feet.getY(), feet.getZ() + .5);
                player.fallDistance = 0;
                if (level instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + .8, target.getZ(), 35, .5, .7, .5, .2);
                    server.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + .8, player.getZ(), 35, .5, .7, .5, .2);
                }
                return true;
            }
            case LIGHTNING_SPEAR -> {
                Vec3 start = player.getEyePosition();
                Vec3 end = start.add(player.getLookAngle().normalize().scale(36));
                BlockHitResult hit = level.clip(new net.minecraft.world.level.ClipContext(start, end,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, player));
                Vec3 strike = hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : end;
                net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(strike.x, strike.y, strike.z);
                    level.addFreshEntity(bolt);
                }
                if (level instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, start.x, start.y, start.z, 35, .3, .3, .3, .25);
                    server.sendParticles(ParticleTypes.FLASH, strike.x, strike.y, strike.z, 1, 0, 0, 0, 0);
                }
                level.playSound(null, BlockPos.containing(strike), net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.4F, 1.25F);
                return true;
            }
        }
        return false;
    }

    private void playPowerSound(Level level, Player player) {
        net.minecraft.sounds.SoundEvent sound = ModSounds.POWER_CAST.get();
        float pitch = 0.9F + level.random.nextFloat() * 0.25F;
        if (power == Power.BLACK_HOLE || power == Power.VOID_PULSE) sound = ModSounds.VOID_CAST.get();
        else if (power == Power.REALITY_RIFT || power == Power.POCKET_DIMENSION) sound = ModSounds.RIFT_CAST.get();
        else if (power == Power.LIGHTNING_SPEAR) sound = ModSounds.LIGHTNING_CAST.get();
        level.playSound(null, player.blockPosition(), sound, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, pitch);
    }

    private void spawnCastEffect(Level level, Player player) {
        if (!(level instanceof ServerLevel server)) return;
        Vec3 center = player.getEyePosition().add(player.getLookAngle().normalize().scale(1.0));
        net.minecraft.core.particles.ParticleOptions particle = switch (power) {
            case BLACK_HOLE, VOID_PULSE -> ParticleTypes.REVERSE_PORTAL;
            case REALITY_RIFT, POCKET_DIMENSION, PHASE_SHIFT -> ParticleTypes.PORTAL;
            case LIGHTNING_SPEAR, TIME_WARP -> ParticleTypes.ELECTRIC_SPARK;
            case PLOT_ARMOR -> ParticleTypes.TOTEM_OF_UNDYING;
            case GRAVITY_BOOTS, ADMIN_WAND -> ParticleTypes.CLOUD;
            case MIND_READING, REALITY_CHECK, X_RAY_VISION -> ParticleTypes.ENCHANT;
            case LAG_SPIKE -> ParticleTypes.CRIT;
            default -> ParticleTypes.END_ROD;
        };
        server.sendParticles(particle, center.x, center.y, center.z, 28, .45, .55, .45, .12);
        server.sendParticles(ParticleTypes.WITCH, center.x, center.y, center.z, 10, .35, .4, .35, .03);
    }

    private void fireKamehameha(Level level, Player player, double charge) {
        Vec3 start = player.getEyePosition().add(player.getLookAngle().normalize().scale(1.0));
        Vec3 direction = player.getLookAngle().normalize();
        double maxLength = 32.0 + charge * 12.0;
        Vec3 end = start.add(direction.scale(maxLength));
        BlockHitResult blockHit = level.clip(new net.minecraft.world.level.ClipContext(start, end,
            net.minecraft.world.level.ClipContext.Block.COLLIDER,
            net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        Vec3 beamEnd = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : end;
        double length = start.distanceTo(beamEnd);
        double thickness = 0.85 + charge * 0.7;
        if (level instanceof ServerLevel server) {
            for (double d = 0; d < length; d += 0.22) {
                Vec3 point = start.add(direction.scale(d));
                double pulse = 0.07 + 0.05 * Math.sin(d * 1.7 + player.tickCount);
                server.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 2, thickness * .16, thickness * .16, thickness * .16, .01);
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, point.x, point.y, point.z, 2, pulse, pulse, pulse, .015);
                if (((int)(d * 10)) % 4 == 0) server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    point.x, point.y, point.z, 1, thickness * .1, thickness * .1, thickness * .1, .02);
            }
            server.sendParticles(ParticleTypes.FLASH, beamEnd.x, beamEnd.y, beamEnd.z, 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.EXPLOSION, beamEnd.x, beamEnd.y, beamEnd.z, 8 + (int)(charge * 12), .4, .4, .4, .08);
            server.sendParticles(ParticleTypes.END_ROD, beamEnd.x, beamEnd.y, beamEnd.z, 30, .7, .7, .7, .14);
        }
        AABB beamBox = new AABB(start, beamEnd).inflate(thickness);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, beamBox,
            e -> e != player && e.isAlive() && beamDistanceSquared(e, start, direction, length) < thickness * thickness * 1.8);
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().magic(), (float)(14.0 + charge * 14.0));
            target.setDeltaMovement(target.getDeltaMovement().add(direction.scale(1.3 + charge * 1.7)));
            target.hurtMarked = true;
        }
        level.playSound(null, player.blockPosition(), ModSounds.KAMEHAMEHA_RELEASE.get(),
            net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 1.0F - (float)charge * .2F);
    }

    private static double beamDistanceSquared(LivingEntity entity, Vec3 start, Vec3 direction, double maxLength) {
        Vec3 relative = entity.position().add(0, entity.getBbHeight() * 0.5, 0).subtract(start);
        double along = relative.dot(direction);
        if (along < 0 || along > maxLength) return Double.MAX_VALUE;
        return relative.subtract(direction.scale(along)).lengthSqr();
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }
}
