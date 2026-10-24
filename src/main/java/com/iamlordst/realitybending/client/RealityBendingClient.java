package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.registry.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@Mod.EventBusSubscriber(modid = RealityBending.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RealityBendingClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GeoItemRenderer.registerItemRenderer(ModItems.FORUM_PHANTOM_MASK.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.REALITY_CHECK.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.PLOT_ARMOR.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.LAG_SPIKE.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.POCKET_DIMENSION.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.ADMIN_WAND.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.CHAOS_DICE.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.MEME_BEACON.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.GRAVITY_BOOTS.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.KI_BLAST.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.KAMEHAMEHA.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.X_RAY_VISION.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.MIND_READING.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.TIME_WARP.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.PHASE_SHIFT.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.VOID_PULSE.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.BLACK_HOLE.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.REALITY_RIFT.get(), new GeoItemRenderer<>(new RealityOrbModel()));
            GeoItemRenderer.registerItemRenderer(ModItems.LIGHTNING_SPEAR.get(), new GeoItemRenderer<>(new RealityOrbModel()));
        });
    }

    private RealityBendingClient() {}
}
