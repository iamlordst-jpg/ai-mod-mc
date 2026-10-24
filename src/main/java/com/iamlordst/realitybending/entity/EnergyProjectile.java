package com.iamlordst.realitybending.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EnergyProjectile extends Entity {
    private static final EntityDataAccessor<Integer> TYPE=SynchedEntityData.defineId(EnergyProjectile.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CHARGE=SynchedEntityData.defineId(EnergyProjectile.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER=SynchedEntityData.defineId(EnergyProjectile.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LENGTH=SynchedEntityData.defineId(EnergyProjectile.class,EntityDataSerializers.FLOAT);
    public EnergyProjectile(EntityType<? extends EnergyProjectile> type,Level level){super(type,level);noPhysics=true;}
    @Override protected void defineSynchedData(){entityData.define(TYPE,0);entityData.define(CHARGE,.3F);entityData.define(OWNER,-1);entityData.define(LENGTH,24F);}
    public void configure(int type,float charge,Entity owner){entityData.set(TYPE,type);entityData.set(CHARGE,charge);entityData.set(OWNER,owner==null?-1:owner.getId());}
    public int visualType(){return entityData.get(TYPE);} public float charge(){return entityData.get(CHARGE);}
    public int ownerId(){return entityData.get(OWNER);} public float beamLength(){return entityData.get(LENGTH);}
    public void setBeamLength(float length){entityData.set(LENGTH,Math.max(1,length));}
    @Override public void tick(){
        super.tick();
        if(visualType()==2){
            Entity owner=level().getEntity(ownerId());
            if(!(owner instanceof Player player)||!player.isAlive()){if(tickCount>4)discard();return;}
            Vec3 c=player.getEyePosition().add(player.getLookAngle().normalize().scale(1.05));setPos(c.x,c.y,c.z);
            if(!level().isClientSide){
                boolean active=player.getPersistentData().getBoolean("realitybending_ability_charging");
                if(!active){discard();return;}
                entityData.set(CHARGE,Math.min(1F,Math.max(0,(player.tickCount-player.getPersistentData().getInt("realitybending_ability_start_tick"))/42F)));
                if(player.tickCount%2==0&&level() instanceof ServerLevel s){
                    s.sendParticles(ParticleTypes.GLOW,c.x,c.y,c.z,8,.2,.2,.2,.03);
                    s.sendParticles(ParticleTypes.ELECTRIC_SPARK,c.x,c.y,c.z,12,.18,.18,.18,.14);
                    s.sendParticles(ParticleTypes.END_ROD,c.x,c.y,c.z,7,.14,.14,.14,.05);
                }
            }
            return;
        }
        if(visualType()==1){if(tickCount>12)discard();return;}
        if(visualType()==3){
            if(tickCount>105){if(level() instanceof ServerLevel s)s.sendParticles(ParticleTypes.POOF,getX(),getY(),getZ(),40,1,1,1,.08);discard();return;}
            if(level().isClientSide)return;
            double radius=7.0+charge()*2.0;Vec3 center=position();
            if(level() instanceof ServerLevel s){
                s.sendParticles(ParticleTypes.REVERSE_PORTAL,getX(),getY(),getZ(),12,.65,.65,.65,.1);
                s.sendParticles(ParticleTypes.DRAGON_BREATH,getX(),getY(),getZ(),7,.5,.5,.5,.035);
                if(tickCount%3==0)s.sendParticles(ParticleTypes.ELECTRIC_SPARK,getX(),getY(),getZ(),12,1,1,1,.14);
            }
            AABB area=new AABB(getX()-radius,getY()-radius,getZ()-radius,getX()+radius,getY()+radius,getZ()+radius);
            for(LivingEntity target:level().getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()&&e.getId()!=ownerId()&&!(e instanceof Player))){
                Vec3 delta=center.subtract(target.position().add(0,target.getBbHeight()*.5,0));double distance=Math.max(.5,delta.length());
                double strength=Math.min(.75,.14+(radius-Math.min(radius,distance))*.085);
                target.setDeltaMovement(target.getDeltaMovement().add(delta.normalize().scale(strength)));target.hurtMarked=true;
                if(distance<1.15&&tickCount>12){
                    Vec3 at=target.position();target.discard();
                    if(level() instanceof ServerLevel s){s.sendParticles(ParticleTypes.REVERSE_PORTAL,at.x,at.y+target.getBbHeight()*.5,at.z,24,.25,.35,.25,.09);s.sendParticles(ParticleTypes.POOF,at.x,at.y,at.z,10,.2,.2,.2,.04);}
                }
            }
            return;
        }
        Vec3 motion=getDeltaMovement();setPos(getX()+motion.x,getY()+motion.y,getZ()+motion.z);
        if(tickCount>55||!level().isLoaded(blockPosition())){discard();return;}
        if(level().isClientSide)return;
        if((visualType()==0||visualType()==4)&&level() instanceof ServerLevel s){
            s.sendParticles(visualType()==4?ParticleTypes.ELECTRIC_SPARK:ParticleTypes.END_ROD,getX(),getY(),getZ(),visualType()==4?10:7,.12,.12,.12,.12);
            s.sendParticles(ParticleTypes.GLOW,getX(),getY(),getZ(),4,.08,.08,.08,.03);
        }
        if(!level().getBlockState(blockPosition()).getCollisionShape(level(),blockPosition()).isEmpty()){discard();return;}
        for(LivingEntity target:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(visualType()==4?.35:.26),e->e.isAlive()&&e.getId()!=ownerId())){
            Entity owner=level().getEntity(ownerId());
            target.hurt(owner instanceof Player p?level().damageSources().playerAttack(p):level().damageSources().magic(),(visualType()==4?10F:7F)+charge()*8F);
            target.setDeltaMovement(target.getDeltaMovement().add(motion.normalize().scale(visualType()==4?1.0:.65)));target.hurtMarked=true;discard();break;
        }
    }
    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag t){entityData.set(TYPE,t.getInt("Type"));entityData.set(CHARGE,t.getFloat("Charge"));entityData.set(OWNER,t.getInt("Owner"));entityData.set(LENGTH,t.getFloat("Length"));}
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag t){t.putInt("Type",visualType());t.putFloat("Charge",charge());t.putInt("Owner",ownerId());t.putFloat("Length",beamLength());}
    @Override public boolean isPickable(){return false;}
    @Override public boolean displayFireAnimation(){return false;}
}
