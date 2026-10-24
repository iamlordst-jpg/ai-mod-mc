package com.iamlordst.realitybending.registry;

import com.iamlordst.realitybending.RealityBending;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RealityBending.MOD_ID);

    public static final RegistryObject<SoundEvent> POWER_CAST = register("power_cast");
    public static final RegistryObject<SoundEvent> KI_CHARGE = register("ki_charge");
    public static final RegistryObject<SoundEvent> KI_RELEASE = register("ki_release");
    public static final RegistryObject<SoundEvent> KAMEHAMEHA_CHARGE = register("kamehameha_charge");
    public static final RegistryObject<SoundEvent> KAMEHAMEHA_RELEASE = register("kamehameha_release");
    public static final RegistryObject<SoundEvent> VOID_CAST = register("void_cast");
    public static final RegistryObject<SoundEvent> RIFT_CAST = register("rift_cast");
    public static final RegistryObject<SoundEvent> LIGHTNING_CAST = register("lightning_cast");
    public static final RegistryObject<SoundEvent> FEDORA_ACTIVATE = register("fedora_activate");
    public static final RegistryObject<SoundEvent> TIME_WARP_CAST = register("time_warp_cast");
    public static final RegistryObject<SoundEvent> TELEPATHY_CAST = register("telepathy_cast");
    public static final RegistryObject<SoundEvent> GRAVITY_CAST = register("gravity_cast");
    public static final RegistryObject<SoundEvent> DEFENSE_CAST = register("defense_cast");
    public static final RegistryObject<SoundEvent> CHAOS_CAST = register("chaos_cast");
    public static final RegistryObject<SoundEvent> ADMIN_CAST = register("admin_cast");
    public static final RegistryObject<SoundEvent> PHASE_CAST = register("phase_cast");
    public static final RegistryObject<SoundEvent> LAG_CAST = register("lag_cast");
    public static final RegistryObject<SoundEvent> REVEAL_CAST = register("reveal_cast");
    public static final RegistryObject<SoundEvent> MEME_BEACON_CAST = register("meme_beacon_cast");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation(RealityBending.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    private ModSounds() {}
}
