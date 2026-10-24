package com.iamlordst.realitybending.network;

import com.iamlordst.realitybending.entity.EnergyProjectile;
import com.iamlordst.realitybending.item.RealityItem;
import com.iamlordst.realitybending.registry.ModEntities;
import com.iamlordst.realitybending.registry.ModItems;
import com.iamlordst.realitybending.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public final class PowerAbilityPacket {
    private final int ability;
    private final boolean charging;
    public PowerAbilityPacket(int ability,boolean charging){this.ability=ability;this.charging=charging;}
    public static void encode(PowerAbilityPacket p,FriendlyByteBuf b){b.writeVarInt(p.ability);b.writeBoolean(p.charging);}
    public static PowerAbilityPacket decode(FriendlyByteBuf b){return new PowerAbilityPacket(b.readVarInt(),b.readBoolean());}
    private static net.minecraft.world.item.Item item(int a){
        return switch(a){case 0->ModItems.KI_BLAST.get();case 1->ModItems.KAMEHAMEHA.get();case 2->ModItems.BLACK_HOLE.get();case 3->ModItems.GRAVITY_BOOTS.get();case 4->ModItems.LIGHTNING_SPEAR.get();default->ModItems.VOID_PULSE.get();};
    }
    private static String name(int a){return switch(a){case 0->"KI BLAST";case 1->"KAMEHAMEHA";case 2->"BLACK HOLE";case 3->"GRAVITY CRUSH";case 4->"LIGHTNING SPEAR";default->"VOID PULSE";};}
    public static void handle(PowerAbilityPacket p,Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context ctx=supplier.get();
        ctx.enqueueWork(()->{
            ServerPlayer player=ctx.getSender();
            if(player==null||p.ability<0||p.ability>5)return;
            var data=player.getPersistentData();
            String active="realitybending_ability_charging",type="realitybending_ability_type",start="realitybending_ability_start_tick";
            if(p.charging){
                if(player.isSpectator()||data.getBoolean(active)||player.getCooldowns().isOnCooldown(item(p.ability)))return;
                data.putBoolean(active,true);data.putInt(type,p.ability);data.putInt(start,player.tickCount);
                Vec3 look=player.getLookAngle().normalize(),c=player.getEyePosition().add(look.scale(1.0));
                EnergyProjectile orb=new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(),player.level());
                orb.configure(2,0,player);orb.setBeamLength(p.ability);orb.setPos(c.x,c.y,c.z);player.level().addFreshEntity(orb);
                player.level().playSound(null,player.blockPosition(),p.ability==0?ModSounds.KI_CHARGE.get():ModSounds.KAMEHAMEHA_CHARGE.get(),net.minecraft.sounds.SoundSource.PLAYERS,1.0F,p.ability==0?1.18F:.82F);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(name(p.ability)+" — charging"),true);return;
            }
            if(!data.getBoolean(active)||data.getInt(type)!=p.ability)return;
            int ticks=Math.max(0,player.tickCount-data.getInt(start));data.putBoolean(active,false);
            for(EnergyProjectile e:player.level().getEntitiesOfClass(EnergyProjectile.class,player.getBoundingBox().inflate(9),e->e.visualType()==2&&e.ownerId()==player.getId()))e.discard();
            if(ticks<4)return;
            float charge=Math.min(1,ticks/(p.ability==1?48F:32F));
            player.getCooldowns().addCooldown(item(p.ability),switch(p.ability){case 0->10;case 1->90;case 2->180;case 3->90;case 4->45;default->100;});
            Vec3 look=player.getLookAngle().normalize(),origin=player.getEyePosition().add(look.scale(1.1));
            ServerLevel level=(ServerLevel)player.level();
            switch(p.ability){
                case 0->{
                    EnergyProjectile blast=new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(),level);blast.configure(0,charge,player);blast.setPos(origin.x,origin.y,origin.z);blast.setDeltaMovement(look.scale(1.65+charge*.8));level.addFreshEntity(blast);
                    level.sendParticles(ParticleTypes.FLASH,origin.x,origin.y,origin.z,1,0,0,0,0);level.sendParticles(ParticleTypes.ELECTRIC_SPARK,origin.x,origin.y,origin.z,42,.28,.28,.28,.19);level.sendParticles(ParticleTypes.END_ROD,origin.x,origin.y,origin.z,24,.2,.2,.2,.09);level.sendParticles(ParticleTypes.GLOW,origin.x,origin.y,origin.z,18,.22,.22,.22,.05);
                    level.playSound(null,player.blockPosition(),ModSounds.KI_RELEASE.get(),net.minecraft.sounds.SoundSource.PLAYERS,1.4F,.9F);
                }
                case 1->{level.sendParticles(ParticleTypes.FLASH,origin.x,origin.y,origin.z,1,0,0,0,0);level.sendParticles(ParticleTypes.ELECTRIC_SPARK,origin.x,origin.y,origin.z,48,.35,.35,.35,.2);level.sendParticles(ParticleTypes.END_ROD,origin.x,origin.y,origin.z,32,.3,.3,.3,.12);RealityItem.fireKamehameha(level,player,charge);}
                case 2->{
                    Vec3 c=player.getEyePosition().add(look.scale(5.0));EnergyProjectile hole=new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(),level);hole.configure(3,charge,player);hole.setPos(c.x,c.y,c.z);level.addFreshEntity(hole);
                    level.sendParticles(ParticleTypes.FLASH,c.x,c.y,c.z,1,0,0,0,0);level.sendParticles(ParticleTypes.REVERSE_PORTAL,c.x,c.y,c.z,85,.8,.8,.8,.16);level.sendParticles(ParticleTypes.DRAGON_BREATH,c.x,c.y,c.z,55,.7,.7,.7,.06);level.sendParticles(ParticleTypes.END_ROD,c.x,c.y,c.z,24,.7,.7,.7,.08);
                }
                case 3->{
                    Vec3 c=player.getEyePosition().add(look.scale(4));AABB area=new AABB(c,c).inflate(5);
                    level.sendParticles(ParticleTypes.EXPLOSION,c.x,c.y,c.z,2,.4,.4,.4,.01);level.sendParticles(ParticleTypes.CLOUD,c.x,c.y,c.z,65,1.4,1.2,1.4,.08);level.sendParticles(ParticleTypes.ELECTRIC_SPARK,c.x,c.y,c.z,55,1.2,1.2,1.2,.16);
                    for(LivingEntity e:level.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()&&e!=player)){e.hurt(level.damageSources().playerAttack(player),7+charge*8);e.setDeltaMovement(e.getDeltaMovement().add(0,-.85,0));e.hurtMarked=true;}
                }
                case 4->{
                    EnergyProjectile spear=new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(),level);spear.configure(4,charge,player);spear.setPos(origin.x,origin.y,origin.z);spear.setDeltaMovement(look.scale(2.0+charge));level.addFreshEntity(spear);
                    level.sendParticles(ParticleTypes.FLASH,origin.x,origin.y,origin.z,1,0,0,0,0);level.sendParticles(ParticleTypes.ELECTRIC_SPARK,origin.x,origin.y,origin.z,55,.3,.3,.3,.23);level.sendParticles(ParticleTypes.END_ROD,origin.x,origin.y,origin.z,18,.2,.2,.2,.08);
                }
                case 5->{
                    Vec3 c=player.position().add(0,.8,0);level.sendParticles(ParticleTypes.FLASH,c.x,c.y,c.z,1,0,0,0,0);level.sendParticles(ParticleTypes.REVERSE_PORTAL,c.x,c.y,c.z,90,3,1.5,3,.16);level.sendParticles(ParticleTypes.END_ROD,c.x,c.y,c.z,45,2.4,1.2,2.4,.12);
                    for(LivingEntity e:level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(6),e->e.isAlive()&&e!=player)){Vec3 away=e.position().subtract(player.position()).normalize();e.hurt(level.damageSources().playerAttack(player),5+charge*7);e.setDeltaMovement(e.getDeltaMovement().add(away.scale(1.5+charge)));e.hurtMarked=true;}
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
