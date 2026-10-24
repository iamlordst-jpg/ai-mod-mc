package com.iamlordst.realitybending;

import com.iamlordst.realitybending.registry.ModItems;
import com.iamlordst.realitybending.telekinesis.TelekinesisHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RealityBending.MOD_ID)
public final class RealityBending {
    public static final String MOD_ID = "realitybending";

    public RealityBending() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        bus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.addListener(TelekinesisHandler::onPlayerTick);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.TELEKINETIC_GLOVE);
            event.accept(ModItems.FORUM_PHANTOM_MASK);
            event.accept(ModItems.REALITY_CHECK);
            event.accept(ModItems.PLOT_ARMOR);
            event.accept(ModItems.LAG_SPIKE);
            event.accept(ModItems.POCKET_DIMENSION);
            event.accept(ModItems.ADMIN_WAND);
            event.accept(ModItems.CHAOS_DICE);
            event.accept(ModItems.MEME_BEACON);
            event.accept(ModItems.GRAVITY_BOOTS);
        }
    }
}
