package fr.tchkll.skygrad;

import fr.tchkll.skygrad.blockentity.IslandHeartBlockEntity;
import fr.tchkll.skygrad.blockentity.SkyEngineBlockEntity;
// import fr.tchkll.skygrad.blockentity.TowerSentinelBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Skygrad.MODID);

    @SuppressWarnings("DataFlowIssue") // BlockEntityType.Builder.build accepts null at runtime;
                                       // the @NotNull comes from the package-level
                                       // @ParametersAreNonnullByDefault, not actual code intent.
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IslandHeartBlockEntity>>
            ISLAND_HEART_BE = BLOCK_ENTITIES.register("island_heart",
            () -> BlockEntityType.Builder
                    .of(IslandHeartBlockEntity::new, ModBlocks.ISLAND_HEART_BLOCK.get())
                    .build(null)
    );

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SkyEngineBlockEntity>>
            SKY_ENGINE_BE = BLOCK_ENTITIES.register("sky_engine", () -> {
                // Create's 3-arg constructor requires the type itself; use array to capture self-reference.
                BlockEntityType<SkyEngineBlockEntity>[] holder = new BlockEntityType[1];
                holder[0] = BlockEntityType.Builder
                        .of((pos, state) -> new SkyEngineBlockEntity(holder[0], pos, state),
                                ModBlocks.SKY_ENGINE_BLOCK.get())
                        .build(null);
                return holder[0];
            });
    // public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TowerSentinelBlockEntity>> 
    //     TOWER_SENTINEL_BE = BLOCK_ENTITIES.register("tower_sentinel",
    //         () ->
    //         BlockEntityType.Builder
    //             .of(TowerSentinelBlockEntity::new, ModBlocks.TOWER_SENTINEL_BLOCK.get())
    //             .build(null)
    // );

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}