package com.iamlordst.realitybending.client;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.item.CaseFileItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class ArchiveFileModel extends GeoModel<CaseFileItem> {
    @Override
    public ResourceLocation getModelResource(CaseFileItem animatable) {
        return new ResourceLocation(RealityBending.MOD_ID, "geo/archive_file.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CaseFileItem animatable) {
        return new ResourceLocation("minecraft", "textures/item/book.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CaseFileItem animatable) {
        return new ResourceLocation(RealityBending.MOD_ID, "animations/archive_file.animation.json");
    }
}
