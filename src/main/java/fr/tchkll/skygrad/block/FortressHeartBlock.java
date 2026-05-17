package fr.tchkll.skygrad.block;

import com.mojang.serialization.MapCodec;
import fr.tchkll.skygrad.registration.ModBlockEntities;
import fr.tchkll.skygrad.blockentity.FortressHeartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class FortressHeartBlock extends BaseEntityBlock {

    public FortressHeartBlock() {
        super(Properties.of()
                .mapColor(MapColor.NETHER)
                .strength(999f, 999f)   // indestructible (hors créatif)
                .sound(SoundType.ANCIENT_DEBRIS)
                .requiresCorrectToolForDrops()
                .noLootTable()
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FortressHeartBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            net.minecraft.world.level.Level level, BlockState state,
            BlockEntityType<T> type) {
        // Tick seulement côté serveur
        return level.isClientSide ? null :
                createTickerHelper(type, ModBlockEntities.FORTRESS_HEART_BE.get(),
                        FortressHeartBlockEntity::serverTick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(p -> new FortressHeartBlock());
    }
}