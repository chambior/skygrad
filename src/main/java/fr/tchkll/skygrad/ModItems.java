package fr.tchkll.skygrad;

import fr.tchkll.skygrad.item.FlyingCastleMapItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Skygrad.MODID);

    public static final DeferredItem<FlyingCastleMapItem> FLYING_CASTLE_MAP =
            ITEMS.register("flying_castle_map",
                    () -> new FlyingCastleMapItem(new Item.Properties().stacksTo(16)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
