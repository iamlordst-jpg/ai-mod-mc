package com.iamlordst.realitybending.registry;
import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.item.RealityItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RealityBending.MOD_ID);
    public static final RegistryObject<Item> RIFT_PEARL = register("rift_pearl", RealityItem.Power.RIFT_PEARL, 20);
    public static final RegistryObject<Item> GRAVITY_CORE = register("gravity_core", RealityItem.Power.GRAVITY_CORE, 100);
    public static final RegistryObject<Item> CHRONO_SHARD = register("chrono_shard", RealityItem.Power.CHRONO_SHARD, 80);
    public static final RegistryObject<Item> PHASE_CRYSTAL = register("phase_crystal", RealityItem.Power.PHASE_CRYSTAL, 240);
    public static final RegistryObject<Item> RETURN_ANCHOR = register("return_anchor", RealityItem.Power.RETURN_ANCHOR, 40);
    public static final RegistryObject<Item> BLINK_CHARM = register("blink_charm", RealityItem.Power.BLINK_CHARM, 30);
    public static final RegistryObject<Item> NULL_ORB = register("null_orb", RealityItem.Power.NULL_ORB, 100);
    public static final RegistryObject<Item> MIRROR_TOKEN = register("mirror_token", RealityItem.Power.MIRROR_TOKEN, 160);
    public static final RegistryObject<Item> TIME_BOTTLE = register("time_bottle", RealityItem.Power.TIME_BOTTLE, 100);
    public static final RegistryObject<Item> ECHO_STEP = register("echo_step", RealityItem.Power.ECHO_STEP, 80);
    private static RegistryObject<Item> register(String name, RealityItem.Power power, int cooldown) {
        return ITEMS.register(name, () -> new RealityItem(power, cooldown, new Item.Properties().stacksTo(1)));
    }
    private ModItems() {}
}
