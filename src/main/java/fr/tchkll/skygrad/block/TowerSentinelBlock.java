package fr.tchkll.skygrad.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TowerSentinelBlock extends Block {

    // Fence-post footprint: 6–10 px on X/Z, full height
    private static final VoxelShape POST_SHAPE = Block.box(6, 0, 6, 10, 16, 10);

    public TowerSentinelBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .strength(999f, 999f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noLootTable()
        );
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        return POST_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
                                        BlockPos pos, CollisionContext context) {
        return POST_SHAPE;
    }
}
