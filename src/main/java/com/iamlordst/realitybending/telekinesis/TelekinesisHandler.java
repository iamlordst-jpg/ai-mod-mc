package com.iamlordst.realitybending.telekinesis;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraftforge.event.TickEvent;

import java.util.UUID;

public final class TelekinesisHandler {
    private static final String HELD_MOST = "realitybending_held_most";
    private static final String HELD_LEAST = "realitybending_held_least";
    private static final double RANGE = 32.0;

    private TelekinesisHandler() {}

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        if (!player.getPersistentData().getBoolean("realitybending_telekinesis_unlocked")
            || !player.getPersistentData().getBoolean("realitybending_telekinesis_enabled")) {
            releaseHeld(player);
            return;
        }

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = start.add(look.scale(RANGE));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(3.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, searchBox,
            entity -> entity.isAlive() && entity instanceof LivingEntity && entity != player,
            RANGE * RANGE);

        Entity previous = getHeld(player);
        Entity target = hit == null ? null : hit.getEntity();
        if (previous != null && previous != target) previous.noPhysics = false;
        if (target == null) {
            clearHeld(player);
            return;
        }

        player.getPersistentData().putLong(HELD_MOST, target.getUUID().getMostSignificantBits());
        player.getPersistentData().putLong(HELD_LEAST, target.getUUID().getLeastSignificantBits());
        target.noPhysics = true;
        Vec3 holdPoint = start.add(look.scale(4.5));
        Vec3 center = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 difference = holdPoint.subtract(center);
        Vec3 velocity = difference.scale(0.48);
        double length = velocity.length();
        if (length > 2.5) velocity = velocity.scale(2.5 / length);
        target.setDeltaMovement(velocity);
        target.hurtMarked = true;
        target.fallDistance = 0;
    }

    private static Entity getHeld(Player player) {
        if (!player.getPersistentData().contains(HELD_MOST) || !player.getPersistentData().contains(HELD_LEAST)) return null;
        UUID uuid = new UUID(player.getPersistentData().getLong(HELD_MOST), player.getPersistentData().getLong(HELD_LEAST));
        return player.level().getEntity(uuid);
    }

    private static void clearHeld(Player player) {
        player.getPersistentData().remove(HELD_MOST);
        player.getPersistentData().remove(HELD_LEAST);
    }

    private static void releaseHeld(Player player) {
        Entity previous = getHeld(player);
        if (previous != null) previous.noPhysics = false;
        clearHeld(player);
    }
}
