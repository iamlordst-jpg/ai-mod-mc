package com.iamlordst.realitybending.item;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Locale;
import java.util.function.Consumer;

/** Power items retain GeckoLib animatable compatibility but use vanilla inventory icons. */
public final class GeoRealityItem extends RealityItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GeoRealityItem(Power power, int cooldown, Properties properties) {
        super(power, cooldown, properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public Power getPower() {
        return getPowerType();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        String animation = "animation.reality_orb." + getPower().name().toLowerCase(Locale.ROOT) + ".idle";
        controllers.add(new AnimationController<>(this, "power_idle", 0,
            state -> state.setAndContinue(RawAnimation.begin().thenLoop(animation))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /**
     * Intentionally do not install a GeoItemRenderer: Minecraft's JSON item
     * models should provide the inventory and held-item icons.
     */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        // Use the normal Minecraft item model renderer.
    }
}
