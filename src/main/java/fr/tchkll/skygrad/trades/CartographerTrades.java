package fr.tchkll.skygrad.trades;

import fr.tchkll.skygrad.ModItems;
import fr.tchkll.skygrad.Skygrad;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;

@EventBusSubscriber(modid = Skygrad.MODID)
public class CartographerTrades {
    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.CARTOGRAPHER) {
            return;
        }

        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

        trades.get(1).add((trader, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 5),
                new ItemStack(ModItems.FLYING_CASTLE_MAP.get()),
                12,
                2,
                0.05F
        ));

        trades.get(3).add((trader, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 5),
                new ItemStack(ModItems.FLYING_FORTRESS_MAP.get()),
                12,
                2,
                0.05F
        ));

        // Remove existing trades if wanted
        trades.get(1).removeIf(trade ->
                trade instanceof VillagerTrades.EmeraldForItems
        );
    }
}
