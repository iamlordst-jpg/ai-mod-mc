package com.iamlordst.realitybending.item;

/**
 * Power item rendered with its own vanilla-style inventory icon rather than
 * the shared animated orb model.
 */
public final class GeoRealityItem extends RealityItem {
    public GeoRealityItem(Power power, int cooldown, net.minecraft.world.item.Item.Properties properties) {
        super(power, cooldown, properties);
    }

    /** Kept for compatibility with the legacy model class. */
    public Power getPower() {
        return getPowerType();
    }
}
