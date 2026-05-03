package fr.tchkll.skygrad.block;

import fr.tchkll.skygrad.Skygrad;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Skygrad.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Skygrad.MODID);

    public static final DeferredBlock<BuildersBlock> BUILDERS_BLOCK =
            BLOCKS.register("builders_block", BuildersBlock::new);

    public static final DeferredHolder<Item, BlockItem> BUILDERS_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("builders_block", BUILDERS_BLOCK);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}