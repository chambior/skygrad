package fr.tchkll.skygrad;

import fr.tchkll.skygrad.block.BuildersBlock;
import fr.tchkll.skygrad.block.CottonCropBlock;
import fr.tchkll.skygrad.block.IslandHeartBlock;
import fr.tchkll.skygrad.block.SkyEngineBlock;
import fr.tchkll.skygrad.block.TowerSentinelBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(Skygrad.MODID);

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(Skygrad.MODID);

    public static final DeferredBlock<BuildersBlock> BUILDERS_BLOCK =
        BLOCKS.register("builders_block", BuildersBlock::new);

    public static final DeferredHolder<Item, BlockItem> BUILDERS_BLOCK_ITEM =
        ITEMS.registerSimpleBlockItem("builders_block", BUILDERS_BLOCK);

    public static final DeferredBlock<IslandHeartBlock> ISLAND_HEART_BLOCK =
        BLOCKS.register("island_heart", IslandHeartBlock::new);

    public static final DeferredBlock<TowerSentinelBlock> TOWER_SENTINEL_BLOCK =
        BLOCKS.register("tower_sentinel", TowerSentinelBlock::new);

    public static final DeferredBlock<SkyEngineBlock> SKY_ENGINE_BLOCK =
        BLOCKS.register("sky_engine", () -> new SkyEngineBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                .sound(SoundType.METAL)
                .strength(3.5f, 6f)
                .noOcclusion()
        ));

    public static final DeferredHolder<Item, BlockItem> SKY_ENGINE_ITEM =
        ITEMS.registerSimpleBlockItem("sky_engine", SKY_ENGINE_BLOCK);

    public static final DeferredBlock<CottonCropBlock> COTTON_CROP_BLOCK =
        BLOCKS.register("cotton_crop", () -> new CottonCropBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
        ));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}