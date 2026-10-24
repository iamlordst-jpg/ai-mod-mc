package com.iamlordst.realitybending;
import com.iamlordst.realitybending.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RealityBending.MOD_ID)
public final class RealityBending {
    public static final String MOD_ID = "realitybending";
    public RealityBending() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
    }
}
