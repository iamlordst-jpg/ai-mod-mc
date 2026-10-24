package com.iamlordst.realitybending.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import java.util.List;

public final class RealityItem extends Item {
    public enum Power { RIFT_PEARL, GRAVITY_CORE, CHRONO_SHARD, PHASE_CRYSTAL, RETURN_ANCHOR,
        BLINK_CHARM, NULL_ORB, MIRROR_TOKEN, TIME_BOTTLE, ECHO_STEP }
    private final Power power;
    private final int cooldown;
    public RealityItem(Power power, int cooldown, Properties properties) {
        super(properties); this.power = power; this.cooldown = cooldown;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (activate(level, player, stack)) {
            player.getCooldowns().addCooldown(this, cooldown);
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 0.8F + level.random.nextFloat() * 0.4F);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }
    private boolean activate(Level level, Player player, ItemStack stack) {
        switch (power) {
            case RIFT_PEARL -> {
                BlockHitResult hit = raycast(level, player, 18.0);
                Vec3 dest = hit.getType() == HitResult.Type.MISS
                    ? player.position().add(player.getLookAngle().scale(10))
                    : hit.getLocation().subtract(player.getLookAngle().scale(1.0));
                return safeTeleport(level, player, dest.x, dest.y, dest.z);
            }
            case GRAVITY_CORE -> {
                List<Entity> list = level.getEntities(player, player.getBoundingBox().inflate(9),
                    e -> e.isAlive() && !(e instanceof Projectile));
                if (list.isEmpty()) return false;
                for (Entity e : list) {
                    Vec3 v = player.position().add(0, .8, 0).subtract(e.position());
                    if (v.lengthSqr() > .01) { e.setDeltaMovement(e.getDeltaMovement().add(v.normalize().scale(.65))); e.hurtMarked = true; }
                }
                return true;
            }
            case CHRONO_SHARD, TIME_BOTTLE -> {
                List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(power == Power.TIME_BOTTLE ? 7 : 8), e -> e != player);
                if (list.isEmpty()) return false;
                for (LivingEntity e : list) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, power == Power.TIME_BOTTLE ? 140 : 100, power == Power.TIME_BOTTLE ? 6 : 3));
                    if (power == Power.CHRONO_SHARD) e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                    else e.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 140, 2));
                }
                return true;
            }
            case PHASE_CRYSTAL -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 160, 1)); return true;
            }
            case RETURN_ANCHOR -> {
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.contains("ReturnX")) {
                    if (!tag.getString("ReturnDimension").equals(level.dimension().location().toString())) {
                        player.displayClientMessage(Component.literal("Return in the dimension where you saved the anchor."), true); return false;
                    }
                    boolean ok = safeTeleport(level, player, tag.getDouble("ReturnX"), tag.getDouble("ReturnY"), tag.getDouble("ReturnZ"));
                    if (ok) { tag.remove("ReturnX"); tag.remove("ReturnY"); tag.remove("ReturnZ"); tag.remove("ReturnDimension"); }
                    return ok;
                }
                tag.putDouble("ReturnX", player.getX()); tag.putDouble("ReturnY", player.getY()); tag.putDouble("ReturnZ", player.getZ());
                tag.putString("ReturnDimension", level.dimension().location().toString());
                player.displayClientMessage(Component.literal("Return Anchor saved. Use again to return."), true); return true;
            }
            case BLINK_CHARM -> {
                player.setDeltaMovement(player.getLookAngle().normalize().scale(1.65).add(0, .22, 0));
                player.hurtMarked = true; player.fallDistance = 0; return true;
            }
            case NULL_ORB -> {
                List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6), e -> e != player);
                if (list.isEmpty()) return false;
                for (LivingEntity e : list) {
                    Vec3 away = e.position().subtract(player.position());
                    if (away.lengthSqr() > .01) { e.setDeltaMovement(e.getDeltaMovement().add(away.normalize().scale(1.25)).add(0,.35,0)); e.hurtMarked = true; }
                }
                return true;
            }
            case MIRROR_TOKEN -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 1)); return true;
            }
            case ECHO_STEP -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 240, 1));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 240, 0)); return true;
            }
        }
        return false;
    }
    private static BlockHitResult raycast(Level level, Player player, double distance) {
        Vec3 start = player.getEyePosition();
        return level.clip(new ClipContext(start, start.add(player.getLookAngle().scale(distance)),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }
    private static boolean safeTeleport(Level level, Player player, double x, double y, double z) {
        BlockPos base = BlockPos.containing(x, y, z);
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos feet = base.above(dy), head = feet.above(), floor = feet.below();
            if (level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(head).getCollisionShape(level, head).isEmpty()
                && !level.getBlockState(floor).getCollisionShape(level, floor).isEmpty()) {
                player.teleportTo(feet.getX()+.5, feet.getY(), feet.getZ()+.5); player.fallDistance = 0; return true;
            }
        }
        player.displayClientMessage(Component.literal("No safe landing spot for reality shift."), true); return false;
    }
    @Override public boolean isFoil(ItemStack stack) { return true; }
}
