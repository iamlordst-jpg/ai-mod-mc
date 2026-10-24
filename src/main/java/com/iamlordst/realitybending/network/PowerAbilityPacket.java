package com.iamlordst.realitybending.network;

import com.iamlordst.realitybending.entity.EnergyProjectile;
import com.iamlordst.realitybending.item.RealityItem;
import com.iamlordst.realitybending.registry.ModEntities;
import com.iamlordst.realitybending.registry.ModItems;
import com.iamlordst.realitybending.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class PowerAbilityPacket {
    private final int ability; // 0 = Ki Blast, 1 = Kamehameha
    private final boolean charging;

    public PowerAbilityPacket(int ability, boolean charging) {
        this.ability = ability;
        this.charging = charging;
    }

    public static void encode(PowerAbilityPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.ability);
        buffer.writeBoolean(packet.charging);
    }

    public static PowerAbilityPacket decode(FriendlyByteBuf buffer) {
        return new PowerAbilityPacket(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(PowerAbilityPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || packet.ability < 0 || packet.ability > 1) return;

            String activeKey = "realitybending_ability_charging";
            String typeKey = "realitybending_ability_type";
            String startKey = "realitybending_ability_start_tick";
            var data = player.getPersistentData();

            if (packet.charging) {
                if (player.isSpectator() || data.getBoolean(activeKey)) return;
                var item = packet.ability == 0 ? ModItems.KI_BLAST.get() : ModItems.KAMEHAMEHA.get();
                if (player.getCooldowns().isOnCooldown(item)) return;
                data.putBoolean(activeKey, true);
                data.putInt(typeKey, packet.ability);
                data.putInt(startKey, player.tickCount);

                Vec3 look = player.getLookAngle().normalize();
                Vec3 center = player.getEyePosition().add(look.scale(1.05));
                EnergyProjectile orb = new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(), player.level());
                orb.configure(2, 0.0F, player);
                orb.setPos(center.x, center.y, center.z);
                player.level().addFreshEntity(orb);
                player.level().playSound(null, player.blockPosition(),
                        packet.ability == 0 ? ModSounds.KI_CHARGE.get() : ModSounds.KAMEHAMEHA_CHARGE.get(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.85F, packet.ability == 0 ? 1.15F : 0.78F);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        packet.ability == 0 ? "KI BLAST — charging" : "KAMEHAMEHA — charging"), true);
                return;
            }

            if (!data.getBoolean(activeKey) || data.getInt(typeKey) != packet.ability) return;
            int chargeTicks = Math.max(0, player.tickCount - data.getInt(startKey));
            data.putBoolean(activeKey, false);
            for (EnergyProjectile orb : player.level().getEntitiesOfClass(EnergyProjectile.class,
                    player.getBoundingBox().inflate(6.0),
                    e -> e.visualType() == 2 && e.ownerId() == player.getId())) orb.discard();
            if (chargeTicks < 4) return;

            float charge = Math.min(1.0F, chargeTicks / (packet.ability == 0 ? 36.0F : 50.0F));
            var item = packet.ability == 0 ? ModItems.KI_BLAST.get() : ModItems.KAMEHAMEHA.get();
            player.getCooldowns().addCooldown(item, packet.ability == 0 ? 8 : 80);

            if (packet.ability == 0) {
                Vec3 look = player.getLookAngle().normalize();
                Vec3 origin = player.getEyePosition().add(look.scale(0.9));
                EnergyProjectile blast = new EnergyProjectile(ModEntities.ENERGY_PROJECTILE.get(), player.level());
                blast.configure(0, charge, player);
                blast.setPos(origin.x, origin.y, origin.z);
                blast.setDeltaMovement(look.scale(1.55 + charge * 0.65));
                player.level().addFreshEntity(blast);
                if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, origin.x, origin.y, origin.z, 18, .22, .22, .22, .12);
                    server.sendParticles(ParticleTypes.END_ROD, origin.x, origin.y, origin.z, 10, .15, .15, .15, .04);
                }
                player.level().playSound(null, player.blockPosition(), ModSounds.KI_RELEASE.get(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.25F, 1.0F - charge * 0.18F);
            } else {
                RealityItem.fireKamehameha(player.level(), player, charge);
            }
        });
        context.setPacketHandled(true);
    }
}
