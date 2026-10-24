package com.iamlordst.realitybending.telekinesis;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

public final class TelekinesisHandler {
    private static final String HELD_ID = "realitybending_held_entity_id";
    private static final double RANGE = 40.0;
    private static final double HOLD_DISTANCE = 5.5;

    private TelekinesisHandler() {}

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        // Apply the brief Time Stop field while a player is ticking in the affected area.
        if (player.level() instanceof ServerLevel world) {
            int now = world.getServer().getTickCount();
            for (net.minecraft.world.entity.Mob entity : world.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(32.0), e -> e != player && e.getPersistentData().contains("realitybending_time_stop_until"))) {
                var data = entity.getPersistentData();
                if (data.getInt("realitybending_time_stop_until") > now) {
                    if (!data.contains("realitybending_time_stop_old_noai")) data.putBoolean("realitybending_time_stop_old_noai", entity.isNoAi());
                    entity.setNoAi(true);
                    entity.setDeltaMovement(Vec3.ZERO);
                    entity.hurtMarked = true;
                } else {
                    entity.setNoAi(data.getBoolean("realitybending_time_stop_old_noai"));
                    data.remove("realitybending_time_stop_old_noai");
                    data.remove("realitybending_time_stop_until");
                }
            }
        }
        if (!player.getPersistentData().getBoolean("realitybending_telekinesis_enabled")) {
            releaseHeld(player);
            return;
        }

        Entity target = getHeld(player);
        // Keep the current target locked while the camera moves. Only acquire a new
        // target when nothing is already held, so a small aim adjustment won't drop it.
        if (target == null || !target.isAlive() || target.distanceToSqr(player) > RANGE * RANGE) {
            if (target != null) {
                target.noPhysics = false;
                target.setNoGravity(false);
            }
            clearHeld(player);
            Vec3 start = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            Vec3 end = start.add(look.scale(RANGE));
            AABB searchBox = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(4.0);
            EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, searchBox,
                entity -> entity.isAlive() && entity instanceof LivingEntity && entity != player,
                RANGE * RANGE);
            target = hit == null ? null : hit.getEntity();
            if (target == null) return;
            player.getPersistentData().putInt(HELD_ID, target.getId());
        }

        Vec3 look = player.getLookAngle().normalize();
        Vec3 holdPoint = player.getEyePosition().add(look.scale(HOLD_DISTANCE));
        Vec3 center = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 difference = holdPoint.subtract(center);
        // Strong spring-like tracking with a generous speed cap; camera motion is followed
        // continuously rather than relying on the crosshair to keep hitting the entity.
        Vec3 velocity = target.getDeltaMovement().scale(0.18).add(difference.scale(0.72));
        double length = velocity.length();
        if (length > 3.4) velocity = velocity.scale(3.4 / length);

        target.noPhysics = true; // prevents player/entity collision from shoving the target away
        target.setNoGravity(true);
        target.setDeltaMovement(velocity);
        target.hurtMarked = true;
        target.fallDistance = 0;

        if (player.level() instanceof ServerLevel server && player.tickCount % 2 == 0) {
            double y = target.getY() + target.getBbHeight() * 0.5;
            server.sendParticles(ParticleTypes.ENCHANT, target.getX(), y, target.getZ(), 5, .35, .45, .35, .08);
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), y, target.getZ(), 2, .2, .25, .2, .01);
            server.sendParticles(ParticleTypes.END_ROD, holdPoint.x, holdPoint.y, holdPoint.z, 1, .05, .05, .05, .01);
        }
    }

    private static Entity getHeld(Player player) {
        if (!player.getPersistentData().contains(HELD_ID)) return null;
        return player.level().getEntity(player.getPersistentData().getInt(HELD_ID));
    }

    private static void clearHeld(Player player) {
        player.getPersistentData().remove(HELD_ID);
    }

    private static void releaseHeld(Player player) {
        Entity previous = getHeld(player);
        if (previous != null) {
            previous.noPhysics = false;
            previous.setNoGravity(false);
        }
        clearHeld(player);
    }
}
