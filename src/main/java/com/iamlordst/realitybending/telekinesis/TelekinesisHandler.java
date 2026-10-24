package com.iamlordst.realitybending.telekinesis;

import com.iamlordst.realitybending.item.TelekineticGlove;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraftforge.event.TickEvent;

public final class TelekinesisHandler {
    private TelekinesisHandler() {}

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof TelekineticGlove)
            || !stack.getOrCreateTag().getBoolean("TelekinesisEnabled")) return;

        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(14.0));
        AABB searchBox = player.getBoundingBox().expandTowards(player.getLookAngle().scale(14.0)).inflate(1.5);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, searchBox,
            entity -> entity.isAlive() && entity instanceof LivingEntity && entity != player,
            14.0 * 14.0);
        if (hit == null) return;

        Entity target = hit.getEntity();
        Vec3 holdPoint = start.add(player.getLookAngle().scale(5.0));
        Vec3 difference = holdPoint.subtract(target.position().add(0, target.getBbHeight() * 0.5, 0));
        Vec3 velocity = difference.scale(0.22);
        double length = velocity.length();
        if (length > 0.75) velocity = velocity.scale(0.75 / length);
        target.setDeltaMovement(velocity);
        target.hurtMarked = true;
        target.fallDistance = 0;
    }
}
