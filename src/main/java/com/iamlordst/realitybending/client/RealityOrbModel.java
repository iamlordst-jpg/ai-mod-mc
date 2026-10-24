package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.item.GeoRealityItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class RealityOrbModel extends GeoModel<GeoRealityItem> {
    @Override
    public ResourceLocation getModelResource(GeoRealityItem animatable) {
        return new ResourceLocation(RealityBending.MOD_ID, "geo/reality_orb.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRealityItem animatable) {
        return new ResourceLocation("minecraft", "textures/item/ender_pearl.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeoRealityItem animatable) {
        return new ResourceLocation(RealityBending.MOD_ID, "animations/reality_orb.animation.json");
    }
}
