package com.iamlordst.realitybending.item;

import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Vanilla-rendered power item; power effects are handled by RealityItem. */
public final class GeoRealityItem extends RealityItem {
    public GeoRealityItem(Power power, int cooldown, Properties properties) {
        super(power, cooldown, properties);
    }

    public Power getPower() {
        return getPowerType();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        // Intentionally use vanilla item rendering.
    }
}
