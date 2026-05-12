package fr.tchkll.skygrad;

import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(modid = Skygrad.MODID)
@SuppressWarnings("unused")
public final class ModEvents {

    private ModEvents() {}

    @SubscribeEvent
    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(
            Potions.MUNDANE,
            ModItems.TURTLE_APPLE.get(),
            ModPotions.TURTLE_REGENERATION
        );
    }
}
