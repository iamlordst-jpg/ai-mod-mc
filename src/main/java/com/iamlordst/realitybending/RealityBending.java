package com.iamlordst.realitybending;

import com.iamlordst.realitybending.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
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
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.RIFT_PEARL);
            event.accept(ModItems.GRAVITY_CORE);
            event.accept(ModItems.CHRONO_SHARD);
            event.accept(ModItems.PHASE_CRYSTAL);
            event.accept(ModItems.RETURN_ANCHOR);
            event.accept(ModItems.BLINK_CHARM);
            event.accept(ModItems.NULL_ORB);
            event.accept(ModItems.MIRROR_TOKEN);
            event.accept(ModItems.TIME_BOTTLE);
            event.accept(ModItems.ECHO_STEP);
        }
    }
}
