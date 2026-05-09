package fr.tchkll.skygrad.blockentity;

import fr.tchkll.skygrad.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TowerSentinelBlockEntity extends BlockEntity {

    public TowerSentinelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOWER_SENTINEL_BE.get(), pos, state);
    }
}