package fr.tchkll.skygrad.registration;

import fr.tchkll.skygrad.Skygrad;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.core.registries.BuiltInRegistries.POTION;

@SuppressWarnings("unused")
public class ModPotions {

    public static final DeferredRegister<Potion> POTIONS =
        DeferredRegister.create(POTION, Skygrad.MODID);

    // 30 minutes (36 000 ticks) of Regeneration I — brewed from mundane + turtle apple
    public static final DeferredHolder<Potion, Potion> TURTLE_REGENERATION =
        POTIONS.register("turtle_regeneration",
            () -> new Potion("regeneration", new MobEffectInstance(MobEffects.REGENERATION, 36000, 0)));

    public static void register(IEventBus bus) {
        POTIONS.register(bus);
    }
}
