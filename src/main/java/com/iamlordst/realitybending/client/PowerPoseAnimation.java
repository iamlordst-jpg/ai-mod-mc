package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealityBending.MOD_ID, value = Dist.CLIENT)
public final class PowerPoseAnimation {
    private static final ResourceLocation LAYER = new ResourceLocation(RealityBending.MOD_ID, "power_pose");
    private static String activePose = null;

    private PowerPoseAnimation() {}

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            activePose = null;
            return;
        }
        AbstractClientPlayer player = minecraft.player;

        String desired = TelekinesisKeybind.isLocallyEnabled() ? "telekinesis" : null;
        if (desired == null) {
            for (int i = 0; i < 6; i++) {
                if (PowerAbilityKeybind.isCharging(i)) {
                    desired = "power_charge";
                    break;
                }
            }
        }
        if (java.util.Objects.equals(desired, activePose)) return;

        Object rawLayer = PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);
        if (!(rawLayer instanceof ModifierLayer<?>)) return;
        ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) rawLayer;
        if (desired == null) {
            layer.setAnimation(null);
            activePose = null;
            return;
        }

        var animation = PlayerAnimationRegistry.getAnimation(new ResourceLocation(RealityBending.MOD_ID, desired));
        if (animation == null) return;
        layer.setAnimation(new KeyframeAnimationPlayer(animation));
        activePose = desired;
    }
}
