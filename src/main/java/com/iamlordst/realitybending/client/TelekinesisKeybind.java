package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.network.ModNetwork;
import com.iamlordst.realitybending.network.ToggleTelekinesisPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RealityBending.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class TelekinesisKeybind {
    private static boolean locallyEnabled = false;

    public static boolean isLocallyEnabled() { return locallyEnabled; }
    public static final KeyMapping TOGGLE = new KeyMapping(
        "key.realitybending.toggle_telekinesis",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        "key.categories.realitybending"
    );

    private TelekinesisKeybind() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        while (TOGGLE.consumeClick()) {
            locallyEnabled = !locallyEnabled;
            ModNetwork.CHANNEL.sendToServer(new ToggleTelekinesisPacket(locallyEnabled));
        }
    }
}
