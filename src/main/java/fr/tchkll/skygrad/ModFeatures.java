package fr.tchkll.skygrad;

import fr.tchkll.skygrad.Skygrad;
import fr.tchkll.skygrad.FlyingIslandFeature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, Skygrad.MODID);

    public static final DeferredHolder<Feature<?>, FlyingIslandFeature> FLYING_ISLAND =
            FEATURES.register("flying_island",
                    () -> new FlyingIslandFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}