package com.iamlordst.realitybending.registry;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.item.RealityItem;
import com.iamlordst.realitybending.item.GeoRealityItem;
import com.iamlordst.realitybending.item.TelekineticGlove;
import com.iamlordst.realitybending.item.CaseFileItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

    public static final RegistryObject<Item> KI_BLAST = register("ki_blast", RealityItem.Power.KI_BLAST, 8);
    public static final RegistryObject<Item> KAMEHAMEHA = register("kamehameha", RealityItem.Power.KAMEHAMEHA, 80);
    public static final RegistryObject<Item> X_RAY_VISION = register("x_ray_vision", RealityItem.Power.X_RAY_VISION, 240);
    public static final RegistryObject<Item> MIND_READING = register("mind_reading", RealityItem.Power.MIND_READING, 60);
    public static final RegistryObject<Item> TIME_WARP = register("time_warp", RealityItem.Power.TIME_WARP, 180);
    public static final RegistryObject<Item> PHASE_SHIFT = register("phase_shift", RealityItem.Power.PHASE_SHIFT, 120);
    public static final RegistryObject<Item> VOID_PULSE = register("void_pulse", RealityItem.Power.VOID_PULSE, 100);
    public static final RegistryObject<Item> BLACK_HOLE = register("black_hole", RealityItem.Power.BLACK_HOLE, 180);
    public static final RegistryObject<Item> REALITY_RIFT = register("reality_rift", RealityItem.Power.REALITY_RIFT, 100);
    public static final RegistryObject<Item> LIGHTNING_SPEAR = register("lightning_spear", RealityItem.Power.LIGHTNING_SPEAR, 120);
    // Inert satirical archive references: no combat abilities or spawn eggs.
    public static final RegistryObject<Item> CASE_FILE_ELLIOT_RODGER = ITEMS.register("case_file_elliot_rodger", () -> new CaseFileItem("ELLIOT RODGER", "2014, Isla Vista, California. A mass killing that left victims, families and a community devastated.", "This entry documents the event without celebrating the perpetrator.", new Item.Properties().stacksTo(1))); 
    public static final RegistryObject<Item> CASE_FILE_SALVADOR_RAMOS = ITEMS.register("case_file_salvador_ramos", () -> new CaseFileItem("SALVADOR RAMOS", "2022, Uvalde, Texas: the Robb Elementary School shooting devastated families and a community.", "This entry is a historical record, not a tribute or a reward.", new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CASE_FILE_ADAM_LANZA = ITEMS.register("case_file_adam_lanza", () -> new CaseFileItem("ADAM LANZA", "2012, Newtown, Connecticut: the Sandy Hook Elementary School shooting devastated families and the wider community.", "This entry records the event in a non-glorifying archive.", new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CASE_FILE_ARTHUR_ACHLEITNER = ITEMS.register("case_file_arthur_achleitner", () -> new CaseFileItem("ARTHUR ACHLEITNER", "Creator-requested reference. The identity and event details have not been verified for this mod.", "No claims are made here; this archive item deliberately avoids turning an uncertain reference into a tribute.", new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CASE_FILE_KOSTA_KECMANOVIC = ITEMS.register("case_file_kosta_kecmanovic", () -> new CaseFileItem("KOSTA KECMANOVIĆ", "2023, Belgrade, Serbia: a school shooting that caused profound harm to students, staff, families and the community.", "This is a historical reference, not an endorsement or tribute.", new Item.Properties().stacksTo(1)));

    private static RegistryObject<Item> register(String name, RealityItem.Power power, int cooldown) {
        return ITEMS.register(name, () -> new GeoRealityItem(power, cooldown, new Item.Properties().stacksTo(1)));
    }

    // Powers are keybind-driven; legacy items are hidden.
    public static void addToTab(CreativeModeTab.Output output) { }

    private ModItems() {}
}
