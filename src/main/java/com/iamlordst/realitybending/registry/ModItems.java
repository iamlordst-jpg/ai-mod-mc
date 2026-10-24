package com.iamlordst.realitybending.registry;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.item.RealityItem;
import com.iamlordst.realitybending.item.TelekineticGlove;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RealityBending.MOD_ID);

    public static final RegistryObject<Item> TELEKINETIC_GLOVE = ITEMS.register("telekinetic_glove",
        () -> new TelekineticGlove(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FORUM_PHANTOM_MASK = register("forum_phantom_mask", RealityItem.Power.FORUM_PHANTOM_MASK, 200);
    public static final RegistryObject<Item> REALITY_CHECK = register("reality_check", RealityItem.Power.REALITY_CHECK, 100);
    public static final RegistryObject<Item> PLOT_ARMOR = register("plot_armor", RealityItem.Power.PLOT_ARMOR, 240);
    public static final RegistryObject<Item> LAG_SPIKE = register("lag_spike", RealityItem.Power.LAG_SPIKE, 100);
    public static final RegistryObject<Item> POCKET_DIMENSION = register("pocket_dimension", RealityItem.Power.POCKET_DIMENSION, 80);
    public static final RegistryObject<Item> ADMIN_WAND = register("admin_wand", RealityItem.Power.ADMIN_WAND, 120);
    public static final RegistryObject<Item> CHAOS_DICE = register("chaos_dice", RealityItem.Power.CHAOS_DICE, 100);
    public static final RegistryObject<Item> MEME_BEACON = register("meme_beacon", RealityItem.Power.MEME_BEACON, 160);
    public static final RegistryObject<Item> GRAVITY_BOOTS = register("gravity_boots", RealityItem.Power.GRAVITY_BOOTS, 100);

    private static RegistryObject<Item> register(String name, RealityItem.Power power, int cooldown) {
        return ITEMS.register(name, () -> new RealityItem(power, cooldown, new Item.Properties().stacksTo(1)));
    }

    private ModItems() {}
}
