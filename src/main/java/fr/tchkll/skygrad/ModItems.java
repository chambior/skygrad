package fr.tchkll.skygrad;

import fr.tchkll.skygrad.items.FlyingCastleMapItem;
import fr.tchkll.skygrad.items.FlyingFortressMapItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.ItemNameBlockItem;

@SuppressWarnings("unused")
public class ModItems {

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(Skygrad.MODID);

    public static final DeferredItem<FlyingCastleMapItem> FLYING_CASTLE_MAP =
        ITEMS.register("flying_castle_map",
            () -> new FlyingCastleMapItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<FlyingFortressMapItem> FLYING_FORTRESS_MAP =
        ITEMS.register("flying_fortress_map",
            () -> new FlyingFortressMapItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> LEVITITE_INGOT =
        ITEMS.register("levitite_ingot",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> LEVITITE_SHEET =
        ITEMS.register("levitite_sheet",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> LEVITITE_POWDER =
        ITEMS.register("levitite_powder",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ASH =
        ITEMS.register("ash",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> COTTON =
        ITEMS.register("cotton",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TURTLE_APPLE =
        ITEMS.register("turtle_apple",
            () -> new Item(new Item.Properties()
                .food(new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationModifier(0.3F)
                    .effect(() -> new MobEffectInstance(
                        MobEffects.REGENERATION,
                        200,
                        0
                    ), 1.0F)
                    .alwaysEdible()
                    .build()
                )
            ));

    public static final DeferredItem<Item> COTTON_SEEDS =
        ITEMS.register("cotton_seeds",
            () -> new ItemNameBlockItem(
                ModBlocks.COTTON_CROP_BLOCK.get(),
                new Item.Properties()
            ));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
