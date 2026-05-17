package fr.tchkll.skygrad.block;

import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlock;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import fr.tchkll.skygrad.registration.ModBlockEntities;
import fr.tchkll.skygrad.blockentity.SkyEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SkyEngineBlock extends PortableEngineBlock {

    public SkyEngineBlock(BlockBehaviour.Properties properties) {
        super(properties, DyeColor.YELLOW);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<PortableEngineBlockEntity> getBlockEntityClass() {
        return (Class<PortableEngineBlockEntity>) (Class<?>) SkyEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PortableEngineBlockEntity> getBlockEntityType() {
        return ModBlockEntities.SKY_ENGINE_BE.get();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // Parent's onRemove drops inventory + removes BE unless newState is in SimBlocks.PORTABLE_ENGINES.
        // Since our block isn't in that list, even a simple state toggle (lit false<->true) destroys the BE.
        // Skip the parent path on state-only changes (same block, just different properties).
        if (state.is(newState.getBlock())) {
            return;
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
