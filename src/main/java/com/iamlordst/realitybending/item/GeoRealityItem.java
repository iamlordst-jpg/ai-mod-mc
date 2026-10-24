package com.iamlordst.realitybending.item;

import com.iamlordst.realitybending.client.RealityOrbModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Locale;
import java.util.function.Consumer;

/** Each Reality Bending power has its own animated geometry and idle motion. */
public final class GeoRealityItem extends RealityItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GeoRealityItem(Power power, int cooldown, Properties properties) {
        super(power, cooldown, properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public Power getPower() {
        return powerType();
    }

    private Power powerType() {
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

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<GeoRealityItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new GeoItemRenderer<>(new RealityOrbModel());
                return renderer;
            }
        });
    }
}
