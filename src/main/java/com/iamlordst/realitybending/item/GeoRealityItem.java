package com.iamlordst.realitybending.item;

import com.iamlordst.realitybending.client.RealityOrbModel;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.animatable.processing.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** A power item with a custom animated GeckoLib model. */
public final class GeoRealityItem extends RealityItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GeoRealityItem(Power power, int cooldown, Properties properties) {
        super(power, cooldown, properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "orb_idle", 0,
            state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.reality_orb.idle"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
