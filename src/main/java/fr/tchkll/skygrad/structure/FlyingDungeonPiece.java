package fr.tchkll.skygrad.structure;

import fr.tchkll.skygrad.ModBlocks;
import fr.tchkll.skygrad.ModStructurePieceTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class FlyingDungeonPiece extends StructurePiece {

    private static final int CIRCLE_RADIUS = 12;
    private static final int TOWER_RADIUS  = 10;
    private static final int TOWER_HEIGHT  = 8;

    private static final int[][] TOWER_OFFSETS = {
            { TOWER_RADIUS,  0},
            {-TOWER_RADIUS,  0},
            { 0,  TOWER_RADIUS},
            { 0, -TOWER_RADIUS}
    };

    // Centre absolu de la structure — sauvegardé en NBT
    private final BlockPos center;

    /** Constructeur de génération */
    public FlyingDungeonPiece(BlockPos center) {
        super(ModStructurePieceTypes.FLYING_DUNGEON_PIECE.get(), 0, makeBbox(center));
        this.center = center;
    }

    /** Constructeur de désérialisation (chargement de chunk sauvegardé) */
    public FlyingDungeonPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructurePieceTypes.FLYING_DUNGEON_PIECE.get(), tag);
        this.center = new BlockPos(
                tag.getInt("cx"), tag.getInt("cy"), tag.getInt("cz"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("cx", center.getX());
        tag.putInt("cy", center.getY());
        tag.putInt("cz", center.getZ());
    }

    private static BoundingBox makeBbox(BlockPos c) {
        int r = CIRCLE_RADIUS + 1;
        int h = TOWER_HEIGHT + 2;
        return new BoundingBox(c.getX() - r, c.getY() - 1, c.getZ() - r,
                c.getX() + r, c.getY() + h, c.getZ() + r);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // — Cercle de pierre —
        for (int dx = -CIRCLE_RADIUS; dx <= CIRCLE_RADIUS; dx++) {
            for (int dz = -CIRCLE_RADIUS; dz <= CIRCLE_RADIUS; dz++) {
                if (dx * dx + dz * dz > CIRCLE_RADIUS * CIRCLE_RADIUS) continue;

                BlockPos pos = new BlockPos(cx + dx, cy, cz + dz);
                // Ne placer que les blocs dans le chunk actuellement traité
                if (!box.isInside(pos)) continue;
                level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
            }
        }

        // — Cœur de l'île —
        BlockPos heartPos = new BlockPos(cx, cy, cz);
        if (box.isInside(heartPos)) {
            level.setBlock(heartPos,
                    ModBlocks.ISLAND_HEART_BLOCK.get().defaultBlockState(), 2);
        }

        // — 4 tours —
        for (int[] offset : TOWER_OFFSETS) {
            int tx = cx + offset[0];
            int tz = cz + offset[1];

            for (int dy = 1; dy <= TOWER_HEIGHT; dy++) {
                BlockPos pos = new BlockPos(tx, cy + dy, tz);
                if (box.isInside(pos))
                    level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 2);
            }

            BlockPos tntPos = new BlockPos(tx, cy + TOWER_HEIGHT + 1, tz);
            if (box.isInside(tntPos))
                level.setBlock(tntPos, Blocks.TNT.defaultBlockState(), 2);
        }
    }
}