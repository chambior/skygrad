package fr.tchkll.skygrad.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import fr.tchkll.skygrad.blockentity.IslandHeartBlockEntity;
import fr.tchkll.skygrad.blockentity.TowerSentinelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TowerSentinelBlock extends BaseEntityBlock {

    // Fence-post footprint: 6–10 px on X/Z, full height
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 32, 16);

    public TowerSentinelBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .strength(999f, 6f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noLootTable()
                .noOcclusion()
        );
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
                                        BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(p -> new TowerSentinelBlock());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TowerSentinelBlockEntity(pos, state);
    }
}
