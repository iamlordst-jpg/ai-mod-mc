package com.iamlordst.realitybending.entity;

import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EnergyProjectile extends Entity {
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(EnergyProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CHARGE = SynchedEntityData.defineId(EnergyProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(EnergyProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(EnergyProjectile.class, EntityDataSerializers.FLOAT);

    public EnergyProjectile(EntityType<? extends EnergyProjectile> type, Level level) { super(type, level); noPhysics = true; }
    @Override protected void defineSynchedData() {
        entityData.define(TYPE, 0); entityData.define(CHARGE, 0.3F);
        entityData.define(OWNER, -1); entityData.define(LENGTH, 24.0F);
    }
    public void configure(int type, float charge, Entity owner) {
        entityData.set(TYPE, type); entityData.set(CHARGE, charge);
        entityData.set(OWNER, owner == null ? -1 : owner.getId());
    }
    public int visualType() { return entityData.get(TYPE); }
    public float charge() { return entityData.get(CHARGE); }
    public int ownerId() { return entityData.get(OWNER); }
    public float beamLength() { return entityData.get(LENGTH); }
    public void setBeamLength(float length) { entityData.set(LENGTH, Math.max(1.0F, length)); }

    @Override public void tick() {
        super.tick();
        if (visualType() == 2) {
            Entity owner = level().getEntity(ownerId());
            if (!(owner instanceof Player player) || !player.isAlive() || !player.isUsingItem()) { discard(); return; }
            Vec3 center = player.getEyePosition().add(player.getLookAngle().normalize().scale(1.05));
            setPos(center.x, center.y, center.z);
            entityData.set(CHARGE, Math.min(1.0F, (72000 - player.getUseItemRemainingTicks()) / 42.0F));
            return;
        }
        if (visualType() == 1) { if (tickCount > 12) discard(); return; }
        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        if (tickCount > 55 || !level().isLoaded(blockPosition())) { discard(); return; }
        if (level().isClientSide) return;
        if (!level().getBlockState(blockPosition()).getCollisionShape(level(), blockPosition()).isEmpty()) { discard(); return; }
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.22),
                e -> e.isAlive() && e.getId() != ownerId())) {
            Entity owner = level().getEntity(ownerId());
            target.hurt(owner instanceof Player p ? level().damageSources().playerAttack(p) : level().damageSources().magic(), 7.0F + charge() * 8.0F);
            target.setDeltaMovement(target.getDeltaMovement().add(motion.normalize().scale(0.65)));
            target.hurtMarked = true; discard(); break;
        }
    }
    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        entityData.set(TYPE, tag.getInt("Type")); entityData.set(CHARGE, tag.getFloat("Charge"));
        entityData.set(OWNER, tag.getInt("Owner")); entityData.set(LENGTH, tag.getFloat("Length"));
    }
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        tag.putInt("Type", visualType()); tag.putFloat("Charge", charge());
        tag.putInt("Owner", ownerId()); tag.putFloat("Length", beamLength());
    }
    @Override public boolean isPickable() { return false; }
    @Override public boolean displayFireAnimation() { return false; }
}
