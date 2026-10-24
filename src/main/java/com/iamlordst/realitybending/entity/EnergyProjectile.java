package com.iamlordst.realitybending.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;

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
                    s.sendParticles(ParticleTypes.GLOW,c.x,c.y,c.z,14,.24,.24,.24,.04);s.sendParticles(ParticleTypes.ELECTRIC_SPARK,c.x,c.y,c.z,18,.2,.2,.2,.16);s.sendParticles(ParticleTypes.END_ROD,c.x,c.y,c.z,10,.16,.16,.16,.06);
                }
            }return;
        }
        if(visualType()==1){if(tickCount>12)discard();return;}
        if(visualType()==3){
            if(tickCount>150){if(level() instanceof ServerLevel s)s.sendParticles(ParticleTypes.POOF,getX(),getY(),getZ(),50,1,1,1,.08);discard();return;}
            if(level().isClientSide)return;
            double radius=11.0+charge()*4.0;Vec3 center=position();
            if(level() instanceof ServerLevel s){
                s.sendParticles(ParticleTypes.REVERSE_PORTAL,getX(),getY(),getZ(),18,1,1,1,.12);s.sendParticles(ParticleTypes.DRAGON_BREATH,getX(),getY(),getZ(),12,.7,.7,.7,.04);
                if(tickCount%3==0)s.sendParticles(ParticleTypes.ELECTRIC_SPARK,getX(),getY(),getZ(),18,1.3,1.3,1.3,.16);
                // Pull a small, bounded shell of ordinary blocks toward the singularity.
                if(level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)&&tickCount%8==0){
                    BlockPos origin=blockPosition();int scanned=0;
                    for(BlockPos pos:BlockPos.betweenClosed(origin.offset(-4,-4,-4),origin.offset(4,4,4))){
                        if(scanned++>220)break;
                        if(pos.distSqr(origin)>16||level().getBlockEntity(pos)!=null)continue;
                        BlockState state=level().getBlockState(pos);
                        if(state.isAir()||state.getDestroySpeed(level(),pos)<0||state.getDestroySpeed(level(),pos)>4)continue;
                        if(level().random.nextFloat()<.16F){s.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,state),pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,4,.2,.2,.2,.08);level().destroyBlock(pos,false);}
                    }
                }
            }
            AABB area=new AABB(getX()-radius,getY()-radius,getZ()-radius,getX()+radius,getY()+radius,getZ()+radius);
            for(LivingEntity target:level().getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()&&e.getId()!=ownerId()&&!(e instanceof Player))){
                Vec3 delta=center.subtract(target.position().add(0,target.getBbHeight()*.5,0));double distance=Math.max(.5,delta.length());
                double strength=Math.min(1.25,.18+(radius-Math.min(radius,distance))*.105);
                target.setDeltaMovement(target.getDeltaMovement().add(delta.normalize().scale(strength)));target.hurtMarked=true;
                if(distance<1.35&&tickCount>12){Vec3 at=target.position();float h=target.getBbHeight();target.discard();if(level() instanceof ServerLevel s){s.sendParticles(ParticleTypes.REVERSE_PORTAL,at.x,at.y+h*.5,at.z,30,.3,.4,.3,.1);s.sendParticles(ParticleTypes.POOF,at.x,at.y,at.z,12,.2,.2,.2,.04);}}
            }return;
        }
        Vec3 motion=getDeltaMovement();Vec3 next=position().add(motion);setPos(next.x,next.y,next.z);
        if(tickCount>80||!level().isLoaded(blockPosition())){discard();return;}
        if(level().isClientSide)return;
        if((visualType()==0||visualType()==4)&&level() instanceof ServerLevel s){
            s.sendParticles(visualType()==4?ParticleTypes.ELECTRIC_SPARK:ParticleTypes.END_ROD,getX(),getY(),getZ(),visualType()==4?12:12,.14,.14,.14,.12);
            s.sendParticles(ParticleTypes.GLOW,getX(),getY(),getZ(),8,.1,.1,.1,.04);
            s.sendParticles(ParticleTypes.ELECTRIC_SPARK,getX(),getY(),getZ(),4,.1,.1,.1,.18);
        }
        BlockPos hitPos=blockPosition();
        if(!level().getBlockState(hitPos).getCollisionShape(level(),hitPos).isEmpty()){
            impact(hitPos);
            discard();return;
        }
        for(LivingEntity target:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(visualType()==4?.45:.38),e->e.isAlive()&&e.getId()!=ownerId())){
            Entity owner=level().getEntity(ownerId());
            target.hurt(owner instanceof Player p?level().damageSources().playerAttack(p):level().damageSources().magic(),(visualType()==4?10F:9F)+charge()*12F);
            target.setDeltaMovement(target.getDeltaMovement().add(motion.normalize().scale(visualType()==4?1.0:.9)));target.hurtMarked=true;
            impact(hitPos);discard();break;
        }
    }
    private void impact(BlockPos center){
        if(!(level() instanceof ServerLevel server))return;
        server.sendParticles(ParticleTypes.EXPLOSION,getX(),getY(),getZ(),2,.25,.25,.25,.01);
        server.sendParticles(ParticleTypes.ELECTRIC_SPARK,getX(),getY(),getZ(),55,.7,.7,.7,.2);
        server.playSound(null,center,net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,net.minecraft.sounds.SoundSource.BLOCKS,1.6F,.7F);
        if(!level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))return;
        int radius=visualType()==0?1:2;
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-radius,-radius,-radius),center.offset(radius,radius,radius))){
            if(level().getBlockEntity(pos)!=null)continue;
            BlockState state=level().getBlockState(pos);if(state.isAir()||state.getDestroySpeed(level(),pos)<0||state.getDestroySpeed(level(),pos)>5)continue;
            if(pos.distSqr(center)<=radius*radius+1)level().destroyBlock(pos,true);
        }
    }
    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag t){entityData.set(TYPE,t.getInt("Type"));entityData.set(CHARGE,t.getFloat("Charge"));entityData.set(OWNER,t.getInt("Owner"));entityData.set(LENGTH,t.getFloat("Length"));}
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag t){t.putInt("Type",visualType());t.putFloat("Charge",charge());t.putInt("Owner",ownerId());t.putFloat("Length",beamLength());}
    @Override public boolean isPickable(){return false;}
    @Override public boolean displayFireAnimation(){return false;}
}