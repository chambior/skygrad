package fr.tchkll.skygrad;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Skygrad.MODID)
@SuppressWarnings("unused")
public class ModCreativeTab {

    private static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Skygrad.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SKYGRAD_TAB =
        TABS.register("skygrad_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.skygrad"))
            .withTabsBefore(CreativeModeTabs.INGREDIENTS)
            .icon(() -> ModBlocks.BUILDERS_BLOCK_ITEM.get().getDefaultInstance())
            .displayItems((params, output) -> {})
            .build()
        );

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }

    @SubscribeEvent
    public static void populateTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(SKYGRAD_TAB.getKey())) return;

        ModBlocks.ITEMS.getEntries().forEach(holder -> event.accept(holder.get()));
        ModItems.ITEMS.getEntries().forEach(holder -> event.accept(holder.get()));
    }
}