package com.iamlordst.realitybending.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleTelekinesisPacket {
    private final boolean enabled;

    public ToggleTelekinesisPacket(boolean enabled) { this.enabled = enabled; }
    public static void encode(ToggleTelekinesisPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.enabled);
    }
    public static ToggleTelekinesisPacket decode(FriendlyByteBuf buffer) {
        return new ToggleTelekinesisPacket(buffer.readBoolean());
    }

    public static void handle(ToggleTelekinesisPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            player.getPersistentData().putBoolean("realitybending_telekinesis_enabled", packet.enabled);
            player.displayClientMessage(Component.literal("Telekinesis " + (packet.enabled ? "ENABLED" : "DISABLED")), true);
        });
        context.setPacketHandled(true);
    }
}
