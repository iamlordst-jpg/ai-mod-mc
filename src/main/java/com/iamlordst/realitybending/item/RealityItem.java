package com.iamlordst.realitybending.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import com.iamlordst.realitybending.entity.EnergyProjectile;
import com.iamlordst.realitybending.registry.ModEntities;
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

public class RealityItem extends Item {
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

    public Power getPowerType() {
        return power;
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
            spawnSignatureEffect(level, player);
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
        // Both techniques use Minecraft's two-handed draw pose while charging.
        // The custom entity supplies the energy visuals; vanilla particles are not needed.
        return power == Power.KAMEHAMEHA || power == Power.KI_BLAST ? UseAnim.BOW : UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, net.minecraft.world.entity.LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player) || !(level instanceof ServerLevel server) || remainingUseDuration % 2 != 0) return;
        Vec3 look = player.getLookAngle().normalize();
        Vec3 center = player.getEyePosition().add(look.scale(1.05));
        boolean chargeOrbExists = !server.getEntitiesOfClass(EnergyProjectile.class, player.getBoundingBox().inflate(4.0),
            e -> e.visualType() == 2 && e.ownerId() == player.getId()).isEmpty();
        if (!chargeOrbExists) {
            EnergyProjectile chargeOrb = new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(), level);
            chargeOrb.configure(2, 0.0F, player);
            chargeOrb.setPos(center.x, center.y, center.z);
            server.addFreshEntity(chargeOrb);
        }
        // Intentionally no vanilla END_ROD / flame / spark particles here.
        // The custom rendered charge sphere is the only charging visual.
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity, int timeLeft) {
        if (level.isClientSide || !(entity instanceof Player player)) return;
        int chargeTicks = getUseDuration(stack) - timeLeft;
        if (level instanceof ServerLevel chargeWorld) {
            for (EnergyProjectile chargeOrb : chargeWorld.getEntitiesOfClass(EnergyProjectile.class,
                    player.getBoundingBox().inflate(5.0),
                    e -> e.visualType() == 2 && e.ownerId() == player.getId())) chargeOrb.discard();
        }
        if (chargeTicks < 5 || player.getCooldowns().isOnCooldown(this)) return;
        player.getCooldowns().addCooldown(this, cooldown);
        if (power == Power.KI_BLAST) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 origin = player.getEyePosition().add(look.scale(0.9));
            EnergyProjectile blast = new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(), level);
            blast.configure(0, Math.min(1.0F, chargeTicks / 42.0F), player);
            blast.setPos(origin.x, origin.y, origin.z);
            blast.setDeltaMovement(look.scale(1.55));
            level.addFreshEntity(blast);
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
        net.minecraft.sounds.SoundEvent sound = switch (power) {
            case FORUM_PHANTOM_MASK -> ModSounds.FEDORA_ACTIVATE.get();
            case BLACK_HOLE, VOID_PULSE -> ModSounds.VOID_CAST.get();
            case REALITY_RIFT, POCKET_DIMENSION -> ModSounds.RIFT_CAST.get();
            case LIGHTNING_SPEAR -> ModSounds.LIGHTNING_CAST.get();
            case TIME_WARP -> ModSounds.TIME_WARP_CAST.get();
            case MIND_READING -> ModSounds.TELEPATHY_CAST.get();
            case GRAVITY_BOOTS -> ModSounds.GRAVITY_CAST.get();
            case PLOT_ARMOR -> ModSounds.DEFENSE_CAST.get();
            case CHAOS_DICE -> ModSounds.CHAOS_CAST.get();
            case ADMIN_WAND -> ModSounds.ADMIN_CAST.get();
            case PHASE_SHIFT -> ModSounds.PHASE_CAST.get();
            case LAG_SPIKE -> ModSounds.LAG_CAST.get();
            case REALITY_CHECK, X_RAY_VISION -> ModSounds.REVEAL_CAST.get();
            case MEME_BEACON -> ModSounds.MEME_BEACON_CAST.get();
            default -> ModSounds.POWER_CAST.get();
        };
        float pitch = 0.9F + level.random.nextFloat() * 0.25F;
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

    /** A distinct, lightweight particle signature for every power activation. */
    private void spawnSignatureEffect(Level level, Player player) {
        if (!(level instanceof ServerLevel server)) return;
        Vec3 look = player.getLookAngle().normalize();
        Vec3 center = player.getEyePosition().add(look.scale(1.15));
        switch (power) {
            case FORUM_PHANTOM_MASK -> {
                server.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 22, .35, .45, .35, .025);
                sendRing(server, center, ParticleTypes.PORTAL, 1.0, 18);
            }
            case REALITY_CHECK -> sendRing(server, center, ParticleTypes.END_ROD, 1.45, 28);
            case PLOT_ARMOR -> {
                sendRing(server, player.position().add(0, .6, 0), ParticleTypes.TOTEM_OF_UNDYING, 1.0, 24);
                sendRing(server, player.position().add(0, 1.5, 0), ParticleTypes.END_ROD, .8, 18);
            }
            case LAG_SPIKE -> {
                server.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 30, .55, .45, .55, .12);
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 18, .4, .4, .4, .1);
            }
            case POCKET_DIMENSION, REALITY_RIFT -> {
                sendRing(server, center, ParticleTypes.PORTAL, 1.25, 32);
                sendRing(server, center.add(0, .25, 0), ParticleTypes.REVERSE_PORTAL, .8, 24);
            }
            case ADMIN_WAND -> {
                server.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 24, .2, .7, .2, .02);
                sendRing(server, center, ParticleTypes.ENCHANT, .7, 16);
            }
            case CHAOS_DICE -> {
                server.sendParticles(ParticleTypes.ENCHANT, center.x, center.y, center.z, 36, .6, .6, .6, .2);
                sendRing(server, center, ParticleTypes.WITCH, 1.0, 20);
            }
            case MEME_BEACON -> {
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y, center.z, 28, .6, .5, .6, .08);
                server.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 12, .25, .7, .25, .04);
            }
            case GRAVITY_BOOTS -> {
                sendRing(server, player.position().add(0, .15, 0), ParticleTypes.CLOUD, .85, 24);
                server.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + .2, player.getZ(), 14, .25, .05, .25, .03);
            }
            case KI_BLAST, KAMEHAMEHA -> {
                // These powers use the custom 3D energy renderer, not vanilla particles.
            }
            case X_RAY_VISION, MIND_READING -> {
                sendRing(server, center, ParticleTypes.ENCHANT, 1.0, 24);
                server.sendParticles(ParticleTypes.WITCH, center.x, center.y, center.z, 16, .4, .4, .4, .04);
            }
            case TIME_WARP -> {
                sendRing(server, center, ParticleTypes.ELECTRIC_SPARK, 1.2, 32);
                server.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 18, .35, .35, .35, .03);
            }
            case PHASE_SHIFT -> {
                sendRing(server, player.position().add(0, .8, 0), ParticleTypes.PORTAL, .8, 24);
                server.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + .8, player.getZ(), 24, .3, .6, .3, .08);
            }
            case VOID_PULSE -> {
                sendRing(server, center, ParticleTypes.REVERSE_PORTAL, 1.5, 36);
                server.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 28, .5, .5, .5, .12);
            }
            case BLACK_HOLE -> {
                for (int i = 0; i < 40; i++) {
                    double angle = i * Math.PI / 10.0;
                    double radius = .25 + (i / 40.0) * 1.4;
                    server.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        center.x + Math.cos(angle) * radius, center.y + Math.sin(angle * 1.4) * radius,
                        center.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
                }
            }
            case LIGHTNING_SPEAR -> {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 36, .4, .5, .4, .2);
                sendRing(server, center, ParticleTypes.FLASH, .45, 8);
            }
        }
    }

    private void sendRing(ServerLevel server, Vec3 center, ParticleOptions particle, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = 2.0 * Math.PI * i / count;
            server.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y,
                center.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
        }
    }

    public static void fireKamehameha(Level level, Player player, double charge) {
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
        EnergyProjectile beamVisual = new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(), level);
        beamVisual.configure(1, (float)charge, player);
        beamVisual.setBeamLength((float)length);
        beamVisual.setPos(start.x, start.y, start.z);
        beamVisual.setDeltaMovement(direction);
        level.addFreshEntity(beamVisual);
        // The custom renderer draws the full beam. Avoid the old vanilla particle trail/orbs.

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

    @Override public boolean isFoil(ItemStack stack) { return false; }
}
