package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RealityBending.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PowerAnimationSetup {
    private PowerAnimationSetup() {}

    @SubscribeEvent
    public static void registerPlayerAnimation(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
            new ResourceLocation(RealityBending.MOD_ID, "power_pose"),
            42,
            player -> new ModifierLayer<IAnimation>()
        ));
    }
}
