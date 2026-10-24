package com.iamlordst.realitybending;

import com.iamlordst.realitybending.network.ModNetwork;
import com.iamlordst.realitybending.registry.ModItems;
import com.iamlordst.realitybending.registry.ModSounds;
import com.iamlordst.realitybending.telekinesis.TelekinesisHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(RealityBending.MOD_ID)
public final class RealityBending {
    public static final String MOD_ID = "realitybending";
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final RegistryObject<CreativeModeTab> POWERS_TAB = TABS.register("powers",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.realitybending.powers"))
            .icon(() -> new ItemStack(ModItems.TELEKINETIC_GLOVE.get()))
            .displayItems((parameters, output) -> ModItems.addToTab(output))
            .build());

    public RealityBending() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModSounds.SOUNDS.register(bus);
        TABS.register(bus);
        ModNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(TelekinesisHandler::onPlayerTick);
    }
}
