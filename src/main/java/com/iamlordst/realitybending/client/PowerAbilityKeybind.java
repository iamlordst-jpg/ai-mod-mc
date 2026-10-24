package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.network.ModNetwork;
import com.iamlordst.realitybending.network.PowerAbilityPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RealityBending.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class PowerAbilityKeybind {
    public static final KeyMapping KI_BLAST = new KeyMapping(
            "key.realitybending.ki_blast_ability", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V, "key.categories.realitybending");
    public static final KeyMapping KAMEHAMEHA = new KeyMapping(
            "key.realitybending.kamehameha_ability", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B, "key.categories.realitybending");

    private static boolean kiWasDown;
    private static boolean kamehamehaWasDown;
    private static boolean kiCharging;
    private static boolean kamehamehaCharging;

    private PowerAbilityKeybind() {}

    public static boolean isCharging(int ability) {
        return ability == 0 ? kiCharging : kamehamehaCharging;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            kiWasDown = false;
            kamehamehaWasDown = false;
            kiCharging = false;
            kamehamehaCharging = false;
            return;
        }

        boolean kiDown = KI_BLAST.isDown();
        boolean kamehamehaDown = KAMEHAMEHA.isDown();
        if (kiDown && !kiWasDown && !kamehamehaCharging) {
            kiCharging = true;
            ModNetwork.CHANNEL.sendToServer(new PowerAbilityPacket(0, true));
        } else if (!kiDown && kiWasDown && kiCharging) {
            kiCharging = false;
            ModNetwork.CHANNEL.sendToServer(new PowerAbilityPacket(0, false));
        }
        if (kamehamehaDown && !kamehamehaWasDown && !kiCharging) {
            kamehamehaCharging = true;
            ModNetwork.CHANNEL.sendToServer(new PowerAbilityPacket(1, true));
        } else if (!kamehamehaDown && kamehamehaWasDown && kamehamehaCharging) {
            kamehamehaCharging = false;
            ModNetwork.CHANNEL.sendToServer(new PowerAbilityPacket(1, false));
        }
        kiWasDown = kiDown;
        kamehamehaWasDown = kamehamehaDown;
    }
}
