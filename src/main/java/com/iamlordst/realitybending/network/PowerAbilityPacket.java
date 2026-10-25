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
    private static String name(int a){return switch(a){case 0->"KI BLAST";case 1->"KAMEHAMEHA";case 2->"BLACK HOLE";case 3->"FAULTLINE";case 4->"MIRROR STEP";default->"PRISM CAGE";};}
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
                // Only the three energy-based abilities get a charging sphere. The other
                // abilities charge through distinct world/pose effects instead.
                if(p.ability<=2){
                    EnergyProjectile chargeVisual=new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(),player.level());
                    chargeVisual.configure(2,0,player);chargeVisual.setBeamLength(p.ability);chargeVisual.setPos(c.x,c.y,c.z);player.level().addFreshEntity(chargeVisual);
                } else if(player.level() instanceof ServerLevel server){
                    if(p.ability==3){
                        for(int i=0;i<8;i++){double a=i*Math.PI/4;server.sendParticles(ParticleTypes.CRIT,player.getX()+Math.cos(a)*1.3,player.getY()+.05,player.getZ()+Math.sin(a)*1.3,3,.08,.02,.08,.01);}
                    } else if(p.ability==4){
                        server.sendParticles(ParticleTypes.PORTAL,player.getX(),player.getY()+1,player.getZ(),36,.35,.6,.35,.18);
                        server.sendParticles(ParticleTypes.END_ROD,player.getX(),player.getY()+1,player.getZ(),12,.25,.45,.25,.02);
                    } else {
                        server.sendParticles(ParticleTypes.END_ROD,player.getX(),player.getY()+1,player.getZ(),24,1.2,.8,1.2,.04);
                        server.sendParticles(ParticleTypes.ENCHANT,player.getX(),player.getY()+1,player.getZ(),18,.9,.6,.9,.05);
                    }
                }
                var sound=p.ability==0?ModSounds.KI_CHARGE.get():p.ability==1?ModSounds.KAMEHAMEHA_CHARGE.get():p.ability==2?ModSounds.VOID_CAST.get():p.ability==3?net.minecraft.sounds.SoundEvents.DEEPSLATE_BREAK:p.ability==4?net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT:net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME;
                player.level().playSound(null,player.blockPosition(),sound,net.minecraft.sounds.SoundSource.PLAYERS,p.ability==2?2.8F:1.7F,p.ability==1?.72F:p.ability==3?.65F:p.ability==4?1.25F:.7F);
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
                case 3->{ // FAULTLINE: a ground-hugging rupture, not a projectile.
                    Vec3 flat=new Vec3(look.x,0,look.z);
                    if(flat.lengthSqr()<1.0E-5)flat=new Vec3(0,0,1); else flat=flat.normalize();
                    double reach=8.0+charge*12.0;
                    for(int step=1;step<=reach;step++){
                        Vec3 point=player.position().add(flat.scale(step));
                        BlockPos ground=BlockPos.containing(point.x,player.getY()-0.2,point.z);
                        for(int side=-1;side<=1;side++){
                            BlockPos pos=ground.offset((int)Math.round(-flat.z*side),0,(int)Math.round(flat.x*side));
                            var state=level.getBlockState(pos);
                            if(!state.isAir()&&state.getDestroySpeed(level,pos)>=0&&state.getDestroySpeed(level,pos)<=4&&level.getBlockEntity(pos)==null){
                                level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,state),pos.getX()+.5,pos.getY()+.45,pos.getZ()+.5,8,.25,.15,.25,.12);
                                if(level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))level.destroyBlock(pos,false);
                            }
                        }
                        level.sendParticles(ParticleTypes.CRIT,point.x,player.getY()+.25,point.z,8,.45,.15,.45,.1);
                        level.sendParticles(ParticleTypes.CLOUD,point.x,player.getY()+.15,point.z,5,.3,.08,.3,.03);
                    }
                    AABB shock=player.getBoundingBox().expandTowards(flat.scale(reach)).inflate(1.4,1.0,1.4);
                    for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,shock,e->e.isAlive()&&e!=player)){
                        Vec3 away=target.position().subtract(player.position());if(away.dot(flat)<0)continue;
                        target.hurt(level.damageSources().magic(),8.0F+charge*10.0F);
                        target.setDeltaMovement(target.getDeltaMovement().add(flat.scale(1.0+charge).add(0,.55+charge*.5,0)));target.hurtMarked=true;
                    }
                    level.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,net.minecraft.sounds.SoundSource.BLOCKS,2.5F,.55F);
                    level.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,net.minecraft.sounds.SoundSource.BLOCKS,1.8F,.65F);
                }
                case 4->{ // MIRROR STEP: a fast ground dash with a trail of false afterimages.
                    Vec3 dash=look.normalize();
                    Vec3 from=player.position();
                    double distance=7.0+charge*7.0;
                    for(int step=1;step<=distance*2;step++){
                        Vec3 point=from.add(dash.scale(step*.5));
                        level.sendParticles(ParticleTypes.END_ROD,point.x,point.y+player.getBbHeight()*.55,point.z,3,.2,.45,.2,.01);
                        if(step%3==0)level.sendParticles(ParticleTypes.CLOUD,point.x,point.y+.8,point.z,4,.25,.5,.25,.02);
                    }
                    Vec3 destination=from.add(dash.scale(distance));
                    BlockPos base=BlockPos.containing(destination);boolean moved=false;
                    for(int dy=2;dy>=-3;dy--){
                        BlockPos feet=base.offset(0,dy,0);
                        if(level.getBlockState(feet).getCollisionShape(level,feet).isEmpty()&&level.getBlockState(feet.above()).getCollisionShape(level,feet.above()).isEmpty()&&!level.getBlockState(feet.below()).getCollisionShape(level,feet.below()).isEmpty()){
                            player.teleportTo(feet.getX()+.5,feet.getY(),feet.getZ()+.5);player.fallDistance=0;moved=true;break;
                        }
                    }
                    level.sendParticles(ParticleTypes.FLASH,from.x,from.y+1,from.z,1,0,0,0,0);
                    if(moved){level.sendParticles(ParticleTypes.FLASH,player.getX(),player.getY()+1,player.getZ(),1,0,0,0,0);level.playSound(null,BlockPos.containing(from),net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,net.minecraft.sounds.SoundSource.PLAYERS,1.7F,1.35F);}
                    else player.displayClientMessage(net.minecraft.network.chat.Component.literal("The mirror has no exit here"),true);
                }
                case 5->{ // PRISM CAGE: a geometric cage of light locks nearby hostile mobs in place.
                    AABB area=player.getBoundingBox().inflate(10.0);
                    int trapped=0;
                    for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()&&e!=player&&!(e instanceof net.minecraft.world.entity.player.Player)&&e instanceof net.minecraft.world.entity.Mob)){
                        if(trapped++>=4)break;
                        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,100,9));
                        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,100,1));
                        target.setDeltaMovement(Vec3.ZERO);target.hurtMarked=true;
                        double x=target.getX(),y=target.getY(),z=target.getZ(),w=target.getBbWidth()+.65,h=target.getBbHeight()+.3;
                        for(int corner=0;corner<8;corner++){
                            double cx=x+((corner&1)==0?-w/2:w/2),cz=z+((corner&2)==0?-w/2:w/2),cy=y+((corner&4)==0?0:h);
                            level.sendParticles(ParticleTypes.END_ROD,cx,cy,cz,3,.02,.02,.02,.01);
                        }
                        for(int edge=0;edge<12;edge++){
                            int a=edge/3,b=edge%3;
                            double sx=x+((a&1)==0?-w/2:w/2),sz=z+((a&2)==0?-w/2:w/2),sy=y+((a&4)==0?0:h);
                            for(int seg=1;seg<=5;seg++){
                                double t=seg/6.0,ex=sx,ey=sy,ez=sz;
                                if(b==0)ex=x+((a&1)==0?w/2:-w/2);
                                else if(b==1)ey=y+((a&4)==0?h:0);
                                else ez=z+((a&2)==0?w/2:-w/2);
                                level.sendParticles(ParticleTypes.END_ROD,sx+(ex-sx)*t,sy+(ey-sy)*t,sz+(ez-sz)*t,1,0,0,0,0);
                            }
                        }
                    }
                    level.sendParticles(ParticleTypes.CRIT,player.getX(),player.getY()+1,player.getZ(),45,3,1.5,3,.08);
                    level.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,net.minecraft.sounds.SoundSource.PLAYERS,2.8F,.45F);
                    if(trapped==0)player.displayClientMessage(net.minecraft.network.chat.Component.literal("No creature caught in the prism"),true);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}