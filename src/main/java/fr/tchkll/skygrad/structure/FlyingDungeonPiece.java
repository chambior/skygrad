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
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.List;

import static fr.tchkll.skygrad.utils.algo.Drawer.line;

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
    @ParametersAreNonnullByDefault
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("cx", center.getX());
        tag.putInt("cy", center.getY());
        tag.putInt("cz", center.getZ());
    }

    private static BoundingBox makeBbox(BlockPos c) {
        int r = 80; // was ~32 → now safe margin
        int h = 32;

        return new BoundingBox(
                c.getX() - r, c.getY() - h, c.getZ() - r,
                c.getX() + r, c.getY() + h, c.getZ() + r);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {

        int size = 64;

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        int half = size / 2;

        // --- Generate towers (same logic as Python) ---
        int numTowers = 3 + random.nextInt(5); // 3–7
        int[][] towers = new int[numTowers][2];

        for (int t = 0; t < numTowers; t++) {
            int minAngle = (int)(360.0 * (t + 0.25) / numTowers);
            int maxAngle = (int)(360.0 * (t + 0.75) / numTowers);

            int minR = size / 4;
            int maxR = size / 3;

            int r = random.nextInt(maxR - minR + 1) + minR;
            int omega = random.nextInt(maxAngle - minAngle + 1) + minAngle;

            double rad = Math.toRadians(omega);

            int tx = (int)(r * Math.cos(rad));
            int tz = (int)(r * Math.sin(rad));

            towers[t][0] = tx;
            towers[t][1] = tz;
        }

        // --- Fill polygon (stonebrick floor) ---
        for (int z = -half; z <= half; z++) {
            int worldZ = cz + z;

            // find intersections
            List<Double> intersections = getIntersections(numTowers, towers, z);

            intersections.sort(Double::compare);

            for (int i = 0; i < intersections.size(); i += 2) {
                if (i + 1 >= intersections.size()) break;

                int xStart = (int)Math.ceil(intersections.get(i));
                int xEnd   = (int)Math.floor(intersections.get(i + 1));

                for (int x = xStart; x <= xEnd; x++) {
                    BlockPos pos = new BlockPos(cx + x, cy, worldZ);
                    if (!box.isInside(pos)) continue;

                    level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }
            }
        }

        // --- Draw walls (black) + inner offset (green) ---
        for (int i = 0; i < numTowers; i++) {
            int[] prev = towers[(i - 1 + numTowers) % numTowers];
            int[] curr = towers[i];

            int x1 = prev[0];
            int z1 = prev[1];
            int x2 = curr[0];
            int z2 = curr[1];

            java.util.List<int[]> line = line(x1, z1, x2, z2);

            double angle = Math.atan2(z2 - z1, x2 - x1);
            double deg = Math.toDegrees(angle);

            String direction;
            if (deg >= 45 && deg <= 135) direction = "east";
            else if (deg >= 135 || deg <= -135) direction = "south";
            else if (deg >= -135 && deg <= -45) direction = "west";
            else direction = "north";

            for (int[] p : line) {
                int x = p[0];
                int z = p[1];

                BlockPos wallPos = new BlockPos(cx + x, cy + 1, cz + z);
                if (box.isInside(wallPos)) {
                    level.setBlock(wallPos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }

                int ox = 0, oz = 0;
                if (direction.equals("north")) oz = -1;
                if (direction.equals("south")) oz = 1;
                if (direction.equals("east"))  ox = 1;
                if (direction.equals("west"))  ox = -1;

                BlockPos innerPos = new BlockPos(cx + x + ox, cy + 1, cz + z + oz);
                if (box.isInside(innerPos)) {
                    level.setBlock(innerPos, Blocks.DEEPSLATE.defaultBlockState(), 2);
                }
            }
        }

        // --- Towers (purple = obsidian) ---
        for (int[] t : towers) {
            BlockPos pos = new BlockPos(cx + t[0], cy + 1, cz + t[1]);
            if (box.isInside(pos)) {
                level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 2);
            }
        }

        // --- Center (blue = crying obsidian) ---
        BlockPos centerPos = new BlockPos(cx, cy + 1, cz);
        if (box.isInside(centerPos)) {
            level.setBlock(centerPos, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        }
    }

    private static @NotNull List<Double> getIntersections(int numTowers, int[][] towers, int z) {
        List<Double> intersections = new java.util.ArrayList<>();

        for (int i = 0; i < numTowers; i++) {
            int x1 = towers[i][0];
            int z1 = towers[i][1];
            int x2 = towers[(i + 1) % numTowers][0];
            int z2 = towers[(i + 1) % numTowers][1];

            if (z1 == z2) continue;
            if (z < Math.min(z1, z2) || z >= Math.max(z1, z2)) continue;

            double x = x1 + (double)(z - z1) * (x2 - x1) / (z2 - z1);
            intersections.add(x);
        }
        return intersections;
    }
}