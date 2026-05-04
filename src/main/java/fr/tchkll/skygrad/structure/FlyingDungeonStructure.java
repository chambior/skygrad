package fr.tchkll.skygrad.structure;

import com.mojang.serialization.MapCodec;
import fr.tchkll.skygrad.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class FlyingDungeonStructure extends Structure {

    public static final MapCodec<FlyingDungeonStructure> CODEC =
            simpleCodec(FlyingDungeonStructure::new);

    private static final int FIXED_Y = 250;

    public FlyingDungeonStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        ChunkPos chunkPos = ctx.chunkPos();
        BlockPos center = new BlockPos(
                chunkPos.getMiddleBlockX(),
                FIXED_Y,
                chunkPos.getMiddleBlockZ()
        );

        return Optional.of(new GenerationStub(center,
                builder -> addPieces(builder, center)));
    }

    private static void addPieces(StructurePiecesBuilder builder, BlockPos center) {
        builder.addPiece(new FlyingDungeonPiece(center));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.FLYING_DUNGEON_TYPE.get();
    }
}