package com.iamlordst.realitybending.network;

import com.iamlordst.realitybending.RealityBending;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(RealityBending.MOD_ID, "main"),
        () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, ToggleTelekinesisPacket.class,
            ToggleTelekinesisPacket::encode, ToggleTelekinesisPacket::decode,
            ToggleTelekinesisPacket::handle);
    }
}
