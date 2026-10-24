package com.iamlordst.realitybending.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleTelekinesisPacket {
    public ToggleTelekinesisPacket() {}
    public static void encode(ToggleTelekinesisPacket packet, FriendlyByteBuf buffer) {}
    public static ToggleTelekinesisPacket decode(FriendlyByteBuf buffer) { return new ToggleTelekinesisPacket(); }

    public static void handle(ToggleTelekinesisPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
                if (!player.getPersistentData().getBoolean("realitybending_telekinesis_unlocked")) {
                    player.displayClientMessage(Component.literal("You haven't unlocked telekinesis yet. Use the Telekinesis Unlock item first."), true);
                    return;
                }
                boolean enabled = !player.getPersistentData().getBoolean("realitybending_telekinesis_enabled");
                player.getPersistentData().putBoolean("realitybending_telekinesis_enabled", enabled);
                player.displayClientMessage(Component.literal("Telekinesis " + (enabled ? "ENABLED" : "DISABLED")), true);
            }
        });
        context.setPacketHandled(true);
    }
}
