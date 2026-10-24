package com.iamlordst.realitybending.registry;

import com.iamlordst.realitybending.RealityBending;
import com.iamlordst.realitybending.entity.EnergyProjectile;
import net.minecraft.world.entity.*;
import net.minecraftforge.registries.*;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RealityBending.MOD_ID);
    public static final RegistryObject<EntityType<EnergyProjectile>> ENERGY_PROJECTILE = ENTITY_TYPES.register("energy_projectile",
        () -> EntityType.Builder.of(EnergyProjectile::new, MobCategory.MISC).sized(0.2F, 0.2F).clientTrackingRange(96).updateInterval(1).build("energy_projectile"));
    private ModEntities() {}
}
