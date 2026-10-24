package com.iamlordst.realitybending.network;

import com.iamlordst.realitybending.entity.EnergyProjectile;
import com.iamlordst.realitybending.item.RealityItem;
import com.iamlordst.realitybending.registry.ModEntities;
import com.iamlordst.realitybending.registry.ModItems;
import com.iamlordst.realitybending.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public final class PowerAbilityPacket {
    private final int ability;
    private final boolean charging;
    public PowerAbilityPacket(int ability,boolean charging){this.ability=ability;this.charging=charging;}
    public static void encode(PowerAbilityPacket p,net.minecraft.network.FriendlyByteBuf b){b.writeVarInt(p.ability);b.writeBoolean(p.charging);}
    public static PowerAbilityPacket decode(net.minecraft.network.FriendlyByteBuf b){return new PowerAbilityPacket(b.readVarInt(),b.readBoolean());}
    private static net.minecraft.world.item.Item item(int a){return switch(a){case 0->ModItems.KI_BLAST.get();case 1->ModItems.KAMEHAMEHA.get();case 2->ModItems.BLACK_HOLE.get();case 3->ModItems.GRAVITY_BOOTS.get();case 4->ModItems.LIGHTNING_SPEAR.get();default->ModItems.VOID_PULSE.get();};}
    private static String name(int a){return switch(a){case 0->"KI BLAST";case 1->"KAMEHAMEHA";case 2->"BLACK HOLE";case 3->"SPIRIT BOMB";case 4->"INSTANT TRANSMISSION";default->"TIME STOP";};}
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
                var sound=p.ability==0?ModSounds.KI_CHARGE.get():p.ability==1?ModSounds.KAMEHAMEHA_CHARGE.get():ModSounds.VOID_CAST.get();
                player.level().playSound(null,player.blockPosition(),sound,net.minecraft.sounds.SoundSource.PLAYERS,p.ability==2?2.8F:1.7F,p.ability==1?.72F:.88F);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(name(p.ability)+" — charging"),true);return;
            }
            if(!data.getBoolean(active)||data.getInt(type)!=p.ability)return;
            int ticks=Math.max(0,player.tickCount-data.getInt(start));data.putBoolean(active,false);
            for(EnergyProjectile e:player.level().getEntitiesOfClass(EnergyProjectile.class,player.getBoundingBox().inflate(9),e->e.visualType()==2&&e.ownerId()==player.getId()))e.discard();
            if(ticks<4)return;
            float charge=Math.min(1,ticks/(p.ability==1?48F:36F));
            player.getCooldowns().addCooldown(item(p.ability),switch(p.ability){case 0->10;case 1->90;case 2->180;case 3->120;case 4->60;default->160;});
            Vec3 look=player.getLookAngle().normalize(),origin=player.getEyePosition().add(look.scale(1.1));
            ServerLevel level=(ServerLevel)player.level();
            switch(p.ability){
                case 0->{
                    EnergyProjectile blast=new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(),level);blast.configure(0,charge,player);blast.setPos(origin.x,origin.y,origin.z);blast.setDeltaMovement(look.scale(1.8+charge*1.1));level.addFreshEntity(blast);
                    level.sendParticles(ParticleTypes.FLASH,origin.x,origin.y,origin.z,1,0,0,0,0);level.sendParticles(ParticleTypes.ELECTRIC_SPARK,origin.x,origin.y,origin.z,65,.3,.3,.3,.2);level.sendParticles(ParticleTypes.END_ROD,origin.x,origin.y,origin.z,35,.2,.2,.2,.1);level.sendParticles(ParticleTypes.GLOW,origin.x,origin.y,origin.z,28,.22,.22,.22,.06);
                    level.playSound(null,player.blockPosition(),ModSounds.KI_RELEASE.get(),net.minecraft.sounds.SoundSource.PLAYERS,2.2F,.75F);
                }
                case 1->{
                    level.sendParticles(ParticleTypes.FLASH,origin.x,origin.y,origin.z,1,0,0,0,0);level.sendParticles(ParticleTypes.ELECTRIC_SPARK,origin.x,origin.y,origin.z,70,.35,.35,.35,.2);level.sendParticles(ParticleTypes.END_ROD,origin.x,origin.y,origin.z,42,.3,.3,.3,.12);
                    RealityItem.fireKamehameha(level,player,charge);
                }
                case 2->{
                    Vec3 c=player.getEyePosition().add(look.scale(5.0));EnergyProjectile hole=new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(),level);hole.configure(3,charge,player);hole.setPos(c.x,c.y,c.z);level.addFreshEntity(hole);
                    level.sendParticles(ParticleTypes.FLASH,c.x,c.y,c.z,1,0,0,0,0);level.sendParticles(ParticleTypes.REVERSE_PORTAL,c.x,c.y,c.z,160,1.4,1.4,1.4,.2);level.sendParticles(ParticleTypes.DRAGON_BREATH,c.x,c.y,c.z,100,1.1,1.1,1.1,.08);level.sendParticles(ParticleTypes.END_ROD,c.x,c.y,c.z,45,1,1,1,.1);
                    level.playSound(null,BlockPos.containing(c),ModSounds.VOID_CAST.get(),net.minecraft.sounds.SoundSource.HOSTILE,4.0F,.48F);
                    level.playSound(null,BlockPos.containing(c),net.minecraft.sounds.SoundEvents.WARDEN_SONIC_BOOM,net.minecraft.sounds.SoundSource.HOSTILE,3.5F,.55F);
                }
                case 3->{ // Spirit Bomb: charge-scaled, luminous sphere and radial impact.
                    Vec3 c=player.getEyePosition().add(look.scale(7.0));
                    EnergyProjectile bomb=new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(),level);bomb.configure(0,charge,player);bomb.setPos(c.x,c.y,c.z);bomb.setDeltaMovement(look.scale(.72+charge*.35));level.addFreshEntity(bomb);
                    level.sendParticles(ParticleTypes.FLASH,c.x,c.y,c.z,1,0,0,0,0);level.sendParticles(ParticleTypes.END_ROD,c.x,c.y,c.z,100,.8,.8,.8,.15);level.sendParticles(ParticleTypes.HAPPY_VILLAGER,c.x,c.y,c.z,65,1,1,1,.08);
                    level.playSound(null,player.blockPosition(),ModSounds.KAMEHAMEHA_RELEASE.get(),net.minecraft.sounds.SoundSource.PLAYERS,2.0F,.55F);
                }
                case 4->{ // Instant Transmission: blink to a safe point near the aim ray.
                    Vec3 destination=player.position().add(look.scale(12.0+charge*8.0));
                    BlockPos base=BlockPos.containing(destination);boolean moved=false;
                    for(int dy=2;dy>=-3;dy--){BlockPos feet=base.offset(0,dy,0);
                        if(level.getBlockState(feet).getCollisionShape(level,feet).isEmpty()&&level.getBlockState(feet.above()).getCollisionShape(level,feet.above()).isEmpty()&&!level.getBlockState(feet.below()).getCollisionShape(level,feet.below()).isEmpty()){
                            Vec3 from=player.position();level.sendParticles(ParticleTypes.PORTAL,from.x,from.y+1,from.z,55,.45,.7,.45,.15);
                            player.teleportTo(feet.getX()+.5,feet.getY(),feet.getZ()+.5);player.fallDistance=0;
                            level.sendParticles(ParticleTypes.PORTAL,player.getX(),player.getY()+1,player.getZ(),65,.5,.7,.5,.18);
                            level.playSound(null,BlockPos.containing(from),net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,net.minecraft.sounds.SoundSource.PLAYERS,2.2F,.7F);moved=true;break;
                        }}
                    if(!moved)player.displayClientMessage(net.minecraft.network.chat.Component.literal("No safe destination"),true);
                }
                case 5->{ // Time Stop: briefly lock nearby non-player living entities.
                    Vec3 c=player.position().add(0,.8,0);level.sendParticles(ParticleTypes.FLASH,c.x,c.y,c.z,1,0,0,0,0);level.sendParticles(ParticleTypes.REVERSE_PORTAL,c.x,c.y,c.z,140,5,2.5,5,.05);level.sendParticles(ParticleTypes.END_ROD,c.x,c.y,c.z,70,4,2,4,.04);
                    for(LivingEntity e:level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(12),e->e.isAlive()&&e!=player&&!(e instanceof net.minecraft.world.entity.player.Player))){
                        e.getPersistentData().putInt("realitybending_time_stop_until",level.getServer().getTickCount()+Math.min(100,45+(int)(charge*55)));
                        e.setDeltaMovement(Vec3.ZERO);e.hurtMarked=true;
                    }
                    level.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.WARDEN_HEARTBEAT,net.minecraft.sounds.SoundSource.PLAYERS,2.5F,.55F);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}