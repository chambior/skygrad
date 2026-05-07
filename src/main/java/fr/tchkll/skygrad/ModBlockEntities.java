package fr.tchkll.skygrad;

import fr.tchkll.skygrad.blockentity.IslandHeartBlockEntity;
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

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}