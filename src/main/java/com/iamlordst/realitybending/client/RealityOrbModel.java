package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.item.GeoRealityItem;
import com.iamlordst.realitybending.item.RealityItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import java.util.Locale;
import java.util.Map;

public final class RealityOrbModel extends GeoModel<GeoRealityItem> {
    private static final Map<RealityItem.Power, String> TEXTURES = Map.ofEntries(
        Map.entry(RealityItem.Power.FORUM_PHANTOM_MASK, "phantom_membrane"),
        Map.entry(RealityItem.Power.REALITY_CHECK, "spyglass"),
        Map.entry(RealityItem.Power.PLOT_ARMOR, "shield"),
        Map.entry(RealityItem.Power.LAG_SPIKE, "redstone"),
        Map.entry(RealityItem.Power.POCKET_DIMENSION, "ender_pearl"),
        Map.entry(RealityItem.Power.ADMIN_WAND, "blaze_rod"),
        Map.entry(RealityItem.Power.CHAOS_DICE, "amethyst_shard"),
        Map.entry(RealityItem.Power.MEME_BEACON, "nether_star"),
        Map.entry(RealityItem.Power.GRAVITY_BOOTS, "iron_boots"),
        Map.entry(RealityItem.Power.KI_BLAST, "fire_charge"),
        Map.entry(RealityItem.Power.KAMEHAMEHA, "prismarine_crystals"),
        Map.entry(RealityItem.Power.X_RAY_VISION, "spyglass"),
        Map.entry(RealityItem.Power.MIND_READING, "echo_shard"),
        Map.entry(RealityItem.Power.TIME_WARP, "clock"),
        Map.entry(RealityItem.Power.PHASE_SHIFT, "chorus_fruit"),
        Map.entry(RealityItem.Power.VOID_PULSE, "ender_eye"),
        Map.entry(RealityItem.Power.BLACK_HOLE, "coal"),
        Map.entry(RealityItem.Power.REALITY_RIFT, "amethyst_shard"),
        Map.entry(RealityItem.Power.LIGHTNING_SPEAR, "lightning_rod")
    );

    @Override
    public ResourceLocation getModelResource(GeoRealityItem animatable) {
        String power = animatable.getPower().name().toLowerCase(Locale.ROOT);
        return new ResourceLocation(RealityBending.MOD_ID, "geo/" + power + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRealityItem animatable) {
        String texture = TEXTURES.getOrDefault(animatable.getPower(), "ender_pearl");
        return new ResourceLocation("minecraft", "textures/item/" + texture + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeoRealityItem animatable) {
        return new ResourceLocation(RealityBending.MOD_ID, "animations/reality_powers.animation.json");
    }
}
